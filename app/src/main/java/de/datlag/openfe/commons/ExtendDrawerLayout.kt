@file:Obfuscate
package de.datlag.openfe.commons

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import io.michaelrocks.paranoid.Obfuscate

fun DrawerLayout.toggle(gravity: Int = GravityCompat.START) {
    if (this.isDrawerOpen(gravity)) {
        this.closeDrawer(gravity)
    } else {
        this.openDrawer(gravity)
    }
}
