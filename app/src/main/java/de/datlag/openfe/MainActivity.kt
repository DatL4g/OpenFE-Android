package de.datlag.openfe

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.datlag.openfe.databinding.ActivityMainBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentNoAdPermission
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.ExperimentalSerializationApi
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@Obfuscate
class MainActivity : AdvancedActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding(R.id.container)

    val toolbar: Toolbar
        get() = binding.toolBar

    val searchView: SimpleSearchView
        get() = binding.searchview

    val bottomAppBar: BottomAppBar
        get() = binding.bottomAppBar

    val bottomNavigation: BottomNavigationView
        get() = binding.bottomNavigation

    val fab: FloatingActionButton
        get() = binding.fab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()

        gitHubViewModel.isNoAdsPermitted.observe(this) { permitted ->
            if (gitHubViewModel.reposContributorListLoaded && gitHubViewModel.authenticatedUserLoaded) {
                (getCurrentNavFragment() as? FragmentNoAdPermission?)?.onNoAdPermissionChanged(permitted)
            }
        }
    }

    private fun initViews() = with(binding) {
        setSupportActionBar(toolBar)
    }

    private fun getCurrentNavFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        val fragmentList = navHostFragment?.childFragmentManager?.fragments
        return if (!fragmentList.isNullOrEmpty() && fragmentList.size >= 1) fragmentList[0] else null
    }

    fun githubOAuth() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.Builder()
            .scheme("https")
            .authority("github.com")
            .appendPath("login")
            .appendPath("oauth")
            .appendPath("authorize")
            .appendQueryParameter("client_id", getString(R.string.github_secret_client_id))
            .appendQueryParameter("redirect_uri", getString(R.string.github_callback_uri))
            .appendQueryParameter("scope", "user:read")
            .build()
        startActivity(intent)
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        newIntent?.let {
            it.data?.let { data ->
                if (data.toString().startsWith(getString(R.string.github_callback_uri))) {
                    val code = data.getQueryParameter("code")
                    gitHubViewModel.requestAccessTokenAndLogin(code)
                }
            }
        }
    }

    override fun onBackPressed() {
        val pressed: Boolean = (getCurrentNavFragment() as? FragmentBackPressed?)?.onBackPressed() ?: true

        if (pressed) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return (getCurrentNavFragment() as? FragmentOptionsMenu?)?.onCreateMenu(menu, menuInflater) ?: true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.e("activity called")

        when (requestCode) {
            1337 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.e("activity granted")
                } else {
                    Timber.e("activity not granted")
                }
            }
        }
    }
}
