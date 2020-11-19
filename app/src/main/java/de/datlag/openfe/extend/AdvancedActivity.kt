package de.datlag.openfe.extend

import android.content.Context
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.viewmodel.GitHubViewModel
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
abstract class AdvancedActivity : AppCompatActivity {

    constructor() : super() { }
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId) { }

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
        } else {
            super.attachBaseContext(newBase)
        }
    }
}
