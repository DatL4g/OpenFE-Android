package de.datlag.openfe.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.MainActivity
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.ConfirmActionSheet
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.databinding.FragmentSettingsBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOAuthCallback
import de.datlag.openfe.models.GitHubUser
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@Obfuscate
@AndroidEntryPoint
class SettingsFragment : AdvancedFragment(R.layout.fragment_settings), FragmentBackPressed, FragmentOAuthCallback {

    private val binding: FragmentSettingsBinding by viewBinding()

    private val navigationListener = View.OnClickListener {
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToOverviewFragment())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateToggle(getColor(R.color.defaultNavigationColor), navigationListener)
        updateBottom(false)
        updateFAB(false)

        updateGitHubViews(githubViewModel?.authenticatedGitHubUser?.value)

        binding.githubAccountRevoke.setOnClickListener {
            revokeGitHubAccess()
        }

        githubViewModel?.authenticatedGitHubUser?.observe(viewLifecycleOwner) {
            updateGitHubViews(it)
        }
    }

    override fun initToolbar() {
        toolbar?.menu?.clear()
    }

    private fun updateGitHubViews(user: GitHubUser?) = with(binding) {
        if (user == null) {
            githubAccountLogin.text = "Login"
            githubAccountSubTitle.text = "Currently not logged in"
            githubAccountLogin.setOnClickListener {
                val loginSheet = ConfirmActionSheet.githubLoginInstance()
                loginSheet.setRightButtonClickListener {
                    (activity as? MainActivity?)?.githubOAuth()
                }
                showBottomSheetFragment(loginSheet)
            }
        } else {
            githubAccountLogin.text = "Logout"
            githubAccountSubTitle.text = "Logged in as: ${user.login}"
            githubAccountLogin.setOnClickListener {
                val githubLogoutSheet = ConfirmActionSheet.githubLogoutInstance()
                githubLogoutSheet.setRightButtonClickListener {
                    githubViewModel?.logout()
                    revokeGitHubAccess()
                }
                showBottomSheetFragment(githubLogoutSheet)
            }
        }
    }

    private fun revokeGitHubAccess() {
        val revokeAccessSheet = ConfirmActionSheet.githubRevokeAccessInstance()
        revokeAccessSheet.setRightButtonClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.Builder()
                .scheme("https")
                .authority("github.com")
                .appendPath("settings")
                .appendPath("connections")
                .appendPath("applications")
                .appendPath(safeContext.getString(R.string.github_secret_client_id))
                .build()

            startActivity(intent)
        }
        showBottomSheetFragment(revokeAccessSheet)
    }

    override fun onBackPressed(): Boolean = true

    override fun onAuthCode(code: String?) {
    }
}
