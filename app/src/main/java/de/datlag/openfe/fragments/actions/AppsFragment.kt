package de.datlag.openfe.fragments.actions

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import de.datlag.openfe.commons.androidGreaterOr
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.getDrawable
import de.datlag.openfe.commons.getPermissions
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.isTelevision
import de.datlag.openfe.commons.mutableCopyOf
import de.datlag.openfe.commons.saveContext
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.commons.statusBarColor
import de.datlag.openfe.commons.tint
import de.datlag.openfe.databinding.FragmentAppsActionBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.factory.AppsActionViewModelFactory
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import de.datlag.openfe.other.AppsSortType
import de.datlag.openfe.recycler.adapter.AppsActionRecyclerAdapter
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.util.PermissionChecker
import de.datlag.openfe.viewmodel.AppsActionViewModel
import de.datlag.openfe.viewmodel.AppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
@AndroidEntryPoint
class AppsFragment : Fragment(), FragmentOptionsMenu, FragmentBackPressed, PopupMenu.OnMenuItemClickListener {

    private val args: AppsFragmentArgs by navArgs()
    private val viewModel: AppsActionViewModel by viewModels { AppsActionViewModelFactory(args) }
    private val appsViewModel: AppsViewModel by viewModels()
    private lateinit var binding: FragmentAppsActionBinding

    private var copiedList = listOf<AppItem>()
    private lateinit var adapter: AppsActionRecyclerAdapter

    override fun onResume() {
        super.onResume()
        if (appsViewModel.apps.value.isNullOrEmpty()) {
            statusBarColor(getColor(R.color.appsActionLoadingStatusbarColor))
        } else {
            statusBarColor(getColor(R.color.appsActionStatusbarColor))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(saveContext, R.style.AppsActionFragmentTheme)
        val clonedLayoutInflater = inflater.cloneInContext(contextThemeWrapper)

        saveContext.theme.applyStyle(R.style.AppsActionFragmentTheme, true)
        binding = FragmentAppsActionBinding.inflate(clonedLayoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AdvancedActivity).setSupportActionBar(appsActionToolbar)
        (activity as AdvancedActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        updateToolbar()

        appsActionToolbar.setNavigationOnClickListener {
            if (onBackPressedCheck()) {
                findNavController().navigate(R.id.action_AppsActionFragment_to_OverviewFragment)
            }
        }

        initRecycler()
        initEditText()
        initBottomNavigation()
        loadAppsAsync()
    }

    @ExperimentalContracts
    private fun initRecycler() = with(binding) {
        appsActionRecycler.layoutManager = GridLayoutManager(
            saveContext,
            if (saveContext.packageManager.isTelevision()) 5 else 3
        )
        adapter = AppsActionRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                appsActionBottomNavigation.visibility = View.VISIBLE
                viewModel.selectedApp = copiedList[position]
                appsActionLayoutWrapper.requestLayout()
                updateToolbar()
            }
        }
        adapter.submitList(listOf())
        appsActionRecycler.adapter = adapter
        appsActionRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (dy >= 24) {
                        appsActionBottomNavigation.visibility = View.GONE
                    }
                } else {
                    if (itemValid()) {
                        appsActionBottomNavigation.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    @ExperimentalContracts
    private fun initEditText() = with(binding) {
        appsActionSearchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
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

    private fun initBottomNavigation() = with(binding) {
        appsActionBottomNavigation.setOnNavigationItemSelectedListener {
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

    private fun loadAppsAsync() = with(binding) {
        appsViewModel.apps.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                adapter.submitList(list)
                copiedList = list.mutableCopyOf()
                loadingTextView.visibility = View.GONE
                appBar.visibility = View.VISIBLE
                appsActionRecycler.visibility = View.VISIBLE
                appsActionLayoutWrapper.visibility = View.VISIBLE
                statusBarColor(getColor(R.color.appsActionStatusbarColor))
            }
        }
    }

    @ExperimentalContracts
    private fun requestUninstall() {
        if (itemValid()) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:${viewModel.selectedApp!!.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            saveContext.startActivity(intent)
        }
    }

    @ExperimentalContracts
    private fun requestLaunch() {
        if (itemValid()) {
            startActivity(saveContext.packageManager.getLaunchIntentForPackage(viewModel.selectedApp!!.packageName))
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
        val confirmActionSheet = ConfirmActionSheet.newInstance()

        confirmActionSheet.title = "Backup ${item.name}"
        confirmActionSheet.text = "The backup file is created in the folder OpenFE/Apps/${item.name}-Backup and can be installed and shared from there"
        confirmActionSheet.leftText = "Cancel"
        confirmActionSheet.rightText = "Backup"
        confirmActionSheet.closeOnLeftClick = true
        confirmActionSheet.closeOnRightClick = true
        confirmActionSheet.rightClickListener = {
            backupProgressDialog(item)
        }
        showBottomSheetFragment(confirmActionSheet)
    }

    private fun backupProgressDialog(item: AppItem) {
        /*
        val originalFile = File(viewModel.selectedApp!!.publicSourceDir)
                    val fileName = "${viewModel.selectedApp!!.name}-Backup"
                    val storage = File("${viewModel.storageFile.absolutePath}${File.separator}OpenFE${File.separator}Apps")
                    val createFolderSuccess = if (!storage.exists()) { storage.mkdirs() } else { true }

                    if (createFolderSuccess) {
                        val backupFile = File(storage, "$fileName-${originalFile.name}")
                        val createFileSuccess = backupFile.createNewFile()

                        if (createFileSuccess) {
                            val confirmSheet = ConfirmActionSheet.newInstance()
                            confirmSheet.title = "Copy file..."
                            confirmSheet.rightText = "Close"
                            confirmSheet.closeOnRightClick = true
                            showBottomSheetFragment(confirmSheet)
                            try {
                                originalFile.copyTo(backupFile, true) {
                                    confirmSheet.text = "Done: $it%"
                                }
                            } catch (ignored: Exception) {
                                return@checkWritePermission
                            }
                        }
                    }
         */
    }

    private fun checkManageFilePermission(): Boolean {
        return if (androidGreaterOr(Build.VERSION_CODES.R)) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.fromParts("package", saveContext.packageName, null)
                    saveContext.startActivity(intent)
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
            saveContext,
            object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    granted.invoke(viewModel.storageFile.getPermissions().second)
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showBottomSheetFragment(PermissionChecker.storagePermissionSheet(saveContext, p1))
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) { }
            }
        )
    }

    private fun showPopupMenu(anchor: View) = with(appsViewModel) {
        val popupMenu = PopupMenu(saveContext, anchor)
        popupMenu.menuInflater.inflate(R.menu.apps_action_popup_menu, popupMenu.menu)
        if (isAppsSortedByNameReversed) {
            popupMenu.menu.getItem(0).title = "Name (Reversed)"
        }
        if (isAppsSortedByInstalledReversed) {
            popupMenu.menu.getItem(1).title = "Name (Reversed)"
        }
        if (isAppsSortedByUpdatedReversed) {
            popupMenu.menu.getItem(2).title = "Name (Reversed)"
        }
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

    private fun onBackPressedCheck(): Boolean = with(binding) {
        return if (itemValid()) {
            viewModel.selectedApp = null
            appsActionBottomNavigation.visibility = View.GONE
            updateToolbar()
            false
        } else {
            true
        }
    }

    private fun updateToolbar() {
        if (itemValid()) {
            (activity as AdvancedActivity).supportActionBar?.title = viewModel.selectedApp!!.name
            (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(
                getDrawable(R.drawable.ic_close_24dp)?.apply {
                    tint(
                        getColor(
                            R.color.appsActionToolbarIconTint
                        )
                    )
                }
            )
        } else {
            (activity as AdvancedActivity).supportActionBar?.title = saveContext.getString(R.string.app_name)
            (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(
                getDrawable(R.drawable.ic_arrow_back_24dp)?.apply {
                    tint(
                        getColor(
                            R.color.appsActionToolbarIconTint
                        )
                    )
                }
            )
        }
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        p0?.let {
            when (it.itemId) {
                R.id.appsActionPopupFilterName -> appsViewModel.sortType = AppsSortType.NAME
                R.id.appsActionPopupFilterInstalled -> appsViewModel.sortType = AppsSortType.INSTALLED
                R.id.appsActionPopupFilterUpdated -> appsViewModel.sortType = AppsSortType.UPDATED
                else -> appsViewModel.sortType = AppsSortType.NAME
            }
        }
        return false
    }

    override fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean = with(binding) {
        inflater.inflate(R.menu.apps_action_toolbar_menu, menu)
        menu?.let {
            appsActionSearchView.setMenuItem(it.findItem(R.id.appsActionSearchItem))
            for (item in it.iterator()) {
                if (item.itemId != R.id.appsActionSearchItem) {
                    item.setOnMenuItemClickListener { menuItem ->
                        return@setOnMenuItemClickListener setupMenuItemClickListener(menuItem)
                    }
                }
            }
        }
        return true
    }

    override fun onBackPressed(): Boolean {
        return onBackPressedCheck()
    }

    companion object {
        fun newInstance() = AppsFragment()
    }
}
