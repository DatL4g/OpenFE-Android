package de.datlag.openfe.fragments.actions

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import de.datlag.openfe.R
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.getThemedLayoutInflater
import de.datlag.openfe.commons.invisible
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.show
import de.datlag.openfe.databinding.FragmentBrowserActionBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.helper.NightModeHelper.NightMode
import de.datlag.openfe.helper.NightModeHelper.NightModeUtil
import de.datlag.openfe.interfaces.FragmentBackPressed
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
class BrowserFragment : AdvancedFragment(), FragmentBackPressed {

    private val args: BrowserFragmentArgs by navArgs()
    private lateinit var nightModeHelper: NightModeUtil

    private lateinit var binding: FragmentBrowserActionBinding

    private val navigationListener = View.OnClickListener {
        findNavController().navigate(R.id.action_BrowserActionFragment_to_OverviewFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowserActionBinding.inflate(getThemedLayoutInflater(inflater), container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        updateToggle(false, getColor(R.color.defaultNavigationColor), navigationListener)
        nightModeHelper = NightModeUtil(safeContext, activity)

        browserWebview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                browserWebview.invisible()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                browserWebview.show()
            }
        }

        browserWebview.loadUrl(args.url)
        browserWebview.settings.javaScriptEnabled = true

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) && nightModeHelper.getMode() == NightMode.DARK) {
            WebSettingsCompat.setForceDark(browserWebview.settings, WebSettingsCompat.FORCE_DARK_ON)
        }
    }

    private fun setupMenuItemClickListener(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.browserActionOpenWithItem -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(args.url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ContextCompat.startActivity(safeContext, Intent.createChooser(intent, "Choose Browser"), null)
            }
        }
        return false
    }

    private fun initToolbar() {
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

    override fun onResume() {
        super.onResume()
        initToolbar()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initToolbar()
    }

    override fun onBackPressed(): Boolean = true
}
