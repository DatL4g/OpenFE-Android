package de.datlag.openfe.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.datlag.openfe.bottomsheets.FileProgressSheet
import de.datlag.openfe.commons.deleteRecursively
import de.datlag.openfe.commons.getRootOfStorage
import de.datlag.openfe.commons.isInternal
import de.datlag.openfe.commons.mutableCopyOf
import de.datlag.openfe.databinding.FragmentExplorerBinding
import de.datlag.openfe.fragments.ExplorerFragmentArgs
import de.datlag.openfe.recycler.adapter.ExplorerRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.recycler.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.contracts.ExperimentalContracts

typealias FileLiveData = MutableLiveData<File>
typealias ExplorerLiveData = MutableLiveData<List<ExplorerItem>>

@ExperimentalContracts
class ExplorerViewModel(
    private val explorerFragmentArgs: ExplorerFragmentArgs,
    private val appsViewModel: AppsViewModel
) : ViewModel() {

    var startDirectory: File = getStartDirectory(explorerFragmentArgs)
        private set

    var currentDirectory: File = startDirectory
        private set

    var directory: FileLiveData = FileLiveData(currentDirectory)
    var directories: ExplorerLiveData = MutableLiveData()

    private var systemAppsObserver = Observer<AppList> { list ->
        matchDirectoriesWithApps(list)
    }

    var selectedItems = mutableListOf<ExplorerItem>()

    var isSearching: Boolean = false

    init {
        appsViewModel.systemApps.observeForever(systemAppsObserver)
    }

    private fun matchDirectoriesWithApps(list: AppList) = viewModelScope.launch(Dispatchers.IO) {
        directories.value?.let {
            val explorerCopy = it.mutableCopyOf()

            for (explorerItem in explorerCopy) {
                try {
                    matchDirectoryWithApps(explorerItem, list)
                } catch (ignored: Exception) {
                }
            }
            withContext(Dispatchers.Main) {
                try {
                    directories.value = explorerCopy
                } catch (exception: Exception) {
                    directories.postValue(explorerCopy)
                }
            }
        }
    }

    private fun matchDirectoryWithApps(explorerItem: ExplorerItem, list: AppList): ExplorerItem {
        return try {
            for (appItem in list) {
                if (explorerItem.fileItem.name?.equals(appItem.name, true) == true ||
                    explorerItem.fileItem.name?.equals(appItem.packageName, true) == true ||
                    explorerItem.fileItem.file.name.equals(appItem.name, true) ||
                    explorerItem.fileItem.file.name.equals(appItem.packageName, true) ||
                    explorerItem.fileItem.file.path.equals(appItem.sourceDir, true) ||
                    explorerItem.fileItem.file.path.equals(appItem.publicSourceDir, true) ||
                    explorerItem.fileItem.file.path.equals(appItem.dataDir, true) ||
                    explorerItem.fileItem.file.absolutePath.equals(appItem.sourceDir, true) ||
                    explorerItem.fileItem.file.absolutePath.equals(appItem.publicSourceDir, true) ||
                    explorerItem.fileItem.file.absolutePath.equals(appItem.dataDir, true)
                ) {
                    explorerItem.appItem = appItem
                }
            }
            explorerItem
        } catch (exception: Exception) {
            explorerItem
        }
    }

    private fun getStartDirectory(args: ExplorerFragmentArgs): File {
        val file = File(args.storage.list[args.storage.item].usage.file.absolutePath)
        return if (file.isDirectory) file else file.parentFile ?: File(file.getRootOfStorage())
    }

    fun moveToPath(path: File, force: Boolean = false) {
        val newPath = if (path.isInternal() && !force) startDirectory else path

        if (newPath.isDirectory) {
            val fileList = newPath.listFiles()?.toMutableList() ?: mutableListOf()

            val rootFile =
                explorerFragmentArgs.storage.list[explorerFragmentArgs.storage.item].rootFile
            val rootStorageFile = rootFile.parentFile
                ?: if (rootFile.parent != null) File(rootFile.parent!!) else rootFile
            if (newPath == rootStorageFile) {
                for (item in explorerFragmentArgs.storage.list) {
                    if (!fileList.contains(item.rootFile)) {
                        fileList.add(item.rootFile)
                    }
                }
            }
            if (newPath == File("/")) {
                val storagePath = File("/storage")
                if (!fileList.contains(storagePath)) {
                    fileList.add(storagePath)
                }
                directories.value = mutableListOf()
            } else {
                val parentFile = ExplorerItem(
                    FileItem(
                        newPath.parentFile
                            ?: if (newPath.parent != null) File(newPath.parent!!) else newPath,
                        ".."
                    ),
                    null, false
                )
                directories.value = mutableListOf(parentFile)
            }

            currentDirectory = newPath

            createExplorerDirectories(fileList)
        }
    }

    private fun createExplorerDirectories(fileList: MutableList<File>) =
        viewModelScope.launch(Dispatchers.IO) {
            fileList.sort()
            val fileIterator = fileList.listIterator()

            while (fileIterator.hasNext()) {
                val file = fileIterator.next()

                if (!file.isHidden) {
                    val fileItem = FileItem(file)

                    val explorerItem = if (!appsViewModel.systemApps.value.isNullOrEmpty()) {
                        matchDirectoryWithApps(
                            ExplorerItem(fileItem),
                            appsViewModel.systemApps.value!!
                        )
                    } else {
                        ExplorerItem(fileItem)
                    }

                    withContext(Dispatchers.Main) {
                        if (directories.value != null) {
                            directories.value =
                                directories.value!!.mutableCopyOf().apply { add(explorerItem) }
                        } else {
                            directories.value = mutableListOf(explorerItem)
                        }
                    }
                }
            }
        }

    fun recyclerSelectUpdate(
        explorerItem: ExplorerItem,
        position: Int,
        recyclerAdapter: ExplorerRecyclerAdapter,
        binding: FragmentExplorerBinding?
    ): ExplorerItem = with(binding) {
        recyclerAdapter.differ.currentList.let {
            val explorerItems = it.mutableCopyOf()

            for (i in 0 until explorerItems.size) {
                if (explorerItems[i] == explorerItem) {
                    explorerItems.removeAt(i)
                    explorerItem.selected = !explorerItem.selected
                    explorerItems.add(i, explorerItem)

                    directories.value = explorerItems
                }
            }
        }

        val holder: ExplorerRecyclerAdapter.ViewHolder? =
            this?.explorerRecycler?.findViewHolderForAdapterPosition(position) as ExplorerRecyclerAdapter.ViewHolder?
        holder?.binding?.explorerCheckbox?.isChecked = explorerItem.selected
        return explorerItem
    }

    fun clearSelectedItems(copiedList: List<ExplorerItem>, binding: FragmentExplorerBinding?) =
        with(binding) {
            selectedItems.clear()
            copiedList.let {
                val explorerItems = it.mutableCopyOf()

                for (i in 0 until explorerItems.size) {
                    explorerItems[i].selected = false

                    val holder: ExplorerRecyclerAdapter.ViewHolder? =
                        this?.explorerRecycler?.findViewHolderForAdapterPosition(i) as ExplorerRecyclerAdapter.ViewHolder?
                    holder?.binding?.explorerCheckbox?.isChecked = false
                }

                directories.value = explorerItems
            }

            if (isSearching) {
                isSearching = false
                this?.explorerSearchView?.searchEditText?.text = null
                directories.value = copiedList
            }
        }

    fun deleteSelectedItems(sheet: FileProgressSheet, items: List<ExplorerItem> = selectedItems, done: (() -> Unit)? = null): Job {
        val initialList = FloatArray(items.size)
        val copyList = initialList.copyOf()

        sheet.updateProgressList(initialList)
        val job = viewModelScope.launch(Dispatchers.IO) {
            for (pos in items.indices) {
                items[pos].fileItem.file.deleteRecursively { percent ->
                    copyList[pos] = percent
                    sheet.updateProgressList(copyList)
                }
            }
            withContext(Dispatchers.Main) {
                done?.invoke()
            }
        }

        sheet.leftClickListener = {
            job.cancel()
        }
        sheet.rightClickListener = {
            job.start()
        }
        return job
    }

    override fun onCleared() {
        super.onCleared()
        appsViewModel.systemApps.removeObserver(systemAppsObserver)
    }
}
