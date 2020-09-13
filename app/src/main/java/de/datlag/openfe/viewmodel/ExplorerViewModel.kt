package de.datlag.openfe.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.datlag.openfe.commons.getRootOfStorage
import de.datlag.openfe.commons.isInternal
import de.datlag.openfe.commons.mutableCopyOf
import de.datlag.openfe.fragments.ExplorerFragmentArgs
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.recycler.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.Exception

typealias FileLiveData = MutableLiveData<File>
typealias ExplorerLiveData = MutableLiveData<List<ExplorerItem>>

class ExplorerViewModel(private val explorerFragmentArgs: ExplorerFragmentArgs, private val appsViewModel: AppsViewModel) : ViewModel() {

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
                } catch (ignored: Exception) { }
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
                if (explorerItem.fileItem.name?.equals(appItem.name, true) == true
                    || explorerItem.fileItem.name?.equals(appItem.packageName, true) == true
                    || explorerItem.fileItem.file.name.equals(appItem.name, true)
                    || explorerItem.fileItem.file.name.equals(appItem.packageName, true)
                    || explorerItem.fileItem.file.path.equals(appItem.sourceDir, true)
                    || explorerItem.fileItem.file.path.equals(appItem.publicSourceDir, true)
                    || explorerItem.fileItem.file.path.equals(appItem.dataDir, true)
                    || explorerItem.fileItem.file.absolutePath.equals(appItem.sourceDir, true)
                    || explorerItem.fileItem.file.absolutePath.equals(appItem.publicSourceDir, true)
                    || explorerItem.fileItem.file.absolutePath.equals(appItem.dataDir, true)) {
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
        return if(file.isDirectory) file else file.parentFile ?: File(file.getRootOfStorage())
    }

    fun moveToPath(path: File, force: Boolean = false) {
        val newPath = if(path.isInternal() && !force) startDirectory else path

        if (newPath.isDirectory) {
            val fileList = newPath.listFiles()?.toMutableList() ?: mutableListOf()


            val rootFile = explorerFragmentArgs.storage.list[explorerFragmentArgs.storage.item].rootFile
            val rootStorageFile = rootFile.parentFile ?: if (rootFile.parent != null) File(rootFile.parent!!) else rootFile
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
                val parentFile = ExplorerItem(FileItem(newPath.parentFile ?: if (newPath.parent != null) File(newPath.parent!!) else newPath, ".."), null, false)
                directories.value = mutableListOf(parentFile)
            }


            currentDirectory = newPath

            createExplorerDirectories(fileList)
        }
    }

    private fun createExplorerDirectories(fileList: MutableList<File>) = viewModelScope.launch(Dispatchers.IO) {
        fileList.sort()
        val fileIterator = fileList.listIterator()

        while(fileIterator.hasNext()) {
            val file = fileIterator.next()

            if (!file.isHidden) {
                val fileItem = FileItem(file)

                val explorerItem = if (!appsViewModel.systemApps.value.isNullOrEmpty()) {
                    matchDirectoryWithApps(ExplorerItem(fileItem), appsViewModel.systemApps.value!!)
                } else {
                    ExplorerItem(fileItem)
                }

                withContext(Dispatchers.Main) {
                    if (directories.value != null) {
                        directories.value = directories.value!!.mutableCopyOf().apply { add(explorerItem) }
                    } else {
                        directories.value = mutableListOf(explorerItem)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        appsViewModel.systemApps.removeObserver(systemAppsObserver)
    }
}