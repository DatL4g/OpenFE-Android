package de.datlag.openfe.helper

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import de.datlag.openfe.R
import de.datlag.openfe.models.AccessToken
import de.datlag.openfe.models.Contributor
import de.datlag.openfe.models.User
import de.datlag.openfe.services.GitHubService
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

@ExperimentalSerializationApi
@Obfuscate
class GitHubHelper(private val context: Context) {

    private val githubAuthRetrofit: Retrofit
    private val githubAuth: GitHubService
    private val githubApiRetrofit: Retrofit
    private val githubApi: GitHubService

    init {
        val contentType = MediaType.get("application/json")
        githubAuthRetrofit = Retrofit.Builder()
            .baseUrl("https://github.com")
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()
        githubAuth = githubAuthRetrofit.create(GitHubService::class.java)

        githubApiRetrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.github_api_url))
            .addConverterFactory(Json.asConverterFactory(contentType))
            .build()

        githubApi = githubApiRetrofit.create(GitHubService::class.java)
    }

    fun getAllContributors(
        owner: String = context.getString(R.string.github_owner),
        repo: String = context.getString(R.string.github_repo),
        listener: (List<Contributor>) -> Unit
    ) {
        val call = githubApi.listContributors(owner, repo)

        call.enqueue(object : Callback<List<Contributor>> {
            override fun onResponse(
                call: Call<List<Contributor>>,
                response: Response<List<Contributor>>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        listener.invoke(response.body()!!)
                    } else {
                        listener.invoke(listOf())
                    }
                } else {
                    listener.invoke(listOf())
                }
            }

            override fun onFailure(call: Call<List<Contributor>>, t: Throwable) {
                listener.invoke(listOf())
            }
        })
    }

    fun getAccessToken(clientId: String = context.getString(R.string.github_secret_client_id), clientSecret: String = context.getString(R.string.github_secret_client_secret), code: String, listener: (accessToken: AccessToken?) -> Unit) {
        val call = githubAuth.getAccessToken(clientId, clientSecret, code)

        call.enqueue(object : Callback<AccessToken> {
            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        listener.invoke(response.body()!!)
                    } else {
                        listener.invoke(null)
                    }
                } else {
                    listener.invoke(null)
                }
            }

            override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                listener.invoke(null)
            }
        })
    }

    fun getUserWithToken(token: String, listener: (user: User?) -> Unit) {
        val call = githubApi.getUserWithToken(" token $token")

        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        listener.invoke(response.body()!!)
                    } else {
                        listener.invoke(null)
                    }
                } else {
                    listener.invoke(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                listener.invoke(null)
            }
        })
    }
}
