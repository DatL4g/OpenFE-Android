package de.datlag.openfe.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.datlag.openfe.commons.isInternal
import de.datlag.openfe.commons.matchWithApps
import de.datlag.openfe.commons.mutableCopyOf
import de.datlag.openfe.commons.parentDir
import de.datlag.openfe.fragments.ExplorerFragmentArgs
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.recycler.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class ExplorerViewModel(
    private val explorerFragmentArgs: ExplorerFragmentArgs,
    private val appsViewModel: AppsViewModel
) : ViewModel() {

    private val startDirectory: File = getStartDirectory(explorerFragmentArgs)

    var currentDirectory: MutableLiveData<File> = MutableLiveData(startDirectory)
    var currentSubDirectories: MutableLiveData<List<ExplorerItem>> = MutableLiveData(listOf())

    private val systemAppsObserver = Observer<AppList> { list ->
        matchNewAppsToDirectories(list)
    }

    private val currentDirectoryObserver = Observer<File> { dir ->
        val fileList = dir.listFiles()?.toMutableList() ?: mutableListOf()
        val startDirParent = startDirectory.parentDir

        if (dir == startDirParent) {
            for (item in explorerFragmentArgs.storage.list) {
                if (!fileList.contains(item.rootFile)) {
                    fileList.add(item.rootFile)
                }
            }
        }

        if (dir == File("/")) {
            val storagePath = File("/storage")
            if (!fileList.contains(storagePath)) {
                fileList.add(storagePath)
            }
        } else {
            val parentFile = ExplorerItem(
                FileItem(
                    dir.parentDir,
                    ".."
                ),
                null, false, false
            )
            currentSubDirectories.value = listOf(parentFile)
        }
        createSubDirectories(fileList)
    }

    init {
        appsViewModel.systemApps.observeForever(systemAppsObserver)
        currentDirectory.observeForever(currentDirectoryObserver)
    }

    private fun getStartDirectory(args: ExplorerFragmentArgs = explorerFragmentArgs): File {
        val file = File(args.storage.list[args.storage.selected].usage.file.absolutePath)
        return if (file.isDirectory) file else file.parentDir
    }

    private fun createSubDirectories(fileList: MutableList<File>) = viewModelScope.launch(Dispatchers.IO) {
        fileList.sort()
        val iterator = fileList.listIterator()

        while (iterator.hasNext()) {
            val file = iterator.next()

            if (!file.isHidden) {
                val explorerItem = ExplorerItem.from(file, appsViewModel.systemApps.value ?: listOf())
                val copy = currentSubDirectories.value?.mutableCopyOf() ?: mutableListOf()
                copy.add(explorerItem)

                withContext(Dispatchers.Main) {
                    currentSubDirectories.value = copy
                }
            }
        }
    }

    fun moveToPath(path: File, force: Boolean = false) {
        val newPath = if (path.isInternal() && !force) startDirectory else path

        currentDirectory.value = if (newPath.isDirectory) {
            newPath
        } else {
            newPath.parentDir
        }
    }

    private fun matchNewAppsToDirectories(list: AppList) = viewModelScope.launch(Dispatchers.IO) {
        val copy = currentSubDirectories.value?.mutableCopyOf() ?: mutableListOf()

        for (explorerItem in copy) {
            explorerItem.matchWithApps(list)
        }

        withContext(Dispatchers.Main) {
            try {
                currentSubDirectories.value = copy
            } catch (exception: Exception) {
                currentSubDirectories.postValue(copy)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        appsViewModel.systemApps.removeObserver(systemAppsObserver)
        currentDirectory.removeObserver(currentDirectoryObserver)
    }

}
