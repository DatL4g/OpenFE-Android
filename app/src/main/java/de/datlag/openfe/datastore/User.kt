package de.datlag.openfe.datastore

import de.datlag.openfe.models.AccessToken
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
data class User(
    var githubCode: String,
    var githubAccessToken: AccessToken,
)
