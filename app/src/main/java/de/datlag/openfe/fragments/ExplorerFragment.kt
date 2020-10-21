package de.datlag.openfe.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.ConfirmActionSheet
import de.datlag.openfe.bottomsheets.FileProgressSheet
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.getDrawable
import de.datlag.openfe.commons.intentChooser
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.parentDir
import de.datlag.openfe.commons.permissions
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.commons.statusBarColor
import de.datlag.openfe.commons.supportActionBar
import de.datlag.openfe.databinding.FragmentExplorerBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.factory.ExplorerViewModelFactory
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.recycler.LinearLayoutManagerWrapper
import de.datlag.openfe.recycler.adapter.ExplorerRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.viewmodel.AppsViewModel
import de.datlag.openfe.viewmodel.ExplorerViewModel
import io.michaelrocks.paranoid.Obfuscate
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
@Obfuscate
class ExplorerFragment : AdvancedFragment(R.layout.fragment_explorer), FragmentBackPressed {

    private val args: ExplorerFragmentArgs by navArgs()
    private val appsViewModel: AppsViewModel by viewModels()
    private val explorerViewModel: ExplorerViewModel by viewModels {
        ExplorerViewModelFactory(
            args,
            appsViewModel
        )
    }

    private lateinit var recyclerAdapter: ExplorerRecyclerAdapter
    private val binding: FragmentExplorerBinding by viewBinding()

    private val navigationListener = View.OnClickListener {
        if (onBackPressedCheck()) {
            findNavController().navigate(R.id.action_ExplorerFragment_to_OverviewFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        updateToggle(false, getColor(R.color.defaultNavigationColor), navigationListener)

        initBottomNavigation()
        initRecyclerView()
        initSearchView()

        explorerViewModel.currentDirectory.observe(viewLifecycleOwner) { dir ->
            if (explorerViewModel.searchShown.value == true) {
                updateFAB(false)
                bottomNavigation?.menu?.getItem(2)?.isVisible = false
            } else {
                val permission = dir.permissions.writeable
                updateFAB(permission)
                bottomNavigation?.menu?.getItem(2)?.isVisible = permission
            }
        }

        explorerViewModel.searchShown.observe(viewLifecycleOwner) { enabled ->
            if (enabled) {
                updateFAB(false)
                bottomNavigation?.menu?.getItem(2)?.isVisible = false
            } else {
                val permission = explorerViewModel.currentDirectory.value?.permissions?.writeable ?: false
                updateFAB(permission)
                bottomNavigation?.menu?.getItem(2)?.isVisible = permission
            }
        }

        explorerViewModel.currentSubDirectories.observe(viewLifecycleOwner) { list ->
            recyclerAdapter.submitList(list)
        }

        explorerViewModel.selectedItems.observe(viewLifecycleOwner) { list ->
            updateBottom(list.isNotEmpty())
            updateToolbar(list)
        }
    }

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.defaultStatusBarColor))
        initToolbar()
    }

    private fun initToolbar() {
        toolbar?.menu?.clear()
        toolbar?.inflateMenu(R.menu.explorer_toolbar_menu)
        toolbar?.menu?.let { searchView?.setMenuItem(it.findItem(R.id.explorerSearchItem)) }
    }

    private fun updateToolbar(list: List<ExplorerItem>? = explorerViewModel.selectedItems.value) {
        if (list.isNullOrEmpty()) {
            supportActionBar?.setHomeAsUpIndicator(
                getDrawable(
                    R.drawable.ic_arrow_back_24dp,
                    getColor(R.color.defaultNavigationColor)
                )
            )
            supportActionBar?.title = safeContext.getString(R.string.app_name)
        } else {
            supportActionBar?.setHomeAsUpIndicator(
                getDrawable(
                    R.drawable.ic_close_24dp,
                    getColor(R.color.defaultNavigationColor)
                )
            )
            supportActionBar?.title = "${list.size} Items"
        }
    }

    private fun initBottomNavigation() {
        bottomNavigation?.menu?.clear()
        bottomNavigation?.inflateMenu(R.menu.explorer_bottom_menu)
        bottomNavigation?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.explorerBottomDelete -> {
                    deleteAction()
                    true
                }
                R.id.explorerBottomExtendMenu -> {
                    moreAction(it.actionView ?: requireView().rootView.findViewById(it.itemId))
                    true
                }
                else -> false
            }
        }
        fab?.setImageDrawable(
            getDrawable(
                R.drawable.ic_baseline_add_24,
                getColor(R.color.defaultFabContentColor)
            )
        )

        updateBottom(false)
        updateFAB(true)
    }

    private fun initRecyclerView() = with(binding) {
        recyclerAdapter = ExplorerRecyclerAdapter(lifecycleScope)

        recyclerAdapter.setOnClickListener { _, position ->
            recyclerEvent(position, false)
        }

        recyclerAdapter.setOnLongClickListener { _, position ->
            recyclerEvent(position, true)
            true
        }

        explorerRecycler.layoutManager = LinearLayoutManagerWrapper(safeContext)
        explorerRecycler.adapter = recyclerAdapter
        explorerRecycler.setHasFixedSize(true)
    }

    private fun initSearchView() {
        searchView?.setOnSearchViewListener(object : SimpleSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                explorerViewModel.searchShown.value = true
            }

            override fun onSearchViewShownAnimation() {
                explorerViewModel.searchShown.value = true
            }

            override fun onSearchViewClosed() {
                explorerViewModel.searchShown.value = false
            }

            override fun onSearchViewClosedAnimation() {
                explorerViewModel.searchShown.value = false
            }
        })
        searchView?.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                explorerViewModel.searchCurrentDirectories(newText, true)
                explorerViewModel.searched = newText.isNotCleared()
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                explorerViewModel.searchCurrentDirectories(null, true)
                explorerViewModel.searched = false
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                explorerViewModel.searchCurrentDirectories(query, true)
                explorerViewModel.searched = query.isNotCleared()
                return false
            }
        })
    }

    private fun recyclerEvent(position: Int, longClick: Boolean) =
        recyclerAdapter.differ.currentList.let {
            if (position < 0) {
                return@let
            }

            val explorerItem = it[position]

            if (!longClick && explorerViewModel.selectedItems.value.isNullOrEmpty()) {
                recyclerClickEvent(explorerItem)
            } else {
                val selected = explorerViewModel.selectItem(explorerItem)
                recyclerHolderCheckboxSelected(position, selected)
            }
        }

    private fun recyclerClickEvent(explorerItem: ExplorerItem) = with(binding) {
        val fileItem = explorerItem.fileItem
        val file = fileItem.file

        if (file.isDirectory) {
            explorerViewModel.moveToPath(file, (fileItem.name != null && fileItem.name == ".."))
        } else {
            try {
                startActivity(file.intentChooser(safeContext))
            } catch (exception: Exception) {
                Timber.e("error when creating intent")
            }
        }
    }

    private fun recyclerHolderCheckboxSelected(position: Int, isChecked: Boolean) {
        val holder =
            binding.explorerRecycler.findViewHolderForAdapterPosition(position) as? ExplorerRecyclerAdapter.ViewHolder?
        holder?.binding?.explorerCheckbox?.isChecked = isChecked
    }

    private fun deleteAction() {
        val selectedItemSize = explorerViewModel.countSelectedItems()
        val confirmActionSheet =
            ConfirmActionSheet.deleteInstance(selectedItemSize > 1, selectedItemSize)
        confirmActionSheet.rightClickListener = {
            val fileProgressSheet = FileProgressSheet.deleteInstance(selectedItemSize)
            showBottomSheetFragment(fileProgressSheet)

            fileProgressSheet.updateable = {
                val job = explorerViewModel.deleteSelectedItems({
                    fileProgressSheet.updateProgressList(it)
                }) {
                    fileProgressSheet.leftText = String()
                    fileProgressSheet.rightText = "Done"
                    explorerViewModel.afterDeleteItems()
                }

                fileProgressSheet.leftClickListener = {
                    job.cancel()
                }
            }
        }
        showBottomSheetFragment(confirmActionSheet)
    }

    private fun moreAction(anchor: View) {
        val popupMenu = PopupMenu(safeContext, anchor)
        popupMenu.inflate(R.menu.explorer_bottom_extended_menu)
        popupMenu.show()
    }

    private fun onBackPressedCheck(): Boolean {
        searchView?.onBackPressed()
        return when {
            explorerViewModel.selectedItems.value.isNullOrEmpty() && !explorerViewModel.searched -> {
                when {
                    explorerViewModel.currentDirectory.value?.absolutePath == "/" -> true
                    explorerViewModel.currentDirectory.value != explorerViewModel.startDirectory -> {
                        explorerViewModel.currentDirectory.value?.let {
                            explorerViewModel.moveToPath(it.parentDir)
                        }
                        false
                    }
                    else -> true
                }
            }
            explorerViewModel.searched -> {
                searchView?.searchEditText?.setText(null)
                false
            }
            else -> {
                val list = recyclerAdapter.differ.currentList
                for (i in list.indices) {
                    recyclerHolderCheckboxSelected(i, false)
                }
                explorerViewModel.clearAllSelectedItems()
                updateToolbar()
                false
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initToolbar()
    }

    override fun onBackPressed(): Boolean = onBackPressedCheck()

    companion object {
        fun newInstance() = ExplorerFragment()
    }
}
