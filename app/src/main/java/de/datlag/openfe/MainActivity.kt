package de.datlag.openfe

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.databinding.ActivityMainBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import de.datlag.openfe.viewmodel.GitHubViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.serialization.ExperimentalSerializationApi
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@Obfuscate
@AndroidEntryPoint
class MainActivity : AdvancedActivity(R.layout.activity_main) {

    private val binding: ActivityMainBinding by viewBinding(R.id.container)
    private val gitHubViewModel: GitHubViewModel by viewModels()

    val toolbar: Toolbar
        get() = binding.toolBar

    val searchView: SimpleSearchView
        get() = binding.searchview

    val bottomAppBar: BottomAppBar
        get() = binding.bottomAppBar

    val bottomNavigation: BottomNavigationView
        get() = binding.bottomNavigation

    val fab: FloatingActionButton
        get() = binding.fab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() = with(binding) {
        setSupportActionBar(toolBar)
    }

    private fun getCurrentNavFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        val fragmentList = navHostFragment?.childFragmentManager?.fragments
        return if (!fragmentList.isNullOrEmpty() && fragmentList.size >= 1) fragmentList[0] else null
    }

    fun githubOAuth() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.Builder()
            .scheme("https")
            .authority("github.com")
            .appendPath("login")
            .appendPath("oauth")
            .appendPath("authorize")
            .appendQueryParameter("client_id", getString(R.string.github_secret_client_id))
            .appendQueryParameter("redirect_uri", getString(R.string.github_callback_uri))
            .appendQueryParameter("scope", "user:read")
            .build()
        startActivity(intent)
    }

    fun discoverUSBDevices() {
        val usbManager = this.getSystemService(Context.USB_SERVICE) as UsbManager
        val massStorageDevices = UsbMassStorageDevice.getMassStorageDevices(this)

        if (massStorageDevices.isEmpty()) {
            Timber.e("no device")
            return
        }

        val usbDevice = intent?.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as? UsbDevice?

        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
            Timber.e("permission")

            for (device in massStorageDevices) {
                device.init()

                for (partition in device.partitions) {
                    partition.fileSystem.also {
                        Toast.makeText(this, it.volumeLabel, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Timber.e("no permission")

            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent("${this.packageName}.USB_PERMISSION"),
                0
            )
            for (device in massStorageDevices) {
                Timber.e("requesting: ${device.usbDevice.deviceName}")
                usbManager.requestPermission(device.usbDevice, permissionIntent)
            }
        }
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        newIntent?.let {
            it.data?.let { data ->
                if (data.toString().startsWith(getString(R.string.github_callback_uri))) {
                    val code = data.getQueryParameter("code")
                    gitHubViewModel.requestAccessTokenAndLogin(code)
                }
            }
        }
    }

    override fun onBackPressed() {
        val pressed: Boolean = (getCurrentNavFragment() as? FragmentBackPressed?)?.onBackPressed() ?: true

        if (pressed) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return (getCurrentNavFragment() as? FragmentOptionsMenu?)?.onCreateMenu(menu, menuInflater) ?: true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.e("activity called")

        when (requestCode) {
            1337 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.e("activity granted")
                } else {
                    Timber.e("activity not granted")
                }
            }
        }
    }
}
