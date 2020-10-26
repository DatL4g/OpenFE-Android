package de.datlag.openfe.data

import androidx.datastore.Serializer
import de.datlag.openfe.datastore.UserPreferences
import io.michaelrocks.paranoid.Obfuscate
import java.io.InputStream
import java.io.OutputStream

@Obfuscate
class UserSerializer : Serializer<UserPreferences> {

    override fun readFrom(input: InputStream): UserPreferences {
        return try {
            UserPreferences.parseFrom(input)
        } catch (exception: Exception) {
            UserPreferences.newBuilder()
                .setGithubCode(String())
                .setGithubAccessToken(String())
                .build()
        }
    }

    override fun writeTo(t: UserPreferences, output: OutputStream) {
        t.writeTo(output)
    }
}
