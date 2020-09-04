package de.datlag.openfe

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

@HiltAndroidApp
class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        applyFont()
    }

    private fun applyFont() {
        ViewPump.init(ViewPump.builder()
            .addInterceptor(
                CalligraphyInterceptor(
                    CalligraphyConfig.Builder()
                        .setDefaultFontPath(this.getString(R.string.font_path))
                        .setFontAttrId(R.attr.fontPath)
                        .build())
            )
            .build())
    }

}