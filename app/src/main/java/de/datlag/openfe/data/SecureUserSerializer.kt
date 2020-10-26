package de.datlag.openfe.data

import androidx.datastore.Serializer
import de.datlag.openfe.datastore.UserPreferences
import de.datlag.openfe.provider.Crypto
import io.michaelrocks.paranoid.Obfuscate
import java.io.InputStream
import java.io.OutputStream

@Obfuscate
class SecureUserSerializer(private val crypto: Crypto) : Serializer<UserPreferences> {

    override fun readFrom(input: InputStream): UserPreferences {
        val defaultUserPreferences = UserPreferences.newBuilder()
                .setGithubCode(String())
                .setGithubAccessToken(String())
                .build()

        return if (input.available() != 0) {
            try {
                UserPreferences.parseFrom(crypto.decrypt(input))
            } catch (exception: Exception) {
                defaultUserPreferences
            }
        } else {
            defaultUserPreferences
        }
    }

    override fun writeTo(t: UserPreferences, output: OutputStream) {
        crypto.encrypt(t.toByteArray(), output)
    }
}
