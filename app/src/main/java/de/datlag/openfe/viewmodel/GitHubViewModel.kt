package de.datlag.openfe.viewmodel

import android.content.Context
import androidx.datastore.DataStore
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.datastore.UserPreferences
import de.datlag.openfe.helper.GitHubHelper
import de.datlag.openfe.models.AccessToken
import de.datlag.openfe.models.Contributor
import de.datlag.openfe.models.User
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@ExperimentalSerializationApi
@Obfuscate
class GitHubViewModel @ViewModelInject constructor(
    context: Context,
    val dataStore: DataStore<UserPreferences>
) : ViewModel() {

    private val gitHubHelper = GitHubHelper(context)
    private val repoContributorList: MutableLiveData<List<Contributor>> = MutableLiveData()
    val authenticatedUser: MutableLiveData<User> = MutableLiveData()
    val isNoAdsPermitted: MutableLiveData<Boolean> = MutableLiveData(false)
    var reposContributorListLoaded: Boolean = false
    var authenticatedUserLoaded: Boolean = false

    private val repoContributorListObserver = Observer<List<Contributor>> { list ->
        reposContributorListLoaded = true
        checkNoAdsPermission(authenticatedUser.value, list)
    }

    private val authenticatedUserObserver = Observer<User> { user ->
        authenticatedUserLoaded = true
        checkNoAdsPermission(user, repoContributorList.value ?: listOf())
    }

    init {
        repoContributorList.observeForever(repoContributorListObserver)
        authenticatedUser.observeForever(authenticatedUserObserver)
        requestAllRepoContributors()
        restoreUserFormDataStore()
    }

    private fun checkNoAdsPermission(user: User?, contributorList: List<Contributor>) {
        if (user == null) {
            isNoAdsPermitted.value = false
            return
        }

        if (contributorList.isNotEmpty()) {
            for (contributor in contributorList) {
                if ((contributor.login == user.login || contributor.id == user.id) && contributor.amount > 0) {
                    isNoAdsPermitted.value = true
                    break
                } else {
                    isNoAdsPermitted.value = false
                }
            }
        } else {
            isNoAdsPermitted.value = false
        }
    }

    fun requestAllRepoContributors() {
        gitHubHelper.getAllContributors { list ->
            repoContributorList.value = list
        }
    }

    fun requestAccessTokenAndLogin(code: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { preferences ->
                preferences.toBuilder()
                    .setGithubCode(code ?: String())
                    .build()
            }
        }

        if (!code.isNotCleared()) {
            authenticatedUser.value = null
            return
        }

        gitHubHelper.getAccessToken(code = code) {
            onNewAccessToken(it)
        }
    }

    fun requestAuthenticatedUser(token: String) {
        gitHubHelper.getUserWithToken(token) { user ->
            user?.let {
                authenticatedUser.value = it
            }
        }
    }

    fun onNewAccessToken(accessToken: AccessToken?) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { preferences ->
                preferences.toBuilder()
                    .setGithubAccessToken(accessToken?.token ?: String())
                    .build()
            }
        }

        if (accessToken == null) {
            authenticatedUser.value = null
            return
        }

        requestAuthenticatedUser(accessToken.token)
    }

    private fun restoreUserFormDataStore() = viewModelScope.launch(Dispatchers.IO) {
        if (authenticatedUser.value != null) {
            return@launch
        }

        dataStore.data.collect { preferences ->
            val token = preferences.githubAccessToken
            val code = preferences.githubCode

            if (token.isNotCleared()) {
                withContext(Dispatchers.Main) {
                    requestAuthenticatedUser(token)
                }
            } else {
                withContext(Dispatchers.Main) {
                    requestAccessTokenAndLogin(code)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.updateData { preferences ->
                preferences.toBuilder()
                    .setGithubCode(String())
                    .setGithubAccessToken(String())
                    .build()
            }
        }

        authenticatedUser.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repoContributorList.removeObserver(repoContributorListObserver)
        authenticatedUser.removeObserver(authenticatedUserObserver)
    }
}
