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
import de.datlag.openfe.models.GitHubAccessToken
import de.datlag.openfe.models.GitHubContributor
import de.datlag.openfe.models.GitHubUser
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@ExperimentalSerializationApi
@Obfuscate
class GitHubViewModel @ViewModelInject constructor(
    context: Context,
    val dataStore: DataStore<UserPreferences>
) : ViewModel() {

    private val gitHubHelper = GitHubHelper(context)
    private val repoGitHubContributorList: MutableLiveData<List<GitHubContributor>> = MutableLiveData()
    val authenticatedGitHubUser: MutableLiveData<GitHubUser> = MutableLiveData()
    val isNoAdsPermitted: MutableLiveData<Boolean> = MutableLiveData(false)
    var reposContributorListLoaded: Boolean = false
    var authenticatedUserLoaded: Boolean = false

    private val repoContributorListObserver = Observer<List<GitHubContributor>> { list ->
        reposContributorListLoaded = true
        checkNoAdsPermission(authenticatedGitHubUser.value, list)
    }

    private val authenticatedUserObserver = Observer<GitHubUser> { user ->
        authenticatedUserLoaded = true
        checkNoAdsPermission(user, repoGitHubContributorList.value ?: listOf())
    }

    init {
        repoGitHubContributorList.observeForever(repoContributorListObserver)
        authenticatedGitHubUser.observeForever(authenticatedUserObserver)
        requestAllRepoContributors()
        restoreUserFormDataStore()
    }

    private fun checkNoAdsPermission(gitHubUser: GitHubUser?, gitHubContributorList: List<GitHubContributor>) {
        if (gitHubUser == null) {
            isNoAdsPermitted.value = false
            return
        }

        if (gitHubContributorList.isNotEmpty()) {
            for (contributor in gitHubContributorList) {
                if ((contributor.login == gitHubUser.login || contributor.id == gitHubUser.id) && contributor.amount > 0) {
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
            repoGitHubContributorList.value = list
        }
    }

    fun requestAccessTokenAndLogin(code: String?) {
        runBlocking {
            dataStore.updateData { preferences ->
                preferences.toBuilder()
                    .setGithubCode(code ?: String())
                    .build()
            }
        }

        if (!code.isNotCleared()) {
            authenticatedGitHubUser.value = null
            return
        }

        gitHubHelper.getAccessToken(code = code) {
            onNewAccessToken(it)
        }
    }

    fun requestAuthenticatedUser(token: String) {
        gitHubHelper.getUserWithToken(token) { user ->
            user?.let {
                authenticatedGitHubUser.value = it
            }
        }
    }

    private fun onNewAccessToken(gitHubAccessToken: GitHubAccessToken?) {
        runBlocking {
            dataStore.updateData { preferences ->
                preferences.toBuilder()
                    .setGithubAccessToken(gitHubAccessToken?.token ?: String())
                    .build()
            }
        }

        if (gitHubAccessToken == null) {
            authenticatedGitHubUser.value = null
            return
        }

        requestAuthenticatedUser(gitHubAccessToken.token)
    }

    private fun restoreUserFormDataStore() = viewModelScope.launch(Dispatchers.Default) {
        if (authenticatedGitHubUser.value != null) {
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

        authenticatedGitHubUser.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repoGitHubContributorList.removeObserver(repoContributorListObserver)
        authenticatedGitHubUser.removeObserver(authenticatedUserObserver)
    }
}
