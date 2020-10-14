package de.datlag.openfe.extend

import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.MainActivity
import de.datlag.openfe.commons.supportActionBar
import de.datlag.openfe.commons.toggle

@AndroidEntryPoint
abstract class AdvancedFragment : Fragment() {

    protected val toolbar: Toolbar?
        get() {
            return if (activity is MainActivity) {
                (activity as? MainActivity?)?.toolbar
            } else {
                null
            }
        }

    protected val searchView: SimpleSearchView?
        get() {
            return if (activity is MainActivity) {
                (activity as? MainActivity?)?.searchView
            } else {
                null
            }
        }

    protected val drawer: DrawerLayout?
        get() {
            return if (activity is MainActivity) {
                (activity as? MainActivity?)?.drawer
            } else {
                null
            }
        }

    protected val toggle: ActionBarDrawerToggle?
        get() {
            return if (activity is MainActivity) {
                (activity as? MainActivity?)?.toggle
            } else {
                null
            }
        }

    protected val toggleListener: View.OnClickListener
        get() {
            return (
                if (activity is MainActivity) {
                    (activity as? MainActivity?)?.toggleListener
                } else {
                    null
                }
                ) ?: View.OnClickListener { drawer?.toggle() }
        }

    protected fun updateToggle(useDrawer: Boolean, listener: View.OnClickListener = toggleListener) {
        supportActionBar?.setDisplayShowHomeEnabled(true)
        if (useDrawer) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            toolbar?.setNavigationOnClickListener(listener)
            toggle?.toolbarNavigationClickListener = listener
            toggle?.isDrawerIndicatorEnabled = true
            toggle?.syncState()
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            toolbar?.setNavigationOnClickListener(listener)
            toggle?.toolbarNavigationClickListener = listener
        }
    }
}
