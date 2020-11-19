package de.datlag.openfe.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.iterator
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.review.ReviewManagerFactory
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.MainActivity
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
import de.datlag.openfe.filter.MimeTypeFilter
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.recycler.adapter.ActionRecyclerAdapter
import de.datlag.openfe.recycler.adapter.LocationRecyclerAdapter
import de.datlag.openfe.recycler.data.ActionItem
import de.datlag.openfe.recycler.data.LocationItem
import de.datlag.openfe.safeargs.StorageArgs
import de.datlag.openfe.util.PermissionChecker
import de.datlag.openfe.viewmodel.GitHubViewModel
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
class OverviewFragment : AdvancedFragment(R.layout.fragment_overview), FragmentBackPressed {

    lateinit var locationList: List<LocationItem>
    lateinit var actionList: List<ActionItem>

    private val binding: FragmentOverviewBinding by viewBinding()
    private val githubViewModel: GitHubViewModel by activityViewModels()

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

        githubViewModel.isNoAdsPermitted.observe(viewLifecycleOwner) { permitted ->
            if (githubViewModel.reposContributorListLoaded && githubViewModel.authenticatedUserLoaded) {
                onNoAdPermissionChanged(permitted)
            }
        }
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
                checkReadPermission(position, null)
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

        if (activity is MainActivity) {
            (activity as MainActivity).discoverUSBDevices()
        }

        return locationList.toList()
    }

    private fun getActionItems(): List<ActionItem> {
        val actionList = mutableListOf<ActionItem>()
        val iconTint = getColor(R.color.coloredCardHighlight)

        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_music_note_24dp, iconTint),
                "Music"
            ) {
                checkReadPermission(0, MimeTypeFilter(acceptAudio = true))
            }
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_image_24dp, iconTint),
                "Images"
            ) {
                checkReadPermission(0, MimeTypeFilter(acceptImage = true))
            }
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_local_movies_24dp, iconTint),
                "Videos"
            ) {
                checkReadPermission(0, MimeTypeFilter(acceptVideo = true))
            }
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_insert_drive_file_24dp, iconTint),
                "Documents"
            ) {
                checkReadPermission(0, MimeTypeFilter(acceptDocument = true))
            }
        )
        actionList.add(
            ActionItem(
                getDrawable(R.drawable.ic_archive_24dp, iconTint),
                "Archives"
            ) {
                checkReadPermission(0, MimeTypeFilter(acceptArchive = true))
            }
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
        actionList.add(
            ActionItem(
                null,
                "Review",
                reviewUnit()
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

    private fun reviewUnit(): () -> Unit = {
        val manager = ReviewManagerFactory.create(safeContext)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener {
            if (it.isSuccessful) {
                val reviewInfo = it.result
                manager.launchReviewFlow(activity, reviewInfo)
            } else {
                Timber.e(it.exception)
            }
        }
    }

    private fun checkReadPermission(position: Int, filter: MimeTypeFilter? = null) {
        PermissionChecker.checkReadStorage(
            safeContext,
            object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val action =
                        OverviewFragmentDirections.actionOverviewFragmentToExplorerFragment(
                            StorageArgs(locationList, position, filter)
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

    fun onNoAdPermissionChanged(permitted: Boolean) = with(binding) {
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
