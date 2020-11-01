package de.datlag.openfe.fragments

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.iterator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.getDisplayName
import de.datlag.openfe.commons.getDrawable
import de.datlag.openfe.commons.getStorageVolumes
import de.datlag.openfe.commons.hide
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.show
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.commons.statusBarColor
import de.datlag.openfe.databinding.FragmentOverviewBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentNoAdPermission
import de.datlag.openfe.safeargs.StorageArgs
import de.datlag.openfe.recycler.adapter.ActionRecyclerAdapter
import de.datlag.openfe.recycler.adapter.LocationRecyclerAdapter
import de.datlag.openfe.recycler.data.ActionItem
import de.datlag.openfe.recycler.data.LocationItem
import de.datlag.openfe.util.PermissionChecker
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts

@ExperimentalSerializationApi
@ExperimentalContracts
@Obfuscate
@AndroidEntryPoint
class OverviewFragment : AdvancedFragment(R.layout.fragment_overview), FragmentBackPressed, FragmentNoAdPermission {

    lateinit var locationList: List<LocationItem>
    lateinit var actionList: List<ActionItem>

    private val binding: FragmentOverviewBinding by viewBinding()

    private var toolbarMenuJob: Job? = null

    private val navigationListener = View.OnClickListener {
        (activity)?.finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(safeContext)

        locationList = getLocationItems()
        actionList = getActionItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateToggle(getColor(R.color.defaultNavigationColor), navigationListener)
        updateBottom(false)
        updateFAB(false)

        toolbarMenuJob = lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main) {
                initToolbar()
            }
        }

        initLocationRecycler()
        initActionRecycler()
    }

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.defaultStatusBarColor))
    }

    override fun initToolbar() {
        toolbar?.let {
            it.menu.clear()
            it.inflateMenu(R.menu.overview_toolbar_menu)
            it.menu.also { menu ->
                for (item in menu.iterator()) {
                    item.setOnMenuItemClickListener { menuItem ->
                        return@setOnMenuItemClickListener setupMenuItemClickListener(menuItem)
                    }
                }
            }
        }
    }

    private fun initLocationRecycler() = with(binding) {
        locationRecycler.isNestedScrollingEnabled = false
        locationRecycler.layoutManager = LinearLayoutManager(safeContext)
        locationRecycler.adapter = LocationRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                checkReadPermission(position)
            }
            submitList(locationList)
        }
    }

    private fun initActionRecycler() = with(binding) {
        actionRecycler.isNestedScrollingEnabled = false
        actionRecycler.layoutManager =
            GridLayoutManager(safeContext, if (safeContext.packageManager.isTelevision()) 5 else 3)

        actionRecycler.adapter = ActionRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                actionList[position].action.invoke()
            }
            submitList(actionList)
        }
    }

    private fun setupMenuItemClickListener(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.overviewSettingsItem -> {
                findNavController().navigate(OverviewFragmentDirections.actionOverviewFragmentToSettingsFragment())
            }
        }
        return false
    }

    private fun getLocationItems(): List<LocationItem> {
        val usageStats = safeContext.getStorageVolumes()
        val locationList = mutableListOf<LocationItem>()

        for (usageStat in usageStats) {
            if (usageStat.max != 0L && usageStat.current != 0L) {
                locationList.add(
                    LocationItem(
                        usageStat.file.getDisplayName(safeContext),
                        usageStat
                    )
                )
            }
        }

        discoverUSBDevices()

        return locationList.toList()
    }

    private fun discoverUSBDevices() {
        val usbManager = safeContext.getSystemService(Context.USB_SERVICE) as UsbManager
        val massStorageDevices = UsbMassStorageDevice.getMassStorageDevices(safeContext)

        if (massStorageDevices.isEmpty()) {
            Timber.e("no device")
            return
        }

        val usbDevice =
            activity?.intent?.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as? UsbDevice?

        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
            Timber.e("permission")

            for (device in massStorageDevices) {
                device.init()

                for (partition in device.partitions) {
                    partition.fileSystem.also {
                        Toast.makeText(safeContext, it.volumeLabel, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Timber.e("no permission")

            val permissionIntent = PendingIntent.getBroadcast(
                safeContext,
                1337,
                Intent("${safeContext.packageName}.USB_PERMISSION"),
                0
            )
            for (device in massStorageDevices) {
                usbManager.requestPermission(device.usbDevice, permissionIntent)
            }
        }
    }

    private fun getActionItems(): List<ActionItem> {
        val actionList = mutableListOf<ActionItem>()
        val iconTint = getColor(R.color.coloredCardHighlight)

        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_music_note_24dp, iconTint),
                "Music", appFragmentUnit()
            )
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_image_24dp, iconTint),
                "Images",
                appFragmentUnit()
            )
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_local_movies_24dp, iconTint),
                "Videos", appFragmentUnit()
            )
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_insert_drive_file_24dp, iconTint),
                "Documents", appFragmentUnit()
            )
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_archive_24dp, iconTint),
                "Archives",
                appFragmentUnit()
            )
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_adb_24dp, iconTint),
                "Apps",
                appFragmentUnit()
            )
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_baseline_delete_24, iconTint),
                "Clean",
                cleanFragmentUnit()
            )
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_github, iconTint),
                "GitHub",
                githubUnit()
            )
        )

        return actionList
    }

    private fun appFragmentUnit(): () -> Unit = {
        findNavController().navigate(
            OverviewFragmentDirections.actionOverviewFragmentToAppsActionFragment(
                locationList[0].usage.file.absolutePath
            )
        )
    }

    private fun cleanFragmentUnit(): () -> Unit = {
        findNavController().navigate(
            OverviewFragmentDirections.actionOverviewFragmentToCleanActionFragment()
        )
    }

    private fun githubUnit(): () -> Unit = {
        findNavController().navigate(
            OverviewFragmentDirections.actionOverviewFragmentToBrowserActionFragment(
                getString(R.string.github_repo_url)
            )
        )
    }

    private fun checkReadPermission(position: Int) {
        PermissionChecker.checkReadStorage(
            safeContext,
            object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val action =
                        OverviewFragmentDirections.actionOverviewFragmentToExplorerFragment(
                            StorageArgs(locationList, position)
                        )
                    findNavController().navigate(action)
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showBottomSheetFragment(
                        PermissionChecker.storagePermissionSheet(
                            safeContext,
                            p1
                        )
                    )
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                }
            }
        )
    }

    override fun onNoAdPermissionChanged(permitted: Boolean) = with(binding) {
        if (permitted) {
            adView.hide()
        } else {
            adView.show()
            adView.loadAd(AdRequest.Builder().build())
        }
    }

    override fun onBackPressed(): Boolean = true

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Timber.e("called")

        when (requestCode) {
            1337 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.e("granted")
                } else {
                    Timber.e("not granted")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        toolbarMenuJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        toolbarMenuJob?.cancel()
    }

    companion object {
        fun newInstance() = OverviewFragment()
    }
}
