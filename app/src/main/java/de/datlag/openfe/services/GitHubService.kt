package de.datlag.openfe.services

import de.datlag.openfe.models.AccessToken
import de.datlag.openfe.models.Contributor
import de.datlag.openfe.models.User
import io.michaelrocks.paranoid.Obfuscate
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

@Obfuscate
interface GitHubService {

    @Headers("Accept: application/json")
    @POST("login/oauth/access_token")
    @FormUrlEncoded
    fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String
    ): Call<AccessToken>

    @Headers("Accept: application/json")
    @GET("/repos/{owner}/{repo}/contributors")
    fun listContributors(@Path("owner") owner: String, @Path("repo") repo: String): Call<List<Contributor>>

    @Headers("Accept: application/json")
    @GET("/user")
    fun getUserWithToken(
        @Header("Authorization") token: String
    ): Call<User>
}
