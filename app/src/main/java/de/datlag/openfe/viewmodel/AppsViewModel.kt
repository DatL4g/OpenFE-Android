package de.datlag.openfe.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.datlag.openfe.other.AppsSortType
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.repository.AppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias AppList = List<AppItem>
typealias MutableAppList = MutableList<AppItem>
typealias AppLiveData = MutableLiveData<AppList>

class AppsViewModel @ViewModelInject constructor(
    private val appsRepository: AppsRepository
) : ViewModel() {

    private val allAppList: MutableAppList = mutableListOf()
    private var firstAllAppsFetch: Boolean = true

    private val nonSystemAppList: MutableAppList = mutableListOf()
    private var firstNonSystemAppsFetch: Boolean = true

    private var appsSortedByName: AppLiveData = getApps {
        appsSortedByName = AppLiveData(it.value?.sortedWith { o1, o2 ->
            o1.name.compareTo(o2.name, true)
        })
    }
    private var appsSortedByInstalled: AppLiveData = getApps {
        appsSortedByInstalled = AppLiveData(it.value?.sortedWith { o1, o2 ->
            o1.firstInstall.compareTo(o2.firstInstall)
        })
    }
    private var appsSortedByUpdated: AppLiveData = getApps {
        appsSortedByUpdated = AppLiveData(it.value?.sortedWith { o1, o2 ->
            o1.lastUpdate.compareTo(o2.lastUpdate)
        })
    }

    val apps = MediatorLiveData<List<AppItem>>()
    var sortType = AppsSortType.NAME
        set(value) {
            when (value) {
                AppsSortType.NAME -> {
                    if (field == value) {
                        appsSortedByName.value = appsSortedByName.value?.asReversed()
                    }
                    appsSortedByName.value?.let { apps.value = it }
                }
                AppsSortType.INSTALLED -> {
                    if (field == value) {
                        appsSortedByInstalled.value = appsSortedByInstalled.value?.asReversed()
                    }
                    appsSortedByInstalled.value?.let { apps.value = it }
                }
                AppsSortType.UPDATED -> {
                    if (field == value) {
                        appsSortedByUpdated.value = appsSortedByUpdated.value?.asReversed()
                    }
                    appsSortedByUpdated.value?.let { apps.value = it }
                }
            }
            field = value
        }

    private var systemAppsSortedByName: AppLiveData = getApps(false) {
        systemAppsSortedByName = it
    }

    val systemApps = MediatorLiveData<List<AppItem>>()

    init {
        apps.addSource(appsSortedByName) { result ->
            if (sortType == AppsSortType.NAME) {
                result?.let { apps.value = it }
            }
        }
        apps.addSource(appsSortedByInstalled) { result ->
            if (sortType == AppsSortType.INSTALLED) {
                result?.let { apps.value = it }
            }
        }
        apps.addSource(appsSortedByUpdated) { result ->
            if (sortType == AppsSortType.UPDATED) {
                result?.let { apps.value = it }
            }
        }
        systemApps.addSource(systemAppsSortedByName) { result ->
            result?.let { systemApps.value = it }
        }
    }

    private fun getApps(nonSystemOnly: Boolean = true, listener: ((AppLiveData) -> Unit)): AppLiveData {
        val liveData = AppLiveData(if (nonSystemOnly) nonSystemAppList else allAppList)

        val job = viewModelScope.launch(Dispatchers.IO) {
            appsRepository.loadApps(nonSystemOnly) {
                val item = AppItem.from(appsRepository.packageManager, it.second, it.first)

                var alreadyContaining = false
                for (app in (if (nonSystemOnly) nonSystemAppList else allAppList)) {
                    if(app.packageName == item.packageName) {
                        alreadyContaining = true
                    }
                }
                if (!alreadyContaining) {
                    (if (nonSystemOnly) nonSystemAppList else allAppList).add(item)
                }
            }
            liveData.postValue(if (nonSystemOnly) nonSystemAppList else allAppList)
        }

        if((if (nonSystemOnly) firstNonSystemAppsFetch else firstAllAppsFetch)) {
            viewModelScope.launch {
                job.join()
                withContext(Dispatchers.Main) {
                    listener.invoke(liveData)
                }
            }
        } else {
            listener.invoke(liveData)
        }

        return liveData
    }

}