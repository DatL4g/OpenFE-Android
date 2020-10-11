package de.datlag.openfe.fragments

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.datlag.openfe.R
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.getDisplayName
import de.datlag.openfe.commons.getDrawable
import de.datlag.openfe.commons.getStorageVolumes
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.commons.statusBarColor
import de.datlag.openfe.data.ExplorerFragmentStorageArgs
import de.datlag.openfe.databinding.FragmentOverviewBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.recycler.adapter.ActionRecyclerAdapter
import de.datlag.openfe.recycler.adapter.LocationRecyclerAdapter
import de.datlag.openfe.recycler.data.ActionItem
import de.datlag.openfe.recycler.data.LocationItem
import de.datlag.openfe.util.PermissionChecker
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class OverviewFragment : Fragment(), FragmentBackPressed {

    lateinit var locationList: List<LocationItem>
    lateinit var actionList: List<ActionItem>

    private lateinit var binding: FragmentOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationList = getLocationItems()
        actionList = getActionItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(safeContext, R.style.OverviewFragmentTheme)
        val clonedLayoutInflater = inflater.cloneInContext(contextThemeWrapper)

        safeContext.theme.applyStyle(R.style.OverviewFragmentTheme, true)
        binding = FragmentOverviewBinding.inflate(clonedLayoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AdvancedActivity).setSupportActionBar(toolBar)
        val toggle = ActionBarDrawerToggle(requireActivity(), drawer, toolBar, R.string.app_name, R.string.app_name)
        toggle.drawerArrowDrawable.color = getColor(R.color.overviewDrawerToggleColor)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        locationRecycler.isNestedScrollingEnabled = false
        locationRecycler.layoutManager = LinearLayoutManager(safeContext)
        locationRecycler.adapter = LocationRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                checkReadPermission(position)
            }
            submitList(locationList)
        }

        actionRecycler.isNestedScrollingEnabled = false
        actionRecycler.layoutManager = GridLayoutManager(safeContext, if (safeContext.packageManager.isTelevision()) 5 else 3)
        actionRecycler.adapter = ActionRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                when (actionList[position].actionId) {
                    R.id.action_OverviewFragment_to_AppsActionFragment -> {
                        val action = OverviewFragmentDirections.actionOverviewFragmentToAppsActionFragment(locationList[0].usage.file.absolutePath)
                        findNavController().navigate(action)
                    }
                    else -> {
                        // findNavController().navigate(actionList[position].actionId)
                    }
                }
            }
            submitList(actionList)
        }
    }

    private fun getLocationItems(): List<LocationItem> {
        val usageStats = safeContext.getStorageVolumes()
        val locationList = mutableListOf<LocationItem>()

        for (usageStat in usageStats) {
            if (usageStat.max != 0L && usageStat.current != 0L) {
                locationList.add(LocationItem(usageStat.file.getDisplayName(safeContext), usageStat))
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

        val usbDevice = activity?.intent?.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as? UsbDevice?

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

        actionList.add(ActionItem(getDrawable(R.drawable.ic_music_note_24dp), "Music", 0))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_image_24dp), "Images", 1))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_local_movies_24dp), "Videos", 2))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_insert_drive_file_24dp), "Documents", 3))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_archive_24dp), "Archives", 4))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_adb_24dp), "Apps", R.id.action_OverviewFragment_to_AppsActionFragment))

        return actionList
    }

    private fun checkReadPermission(position: Int) {
        PermissionChecker.checkReadStorage(
            safeContext,
            object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val action = OverviewFragmentDirections.actionOverviewFragmentToExplorerFragment(
                        ExplorerFragmentStorageArgs(locationList, position)
                    )
                    findNavController().navigate(action)
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showBottomSheetFragment(PermissionChecker.storagePermissionSheet(safeContext, p1))
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.overviewStatusbarColor))
    }

    override fun onBackPressed(): Boolean = with(binding) {
        return if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            false
        } else {
            true
        }
    }

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

    companion object {
        fun newInstance() = OverviewFragment()
    }
}
