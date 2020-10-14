package de.datlag.openfe.commons

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

fun DrawerLayout.toggle(gravity: Int = GravityCompat.START) {
    if (this.isDrawerOpen(gravity)) {
        this.closeDrawer(gravity)
    } else {
        this.openDrawer(gravity)
    }
}
