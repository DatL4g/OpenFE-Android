package de.datlag.openfe.extend

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
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
import de.datlag.openfe.viewmodel.AppsViewModel
import de.datlag.openfe.viewmodel.GitHubViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
abstract class AdvancedFragment : Fragment {

    constructor() : super() { }

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId) { }

    @Inject lateinit var injectedContext: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(getThemedLayoutInflater(inflater), container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        initToolbar()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initToolbar()
    }

    protected val toolbar: Toolbar?
        get() {
            return if (activity is MainActivity) {
                (activity as MainActivity).toolbar
            } else {
                null
            }
        }

    protected val searchView: SimpleSearchView?
        get() {
            return if (activity is MainActivity) {
                (activity as MainActivity).searchView
            } else {
                null
            }
        }

    protected val bottomAppBar: BottomAppBar?
        get() {
            return if (activity is MainActivity) {
                (activity as MainActivity).bottomAppBar
            } else {
                null
            }
        }

    protected val bottomNavigation: BottomNavigationView?
        get() {
            return if (activity is MainActivity) {
                (activity as MainActivity).bottomNavigation
            } else {
                null
            }
        }

    protected val fab: FloatingActionButton?
        get() {
            return if (activity is MainActivity) {
                (activity as MainActivity).fab
            } else {
                null
            }
        }

    protected fun updateToggle(listener: View.OnClickListener) {
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationOnClickListener(listener)
    }

    protected fun updateToggle(@ColorInt color: Int, listener: View.OnClickListener) {
        updateToggle(listener)
        toolBar?.navigationIcon?.tint(color)?.let { toolBar.navigationIcon = it }
        supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(color) })
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

    protected val githubViewModel: GitHubViewModel?
        get() {
            return if (activity is AdvancedActivity) {
                (activity as AdvancedActivity).gitHubViewModel
            } else {
                null
            }
        }

    protected val appsViewModel: AppsViewModel?
        get() {
            return if (activity is MainActivity) {
                (activity as MainActivity).appsViewModel
            } else {
                null
            }
        }

    abstract fun initToolbar()
}
