package de.datlag.openfe.datastore

import de.datlag.openfe.models.GitHubAccessToken
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
data class User(
    var githubCode: String,
    var githubGitHubAccessToken: GitHubAccessToken,
)
