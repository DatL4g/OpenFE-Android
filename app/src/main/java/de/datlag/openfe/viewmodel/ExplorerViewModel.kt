package de.datlag.openfe.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.datlag.mimemagic.commons.getMimeData
import de.datlag.openfe.commons.copyOf
import de.datlag.openfe.commons.copyTo
import de.datlag.openfe.commons.countRecursively
import de.datlag.openfe.commons.deleteRecursively
import de.datlag.openfe.commons.getRootOfStorage
import de.datlag.openfe.commons.isInternal
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.matchWithApps
import de.datlag.openfe.commons.mutableCopyOf
import de.datlag.openfe.commons.parentDir
import de.datlag.openfe.commons.updateValue
import de.datlag.openfe.commons.usage
import de.datlag.openfe.filter.MimeTypeFilter
import de.datlag.openfe.fragments.ExplorerFragmentArgs
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.recycler.data.FileItem
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class ExplorerViewModel constructor(
    private val context: Context,
    private val explorerFragmentArgs: ExplorerFragmentArgs,
    private val backupViewModel: BackupViewModel
) : ViewModel() {

    val startDirectory: File = getStartDirectory(explorerFragmentArgs)

    val currentDirectory: MutableLiveData<File> = MutableLiveData(startDirectory)
    var isObservingCurrentDirectory: Boolean = false
    val currentSubDirectories: MutableLiveData<List<ExplorerItem>> = MutableLiveData()

    val systemApps: MutableLiveData<List<AppItem>> = MutableLiveData()

    val selectedItems = MutableLiveData<List<ExplorerItem>>()
    var reservedItems = listOf<ExplorerItem>()

    val searchShown: MutableLiveData<Boolean> = MutableLiveData(false)
    var searched: Boolean = false
    private var searchDirectoriesCopy: List<ExplorerItem> = listOf()
    private var searchJob: Job? = null
    private var previousSearchText: String? = null

    private val systemAppsObserver = Observer<AppList> { list ->
        if (isObservingCurrentDirectory) {
            viewModelScope.launch(Dispatchers.IO) {
                val matchedAppItems = matchNewAppsToDirectories(list)
                withContext(Dispatchers.Main) {
                    try {
                        currentSubDirectories.value = matchedAppItems
                    } catch (exception: Exception) {
                        currentSubDirectories.postValue(matchedAppItems)
                    }
                }
            }
        }
    }

    private val currentDirectoryObserver = Observer<File> { dir ->
        currentSubDirectories.updateValue(listOf())

        val fileList: MutableList<File> = dir.listFiles()?.toMutableList() ?: mutableListOf()
        val startDirParent = File(startDirectory.getRootOfStorage()).parentDir

        if (dir == startDirParent) {
            for (item in explorerFragmentArgs.storage.list) {
                if (!fileList.contains(item.rootFile)) {
                    fileList.add(item.rootFile)
                }
            }
        }

        if (dir == File("/")) {
            val dataPath = File("/data")
            if (!fileList.contains(dataPath)) {
                fileList.add(dataPath)
            }

            val storagePath = File("/storage")
            if (!fileList.contains(storagePath)) {
                fileList.add(storagePath)
            }

            val systemPath = File("/system")
            if (!fileList.contains(systemPath)) {
                fileList.add(systemPath)
            }
        } else {
            val parentFile = ExplorerItem(
                FileItem(
                    dir.parentDir,
                    ".."
                ),
                null, false, false
            )
            currentSubDirectories.updateValue(listOf(parentFile))
        }
        createSubDirectories(fileList)
    }

    private val selectedItemsObserver = Observer<List<ExplorerItem>> {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSubDirs = matchSelectedWithCurrentSubDirs()
            withContext(Dispatchers.Main) {
                currentSubDirectories.updateValue(currentSubDirs)
            }
        }
    }

    init {
        systemApps.observeForever(systemAppsObserver)
        selectedItems.observeForever(selectedItemsObserver)

        if (explorerFragmentArgs.storage.mimeTypeFilter == null) {
            currentDirectory.observeForever(currentDirectoryObserver)
            isObservingCurrentDirectory = true
        } else {
            getFilesByMimeTypeFilter(explorerFragmentArgs.storage.mimeTypeFilter)
        }
    }

    private fun getStartDirectory(args: ExplorerFragmentArgs = explorerFragmentArgs): File {
        val file = File(args.storage.list[args.storage.selected].usage.file.absolutePath)
        return if (file.isDirectory) file else file.parentDir
    }

    fun switchMimeTypeFilterToNormal() {
        if (!isObservingCurrentDirectory) {
            currentDirectory.observeForever(currentDirectoryObserver)
        }
        currentDirectory.value = currentDirectory.value
    }

    private fun getFilesByMimeTypeFilter(filter: MimeTypeFilter) = viewModelScope.launch(Dispatchers.IO) {
        val fileList: MutableList<File> = mutableListOf()
        for (location in explorerFragmentArgs.storage.list) {
            location.usage.file.walkTopDown().fold(
                true,
                {
                    _, file ->
                    if (!file.isDirectory) {
                        val fileMime = file.getMimeData(context)
                        val accept = when {
                            filter.acceptApplication && fileMime.isApplication -> true
                            filter.acceptArchive && fileMime.isArchive -> true
                            filter.acceptAudio && fileMime.isAudio -> true
                            filter.acceptDocument && fileMime.isDocument -> true
                            filter.acceptFont && fileMime.isFont -> true
                            filter.acceptImage && fileMime.isImage -> true
                            filter.acceptText && fileMime.isText -> true
                            filter.acceptVideo && fileMime.isVideo -> true
                            else -> false
                        }
                        if (accept) {
                            fileList.add(file)
                        }
                    }
                    file.exists()
                }
            )
        }

        createSubDirectories(fileList)
    }

    private fun createSubDirectories(fileList: MutableList<File>) =
        viewModelScope.launch(Dispatchers.IO) {
            fileList.sortWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name })
            val iterator = fileList.listIterator()

            while (iterator.hasNext()) {
                val file = iterator.next()

                if (!file.isHidden) {
                    val explorerItem =
                        ExplorerItem.from(file, systemApps.value ?: listOf())
                    val copy = currentSubDirectories.value?.mutableCopyOf() ?: mutableListOf()
                    copy.add(explorerItem)

                    withContext(Dispatchers.Main) {
                        currentSubDirectories.updateValue(copy)
                    }
                }
            }
        }

    fun switchToPath(path: File, force: Boolean = false) {
        val newPath = if (path.isInternal() && !force) startDirectory else path

        currentDirectory.updateValue(
            if (newPath.isDirectory) {
                newPath
            } else {
                newPath.parentDir
            }
        )
    }

    fun searchCurrentDirectories(text: String?, recursively: Boolean) {
        if (previousSearchText == text || (currentSubDirectories.value.isNullOrEmpty() && searchDirectoriesCopy.isEmpty())) {
            return
        }
        previousSearchText = text
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            if (text.isNotCleared()) {
                if (searchDirectoriesCopy.isEmpty()) {
                    searchDirectoriesCopy = currentSubDirectories.value!!.copyOf()
                }
                withContext(Dispatchers.Main) {
                    currentSubDirectories.updateValue(listOf())
                }

                for (explorerItem in searchDirectoriesCopy) {
                    if (recursively && selectedItems.value.isNullOrEmpty()) {
                        explorerItem.fileItem.file.walkTopDown().fold(true) { res, file ->
                            val fileMatches = file.name.contains(text, true) && !file.isHidden
                            if (fileMatches) {
                                withContext(Dispatchers.Main) {
                                    currentSubDirectories.updateValue(
                                        (
                                            currentSubDirectories.value?.mutableCopyOf()
                                                ?: mutableListOf()
                                            ).apply {
                                            add(
                                                ExplorerItem.from(
                                                    file,
                                                    systemApps.value ?: listOf()
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                            (file.exists() && fileMatches) && res
                        }
                    } else {
                        if (explorerItem.fileItem.name?.contains(
                                text,
                                true
                            ) == true || explorerItem.fileItem.file.name.contains(text, true)
                        ) {
                            withContext(Dispatchers.Main) {
                                currentSubDirectories.updateValue(
                                    (
                                        currentSubDirectories.value?.toMutableList()
                                            ?: mutableListOf()
                                        ).apply { add(explorerItem) }
                                )
                            }
                        }
                    }
                }
            } else {
                if (searchDirectoriesCopy.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        currentDirectory.value = currentDirectory.value
                    }
                }
            }

            val currentSubDirs = matchSelectedWithCurrentSubDirs()
            withContext(Dispatchers.Main) {
                currentSubDirectories.updateValue(currentSubDirs)
            }
        }
    }

    private fun matchSelectedWithCurrentSubDirs(): List<ExplorerItem> {
        val selectedList = selectedItems.value ?: listOf()
        val currentSubDirs = currentSubDirectories.value?.mutableCopyOf() ?: mutableListOf()
        for (selected in selectedList) {
            for (pos in 0 until currentSubDirs.size) {
                if (currentSubDirs[pos].fileItem == selected.fileItem) {
                    currentSubDirs.removeAt(pos)
                    currentSubDirs.add(pos, selected)
                }
            }
        }
        return currentSubDirs
    }

    private fun matchNewAppsToDirectories(list: AppList): List<ExplorerItem> {
        val copy = currentSubDirectories.value?.mutableCopyOf() ?: mutableListOf()

        for (explorerItem in copy) {
            explorerItem.matchWithApps(list)
        }
        return copy
    }

    fun selectItem(explorerItem: ExplorerItem): Boolean {
        val selectedItemsList = selectedItems.value?.mutableCopyOf() ?: mutableListOf()

        if (selectedItemsList.contains(explorerItem)) {
            selectedItemsList.remove(explorerItem)
        }

        if (explorerItem.selectable) {
            explorerItem.selected = !explorerItem.selected
        } else {
            explorerItem.selected = false
        }

        if (explorerItem.selected) {
            selectedItemsList.add(explorerItem)
        }
        selectedItems.updateValue(selectedItemsList)
        return explorerItem.selected
    }

    fun clearAllSelectedItems() = viewModelScope.launch(Dispatchers.IO) {
        val copy = currentSubDirectories.value?.mutableCopyOf() ?: mutableListOf()
        for (item in copy) {
            item.selected = false
        }

        withContext(Dispatchers.Main) {
            selectedItems.updateValue(listOf())
            currentSubDirectories.updateValue(copy)
        }
    }

    fun countSelectedItems(
        direction: FileWalkDirection = FileWalkDirection.TOP_DOWN
    ): Int {
        val items = selectedItems.value ?: listOf()
        var count = 0
        for (item in items) {
            count += item.fileItem.file.countRecursively(direction)
        }
        return count
    }

    fun deleteSelectedItems(
        listener: ((progress: FloatArray) -> Unit)? = null,
        done: (() -> Unit)? = null
    ): Job {
        val items: List<ExplorerItem> = selectedItems.value ?: listOf()
        val initialList = FloatArray(items.size)
        val copyList = initialList.copyOf()

        listener?.invoke(initialList)

        return viewModelScope.launch(Dispatchers.IO) {
            for (pos in items.indices) {
                items[pos].fileItem.file.deleteRecursively { percent, scope ->
                    copyList[pos] = percent
                    scope.launch(Dispatchers.Main) {
                        listener?.invoke(copyList)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                done?.invoke()
                afterDeleteItems()
            }
        }
    }

    fun backupSelectedItemsPossible(): Boolean {
        val items: List<ExplorerItem> = selectedItems.value ?: listOf()
        var possible = true

        for (item in items) {
            val maxStorage = explorerFragmentArgs.storage.list[0].rootFile.usage.max - 5000000000
            val usableStorage = maxStorage - explorerFragmentArgs.storage.list[0].rootFile.usage.current
            val enoughStorage = item.fileItem.file.length() < usableStorage

            possible = enoughStorage
            if (!enoughStorage) {
                break
            }
        }
        return possible
    }

    fun backupSelectedItems(done: () -> Unit) {
        val items: List<ExplorerItem> = selectedItems.value ?: listOf()

        for (item in items) {
            backupItem(item.fileItem.file, done)
        }
    }

    private fun backupItem(file: File, done: (() -> Unit)) {
        backupViewModel.createBackup(file) {
            backupViewModel.insertBackup(it, done)
        }
    }

    fun afterDeleteItems() {
        currentDirectory.value = currentDirectory.value
        clearAllSelectedItems()
    }

    fun reserveSelectedItems() {
        reservedItems = selectedItems.value ?: listOf()
    }

    fun moveReversedItemsHere() {

    }

    fun pasteReservedItemsHere() {
        for (item in reservedItems) {
            currentDirectory.value?.let {
                item.fileItem.file.copyTo(it, false) {
                    Timber.e("Done: $it")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        systemApps.removeObserver(systemAppsObserver)
        if (isObservingCurrentDirectory) {
            currentDirectory.removeObserver(currentDirectoryObserver)
        }
        selectedItems.removeObserver(selectedItemsObserver)
    }
}
