package de.datlag.openfe.fragments.actions

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.iterator
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.AppsActionInfoSheet
import de.datlag.openfe.bottomsheets.ConfirmActionSheet
import de.datlag.openfe.bottomsheets.FileProgressSheet
import de.datlag.openfe.commons.androidGreaterOr
import de.datlag.openfe.commons.copyTo
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.getDrawable
import de.datlag.openfe.commons.hide
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.mutableCopyOf
import de.datlag.openfe.commons.permissions
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.show
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.commons.statusBarColor
import de.datlag.openfe.commons.supportActionBar
import de.datlag.openfe.databinding.FragmentAppsActionBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.factory.AppsActionViewModelFactory
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.other.AppsSortType
import de.datlag.openfe.recycler.adapter.AppsActionRecyclerAdapter
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.util.PermissionChecker
import de.datlag.openfe.viewmodel.AppsActionViewModel
import de.datlag.openfe.viewmodel.AppsViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import timber.log.Timber
import java.io.File
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalSerializationApi
@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
class AppsFragment : AdvancedFragment(R.layout.fragment_apps_action), FragmentBackPressed, PopupMenu.OnMenuItemClickListener {

    private val args: AppsFragmentArgs by navArgs()
    private val appsViewModel: AppsViewModel by activityViewModels()
    private val viewModel: AppsActionViewModel by viewModels { AppsActionViewModelFactory(args) }
    private val binding: FragmentAppsActionBinding by viewBinding()

    private var copiedList = listOf<AppItem>()
    private lateinit var adapter: AppsActionRecyclerAdapter

    private val navigationListener = View.OnClickListener {
        if (onBackPressedCheck()) {
            findNavController().navigate(R.id.action_AppsActionFragment_to_OverviewFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        updateToggle(getColor(R.color.defaultNavigationColor), navigationListener)
        updateToolbar()

        initRecycler()
        initEditText()
        initBottomNavigation()
        loadApps()

        appsViewModel.apps.observe(viewLifecycleOwner) { list ->
            loadApps(list)
        }
    }

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.defaultStatusBarColor))
    }

    private fun loadApps(list: List<AppItem> = appsViewModel.apps.value ?: listOf()) = with(binding) {
        if (list.isNotEmpty()) {
            adapter.submitList(list)
            copiedList = list.mutableCopyOf()
            loadingTextView.hide()
            appsActionRecycler.show()
            appsActionLayoutWrapper.show()
        }
    }

    override fun initToolbar() {
        toolbar?.menu?.clear()
        toolbar?.inflateMenu(R.menu.apps_action_toolbar_menu)
        toolbar?.menu?.let {
            searchView?.setMenuItem(it.findItem(R.id.appsActionSearchItem))
            for (item in it.iterator()) {
                if (item.itemId != R.id.appsActionSearchItem) {
                    item.setOnMenuItemClickListener { menuItem ->
                        return@setOnMenuItemClickListener setupMenuItemClickListener(menuItem)
                    }
                }
            }
        }
    }

    private fun updateToolbar() {
        if (itemValid()) {
            supportActionBar?.title = viewModel.selectedApp!!.name
            supportActionBar?.setHomeAsUpIndicator(
                getDrawable(R.drawable.ic_close_24dp, getColor(R.color.defaultNavigationColor))
            )
        } else {
            supportActionBar?.title = safeContext.getString(R.string.app_name)
            supportActionBar?.setHomeAsUpIndicator(
                getDrawable(R.drawable.ic_arrow_back_24dp, getColor(R.color.defaultNavigationColor))
            )
        }
    }

    @ExperimentalContracts
    private fun initRecycler() = with(binding) {
        appsActionRecycler.layoutManager = GridLayoutManager(
            safeContext,
            if (safeContext.packageManager.isTelevision()) 5 else 3
        )
        adapter = AppsActionRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                viewModel.selectedApp = copiedList[position]
                appsActionLayoutWrapper.requestLayout()
                updateToolbar()
                updateBottom(true)
                updateFAB(false)
            }
        }
        adapter.submitList(listOf())
        appsActionRecycler.adapter = adapter
    }

    @ExperimentalContracts
    private fun initEditText() {
        searchView?.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.submitList(listOf())
                if (newText.isNotCleared()) {
                    val newListCopy = copiedList.mutableCopyOf()

                    lifecycleScope.launch(Dispatchers.IO) {
                        val iterator = newListCopy.iterator()
                        while (iterator.hasNext()) {
                            val nextItem = iterator.next()
                            if (!nextItem.name.contains(
                                    newText,
                                    true
                                ) && !nextItem.packageName.contains(
                                        newText,
                                        true
                                    )
                            ) {
                                iterator.remove()
                                continue
                            }
                        }
                        withContext(Dispatchers.Main) {
                            adapter.submitList(newListCopy)
                        }
                    }
                } else {
                    adapter.submitList(copiedList)
                }
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                adapter.submitList(copiedList)
                return false
            }
        })
    }

    private fun initBottomNavigation() {
        bottomNavigation?.menu?.clear()
        bottomNavigation?.inflateMenu(R.menu.apps_action_bottom_menu)
        updateBottom(false)
        updateFAB(false)

        bottomNavigation?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.appsActionBottomUninstallApp -> {
                    requestUninstall()
                    true
                }
                R.id.appsActionBottomLaunchApp -> {
                    requestLaunch()
                    true
                }
                R.id.appsActionBottomBackupApp -> {
                    requestBackup()
                    true
                }
                R.id.appsActionBottomInfoApp -> {
                    requestInfo()
                    true
                }
                else -> false
            }
        }
    }

    @ExperimentalContracts
    private fun requestUninstall() {
        if (itemValid()) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:${viewModel.selectedApp!!.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            safeContext.startActivity(intent)
        }
    }

    @ExperimentalContracts
    private fun requestLaunch() {
        if (itemValid()) {
            startActivity(safeContext.packageManager.getLaunchIntentForPackage(viewModel.selectedApp!!.packageName))
        }
    }

    @ExperimentalContracts
    private fun requestBackup() {
        if (itemValid()) {
            checkWritePermission { writeable ->
                if (writeable && checkManageFilePermission()) {
                    backupConfirmDialog(viewModel.selectedApp!!)
                }
            }
        }
    }

    private fun backupConfirmDialog(item: AppItem) {
        val confirmActionSheet = ConfirmActionSheet.backupInstance(item.name)
        confirmActionSheet.setRightButtonClickListener {
            backupProgressDialog(item)
        }
        showBottomSheetFragment(confirmActionSheet)
    }

    private fun backupProgressDialog(item: AppItem) {
        val fileProgressSheet = FileProgressSheet.newInstance()
        val originalFile = File(item.publicSourceDir)
        val fileName = "${item.name}-Backup"
        val storage =
            File("${viewModel.storageFile.absolutePath}${File.separator}OpenFE${File.separator}Apps")
        val createFolderSuccess = if (!storage.exists()) {
            storage.mkdirs()
        } else {
            true
        }

        fileProgressSheet.title = "Backup ${item.name}"
        fileProgressSheet.text = "Creating Backup file in OpenFE/Apps/${item.name}-Backup..."
        fileProgressSheet.leftButtonText = "Cancel"
        fileProgressSheet.closeOnLeftClick = true
        fileProgressSheet.closeOnRightClick = true
        fileProgressSheet.setOnCancelListener {
            Timber.e("cancelled")
        }
        fileProgressSheet.updateable = {
            if (createFolderSuccess) {
                val backupFile = File(storage, "$fileName-${originalFile.name}")
                val createFileSuccess = backupFile.createNewFile()

                if (createFileSuccess) {
                    originalFile.copyTo(backupFile, true) {
                        fileProgressSheet.updateProgressList(floatArrayOf(it))

                        if (it == 100F) {
                            fileProgressSheet.leftButtonText = String()
                            fileProgressSheet.rightButtonText = "Done"
                        }
                    }
                }
            }
        }

        showBottomSheetFragment(fileProgressSheet)
    }

    private fun checkManageFilePermission(): Boolean {
        return if (androidGreaterOr(Build.VERSION_CODES.R)) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.fromParts("package", safeContext.packageName, null)
                    safeContext.startActivity(intent)
                } catch (ignored: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
            }

            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    @ExperimentalContracts
    private fun requestInfo() {
        if (itemValid()) {
            showBottomSheetFragment(AppsActionInfoSheet.newInstance(viewModel.selectedApp!!))
        }
    }

    @ExperimentalContracts
    private fun itemValid(item: AppItem? = viewModel.selectedApp): Boolean {
        contract {
            returns(true) implies (item != null)
        }

        return item != null
    }

    private fun checkWritePermission(granted: (writeable: Boolean) -> Unit) {
        PermissionChecker.checkWriteStorage(
            safeContext,
            object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    granted.invoke(viewModel.storageFile.permissions.writeable)
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

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {}
            }
        )
    }

    private fun showPopupMenu(anchor: View) = with(appsViewModel) {
        val popupMenu = PopupMenu(safeContext, anchor)
        popupMenu.menuInflater.inflate(R.menu.apps_action_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(this@AppsFragment)
        popupMenu.show()
    }

    private fun setupMenuItemClickListener(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.appsActionFilterItem -> {
                showPopupMenu(item.actionView ?: requireView().rootView.findViewById(item.itemId))
            }
        }
        return false
    }

    private fun onBackPressedCheck(): Boolean {
        searchView?.onBackPressed()
        return if (itemValid()) {
            viewModel.selectedApp = null
            updateToolbar()
            updateBottom(false)
            updateFAB(false)
            false
        } else {
            true
        }
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        p0?.let {
            when (it.itemId) {
                R.id.appsActionPopupFilterName -> appsViewModel.sortType = AppsSortType.NAME
                R.id.appsActionPopupFilterInstalled ->
                    appsViewModel.sortType =
                        AppsSortType.INSTALLED
                R.id.appsActionPopupFilterUpdated -> appsViewModel.sortType = AppsSortType.UPDATED
                else -> appsViewModel.sortType = AppsSortType.NAME
            }
        }
        return false
    }

    override fun onBackPressed(): Boolean {
        return onBackPressedCheck()
    }

    companion object {
        fun newInstance() = AppsFragment()
    }
}
