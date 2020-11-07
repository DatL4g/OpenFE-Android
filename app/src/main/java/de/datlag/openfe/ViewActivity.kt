package de.datlag.openfe

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.mimemagic.MimePrefix
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.fragments.view.ImageFragment
import de.datlag.openfe.fragments.view.VideoFragment
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
class ViewActivity : AdvancedActivity(R.layout.activity_view) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ViewerAppTheme)

        val fragment = when {
            intent?.type?.startsWith(MimePrefix.IMAGE) == true -> ImageFragment(intent)
            intent?.type?.startsWith(MimePrefix.VIDEO) == true -> VideoFragment(intent)
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
