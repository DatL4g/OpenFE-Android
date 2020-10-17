package de.datlag.openfe.enums

import android.content.Context
import de.datlag.openfe.R
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
enum class AppCategory(val associatedValue: Int) {

    UNDEFINED(-1),
    GAME(0),
    AUDIO(1),
    VIDEO(2),
    IMAGE(3),
    SOCIAL(4),
    NEWS(5),
    MAPS(6),
    PRODUCTIVITY(7);

    fun toString(context: Context): String {
        return when (this) {
            GAME -> context.getString(R.string.app_category_game)
            AUDIO -> context.getString(R.string.app_category_audio)
            VIDEO -> context.getString(R.string.app_category_video)
            IMAGE -> context.getString(R.string.app_category_image)
            SOCIAL -> context.getString(R.string.app_category_social)
            NEWS -> context.getString(R.string.app_category_news)
            MAPS -> context.getString(R.string.app_category_maps)
            PRODUCTIVITY -> context.getString(R.string.app_category_productivity)
            else -> context.getString(R.string.app_category_undefined)
        }
    }
}
