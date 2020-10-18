package de.datlag.openfe.helper

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object NightModeHelper {
    class Theme(activity: Activity) {

        init {
            val currentMode = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
            if (sUiNightMode == Configuration.UI_MODE_NIGHT_UNDEFINED) {
                sUiNightMode = currentMode
            }
        }

        fun getConfigMode(): Int = sUiNightMode

        companion object {
            private var sUiNightMode = Configuration.UI_MODE_NIGHT_UNDEFINED
        }
    }

    class NightModeUtil(private val context: Context, activity: Activity? = null) {
        private val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        private var theme: Theme? = if (activity == null) null else Theme(activity)

        fun getMode(): NightMode {
            val delegateMode = getDelegateMode()
            val uiMode = getUiMode()

            return if (delegateMode == NightMode.LIGHT || uiMode == NightMode.LIGHT) {
                NightMode.LIGHT
            } else if (delegateMode == NightMode.DARK || uiMode == NightMode.DARK) {
                NightMode.DARK
            } else {
                NightMode.SYSTEM
            }
        }

        fun getUiMode(): NightMode {
            return if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_NO || theme?.getConfigMode() == Configuration.UI_MODE_NIGHT_NO) {
                NightMode.LIGHT
            } else if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES || theme?.getConfigMode() == Configuration.UI_MODE_NIGHT_YES) {
                NightMode.DARK
            } else {
                NightMode.SYSTEM
            }
        }

        fun getDelegateMode(): NightMode {
            return when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_NO -> NightMode.LIGHT
                AppCompatDelegate.MODE_NIGHT_YES -> NightMode.DARK
                else -> NightMode.SYSTEM
            }
        }
    }

    enum class NightMode(val type: Int) {
        LIGHT(0),
        DARK(1),
        SYSTEM(2)
    }
}
