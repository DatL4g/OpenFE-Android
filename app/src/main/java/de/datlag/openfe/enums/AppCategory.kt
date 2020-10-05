package de.datlag.openfe.enums

import android.content.Context

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
            GAME -> "Game"
            AUDIO -> "Audio"
            VIDEO -> "Video"
            IMAGE -> "Image"
            SOCIAL -> "Social"
            NEWS -> "News"
            MAPS -> "Maps"
            PRODUCTIVITY -> "Productivity"
            else -> "(undefined)"
        }
    }
}
