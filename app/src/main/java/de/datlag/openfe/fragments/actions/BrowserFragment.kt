package de.datlag.openfe.fragments.actions

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.ConfirmActionSheet
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.invisible
import de.datlag.openfe.commons.isNetworkAvailable
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.show
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.databinding.FragmentBrowserActionBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.helper.NightModeHelper.NightMode
import de.datlag.openfe.helper.NightModeHelper.NightModeUtil
import de.datlag.openfe.interfaces.FragmentBackPressed
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@Obfuscate
@AndroidEntryPoint
@SuppressLint("SetJavaScriptEnabled")
class BrowserFragment : AdvancedFragment(R.layout.fragment_browser_action), FragmentBackPressed {

    private val args: BrowserFragmentArgs by navArgs()
    private lateinit var nightModeHelper: NightModeUtil

    private val binding: FragmentBrowserActionBinding by viewBinding()

    private val navigationListener = View.OnClickListener {
        findNavController().navigate(R.id.action_BrowserActionFragment_to_OverviewFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        updateToggle(getColor(R.color.defaultNavigationColor), navigationListener)
        nightModeHelper = NightModeUtil(safeContext, activity)

        browserWebview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                browserWebview.invisible()
                browserSwipeRefresh.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                browserWebview.show()
                browserSwipeRefresh.isRefreshing = false
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                errorDialog()
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                errorDialog()
            }
        }

        browserSwipeRefresh.setOnRefreshListener {
            loadSite()
        }

        loadSite()

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) && nightModeHelper.getMode() == NightMode.DARK) {
            WebSettingsCompat.setForceDark(browserWebview.settings, WebSettingsCompat.FORCE_DARK_ON)
        }
    }

    private fun loadSite() = with(binding) {
        if (safeContext.isNetworkAvailable()) {
            browserWebview.loadUrl(args.url)
            browserWebview.settings.javaScriptEnabled = true
        } else {
            errorDialog()
        }
    }

    override fun initToolbar() {
        toolbar?.menu?.clear()
        toolbar?.inflateMenu(R.menu.browser_action_toolbar_menu)
        toolbar?.menu?.let {
            for (item in it.iterator()) {
                item.setOnMenuItemClickListener { menuItem ->
                    return@setOnMenuItemClickListener setupMenuItemClickListener(menuItem)
                }
            }
        }
    }

    private fun errorDialog() {
        val confirmSheet = ConfirmActionSheet.newInstance()
        confirmSheet.title = "Error while loading"
        confirmSheet.text = "There was an error while loading. The page is not displayed at all or may be displayed incorrectly. Do you want to open the page in a different browser?"
        confirmSheet.leftButtonText = "Close"
        confirmSheet.rightButtonText = "Browser"
        confirmSheet.closeOnLeftClick = true
        confirmSheet.closeOnRightClick = true
        confirmSheet.setRightButtonClickListener {
            openInBrowser()
        }
        showBottomSheetFragment(confirmSheet)
    }

    private fun setupMenuItemClickListener(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.browserActionOpenWithItem -> {
                openInBrowser()
            }
        }
        return false
    }

    private fun openInBrowser() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(args.url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(
            safeContext,
            Intent.createChooser(intent, "Choose Browser"),
            null
        )
    }

    override fun onBackPressed(): Boolean = true
}
