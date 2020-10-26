package de.datlag.openfe.extend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.MainActivity
import de.datlag.openfe.R
import de.datlag.openfe.commons.getDrawable
import de.datlag.openfe.commons.getThemedLayoutInflater
import de.datlag.openfe.commons.hide
import de.datlag.openfe.commons.invisible
import de.datlag.openfe.commons.show
import de.datlag.openfe.commons.supportActionBar
import de.datlag.openfe.commons.tint
import de.datlag.openfe.commons.toggle
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
abstract class AdvancedFragment : Fragment {

    constructor() : super() { }

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId) { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(getThemedLayoutInflater(inflater), container, savedInstanceState)
    }

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

    protected val bottomAppBar: BottomAppBar?
        get() {
            return if (activity is MainActivity) {
                (activity as? MainActivity?)?.bottomAppBar
            } else {
                null
            }
        }

    protected val bottomNavigation: BottomNavigationView?
        get() {
            return if (activity is MainActivity) {
                (activity as? MainActivity?)?.bottomNavigation
            } else {
                null
            }
        }

    protected val fab: FloatingActionButton?
        get() {
            return if (activity is MainActivity) {
                (activity as? MainActivity?)?.fab
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

    protected fun updateToggle(useDrawer: Boolean, @ColorInt color: Int, listener: View.OnClickListener = toggleListener) {
        updateToggle(useDrawer, listener)
        toggle?.drawerArrowDrawable?.color = color
        toolBar?.navigationIcon?.tint(color)?.let { toolBar.navigationIcon = it }
        if (!useDrawer) {
            supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(color) })
        }
    }

    protected fun updateBottom(showBar: Boolean) {
        if (showBar) {
            bottomNavigation?.show()
            bottomAppBar?.show()
            bottomAppBar?.performShow()
        } else {
            bottomNavigation?.hide()
            bottomAppBar?.performHide()
            bottomAppBar?.invisible()
        }
    }

    protected fun updateFAB(show: Boolean) {
        if (show) {
            fab?.show()
            fab?.let { (it.behavior as? HideBottomViewOnScrollBehavior?)?.slideUp(it) }
        } else {
            fab?.let { (it.behavior as? HideBottomViewOnScrollBehavior?)?.slideDown(it) }
            fab?.hide()
        }
    }
}
