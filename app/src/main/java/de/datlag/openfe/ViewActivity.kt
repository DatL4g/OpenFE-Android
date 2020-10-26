package de.datlag.openfe

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.fragments.view.ImageFragment
import de.datlag.openfe.fragments.view.VideoFragment
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
class ViewActivity : AdvancedActivity(R.layout.activity_view) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragment = when {
            intent?.type?.startsWith("image/") == true -> ImageFragment(intent)
            intent?.type?.startsWith("video/") == true -> VideoFragment(intent)
            else -> null
        }

        if (fragment == null) {
            finish()
            return
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.view_nav_host_fragment, fragment)
            .commit()
    }
}
