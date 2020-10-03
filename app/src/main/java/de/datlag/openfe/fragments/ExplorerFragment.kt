package de.datlag.openfe.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.FileProgressSheet
import de.datlag.openfe.commons.androidGreaterOr
import de.datlag.openfe.commons.countRecursively
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.getDrawable
import de.datlag.openfe.commons.getMimeType
import de.datlag.openfe.commons.getProviderUri
import de.datlag.openfe.commons.getUri
import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.mutableCopyOf
import de.datlag.openfe.commons.saveContext
import de.datlag.openfe.commons.showBottomSheetFragment
import de.datlag.openfe.commons.statusBarColor
import de.datlag.openfe.commons.tint
import de.datlag.openfe.databinding.FragmentExplorerBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.factory.ExplorerViewModelFactory
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import de.datlag.openfe.recycler.LinearLayoutManagerWrapper
import de.datlag.openfe.recycler.adapter.ExplorerRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.viewmodel.AppsViewModel
import de.datlag.openfe.viewmodel.ExplorerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
class ExplorerFragment : Fragment(), FragmentBackPressed, FragmentOptionsMenu {

    private val args: ExplorerFragmentArgs by navArgs()
    private val appsViewModel: AppsViewModel by viewModels()
    private val explorerViewModel: ExplorerViewModel by viewModels { ExplorerViewModelFactory(args, appsViewModel) }

    private lateinit var recyclerAdapter: ExplorerRecyclerAdapter
    private lateinit var binding: FragmentExplorerBinding
    private var copiedList = listOf<ExplorerItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(saveContext, R.style.ExplorerFragmentTheme)
        val clonedLayoutInflater = inflater.cloneInContext(contextThemeWrapper)

        binding = FragmentExplorerBinding.inflate(clonedLayoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AdvancedActivity).setSupportActionBar(explorerToolbar)
        (activity as AdvancedActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        updateToolbar()

        explorerToolbar.setNavigationOnClickListener {
            if (onBackPressedCheck()) {
                findNavController().navigate(R.id.action_ExplorerFragment_to_OverviewFragment)
            }
        }

        initRecycler()
        initBottomNavigation()
        initSearchView()

        explorerViewModel.directory.value?.let {
            if (it != explorerViewModel.currentDirectory) {
                explorerViewModel.directory.value = explorerViewModel.currentDirectory
            }
        }

        explorerViewModel.directory.observe(viewLifecycleOwner) { dir ->
            if (explorerViewModel.selectedItems.isNullOrEmpty()) {
                explorerViewModel.moveToPath(dir)
            }
        }
        explorerViewModel.directories.observe(viewLifecycleOwner) { list ->
            recyclerAdapter.submitList(list)
            if (!explorerViewModel.isSearching) {
                copiedList = list.mutableCopyOf()
            }
            loadingTextView.visibility = View.GONE
            explorerRecycler.visibility = View.VISIBLE
            appBar.visibility = View.VISIBLE
        }
    }

    private fun initRecycler() = with(binding) {
        recyclerAdapter = ExplorerRecyclerAdapter(lifecycleScope)

        recyclerAdapter.setOnClickListener { _, position ->
            recyclerEvent(position)
        }
        recyclerAdapter.setOnLongClickListener { _, position ->
            recyclerEvent(position, true)
            true
        }

        explorerRecycler.layoutManager = LinearLayoutManagerWrapper(saveContext)
        explorerRecycler.adapter = recyclerAdapter
        explorerRecycler.setHasFixedSize(true)
    }

    private fun recyclerEvent(position: Int, longClick: Boolean = false) = recyclerAdapter.differ.currentList.let {
        val explorerItem = it[position]

        if (explorerViewModel.selectedItems.isNotEmpty() || longClick) {
            if (explorerItem.selectable) {
                recyclerSelectEvent(explorerItem, position)
            }
        } else {
            recyclerClickEvent(explorerItem)
        }
    }

    private fun recyclerClickEvent(explorerItem: ExplorerItem) {
        val fileItem = explorerItem.fileItem
        val file = fileItem.file

        if (file.isDirectory) {
            if (fileItem.name != null && fileItem.name == "..") {
                explorerViewModel.moveToPath(file, true)
            } else {
                explorerViewModel.moveToPath(file)
            }
        } else {
            val mime = file.getMimeType(saveContext)
            Log.e("MimeType", mime.toString())
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setDataAndType(file.getProviderUri(saveContext) ?: file.getUri(), mime)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            if (androidGreaterOr(Build.VERSION_CODES.KITKAT)) {
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            if (androidGreaterOr(Build.VERSION_CODES.LOLLIPOP)) {
                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Choose App to open file"))
        }
    }

    private fun recyclerSelectEvent(explorerItem: ExplorerItem, position: Int) {
        if (explorerViewModel.selectedItems.contains(explorerItem)) {
            explorerViewModel.selectedItems.remove(explorerItem)
            explorerViewModel.recyclerSelectUpdate(explorerItem, position, recyclerAdapter, binding)
        } else {
            val newItem = explorerViewModel.recyclerSelectUpdate(explorerItem, position, recyclerAdapter, binding)
            explorerViewModel.selectedItems.add(newItem)
        }
        updateToolbar()
    }

    private fun updateToolbar() = with(binding) {
        if (explorerViewModel.selectedItems.isEmpty()) {
            (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(getColor(R.color.explorerToolbarIconTint)) })
            (activity as AdvancedActivity).supportActionBar?.title = saveContext.getString(R.string.app_name)
            explorerBottomNavigation.visibility = View.GONE
        } else {
            (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_close_24dp)?.apply { tint(getColor(R.color.explorerToolbarIconTint)) })
            (activity as AdvancedActivity).supportActionBar?.title = "${explorerViewModel.selectedItems.size} Items"
            explorerBottomNavigation.visibility = View.VISIBLE
        }
    }

    private fun initBottomNavigation() = with(binding) {
        explorerBottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.explorerBottomDelete -> {
                    deleteSelectedFiles()
                    true
                }
                else -> false
            }
        }
    }

    private fun initSearchView() = with(binding) {
        explorerSearchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNotCleared()) {
                    explorerViewModel.isSearching = false
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recyclerAdapter.submitList(listOf())

                if (newText.isNotCleared()) {
                    explorerViewModel.isSearching = true
                    val newListCopy = copiedList.mutableCopyOf()

                    lifecycleScope.launch(Dispatchers.IO) {
                        val iterator = newListCopy.iterator()

                        while (iterator.hasNext()) {
                            val nextItem = iterator.next()
                            if ((nextItem.fileItem.name?.contains(newText, true) != true) && !nextItem.fileItem.file.name.contains(newText, true)) {
                                iterator.remove()
                                continue
                            }
                        }
                        withContext(Dispatchers.Main) {
                            recyclerAdapter.submitList(newListCopy)
                        }
                    }
                } else {
                    explorerViewModel.isSearching = false
                    recyclerAdapter.submitList(copiedList)
                }
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                explorerViewModel.isSearching = false
                recyclerAdapter.submitList(copiedList)
                return false
            }
        })
    }

    private fun onBackPressedCheck(): Boolean {
        return if (explorerViewModel.selectedItems.isEmpty()) {
            when {
                explorerViewModel.currentDirectory.absolutePath == "/" -> true
                explorerViewModel.currentDirectory != explorerViewModel.startDirectory -> {
                    explorerViewModel.moveToPath(explorerViewModel.currentDirectory.parentFile ?: explorerViewModel.currentDirectory)
                    false
                }
                else -> true
            }
        } else {
            explorerViewModel.clearSelectedItems(copiedList, binding)
            updateToolbar()
            false
        }
    }

    private fun deleteSelectedFiles() {
        val fileProgressSheet = FileProgressSheet.newInstance()

        fileProgressSheet.title = "Delete Files..."
        fileProgressSheet.text = "Deleting ${countSelectedFilesRecursively()} files..."
        fileProgressSheet.rightText = "Close"
        fileProgressSheet.closeOnRightClick = false
        fileProgressSheet.leftText = "Cancel"
        fileProgressSheet.closeOnLeftClick = false
        fileProgressSheet.updateable = {
            explorerViewModel.deleteSelectedItems(fileProgressSheet) {
                fileProgressSheet.leftText = String()
                fileProgressSheet.rightText = "Done"
                explorerViewModel.moveToPath(explorerViewModel.currentDirectory)
                explorerViewModel.clearSelectedItems(copiedList, binding)
                updateToolbar()
            }
        }

        showBottomSheetFragment(fileProgressSheet)
    }

    private fun countSelectedFilesRecursively(direction: FileWalkDirection = FileWalkDirection.TOP_DOWN): Int {
        var count = 0
        for (item in explorerViewModel.selectedItems) {
            count += item.fileItem.file.countRecursively(direction)
        }
        return count
    }

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.explorerStatusbarColor))
    }

    override fun onBackPressed(): Boolean = onBackPressedCheck()

    override fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean = with(binding) {
        inflater.inflate(R.menu.explorer_toolbar_menu, menu)
        menu?.let { explorerSearchView.setMenuItem(it.findItem(R.id.explorerSearchItem)) }
        return true
    }

    companion object {
        fun newInstance() = ExplorerFragment()
    }
}
