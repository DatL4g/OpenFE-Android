package de.datlag.openfe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.commons.getColor
import de.datlag.openfe.commons.hide
import de.datlag.openfe.commons.intentChooser
import de.datlag.openfe.commons.parentDir
import de.datlag.openfe.commons.safeContext
import de.datlag.openfe.commons.show
import de.datlag.openfe.commons.statusBarColor
import de.datlag.openfe.databinding.FragmentExplorerBinding
import de.datlag.openfe.extend.AdvancedFragment
import de.datlag.openfe.factory.ExplorerViewModelFactory
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.recycler.LinearLayoutManagerWrapper
import de.datlag.openfe.recycler.adapter.ExplorerRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.viewmodel.AppsViewModel
import de.datlag.openfe.viewmodel.ExplorerViewModel
import timber.log.Timber
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
class ExplorerFragment : AdvancedFragment(), FragmentBackPressed {

    private val args: ExplorerFragmentArgs by navArgs()
    private val appsViewModel: AppsViewModel by viewModels()
    private val explorerViewModel: ExplorerViewModel by viewModels { ExplorerViewModelFactory(args, appsViewModel) }

    private lateinit var recyclerAdapter: ExplorerRecyclerAdapter
    private lateinit var binding: FragmentExplorerBinding

    private val navigationListener = View.OnClickListener {
        if (onBackPressedCheck()) {
            findNavController().navigate(R.id.action_ExplorerFragment_to_OverviewFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(safeContext, R.style.ExplorerFragmentTheme)
        val clonedLayoutInflater = inflater.cloneInContext(contextThemeWrapper)

        binding = FragmentExplorerBinding.inflate(clonedLayoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        toolbar?.menu?.clear()
        toolbar?.inflateMenu(R.menu.explorer_toolbar_menu)
        toolbar?.menu?.let { searchView?.setMenuItem(it.findItem(R.id.explorerSearchItem)) }

        updateToggle(false, navigationListener)

        initRecyclerView()
        initSearchView()

        explorerViewModel.currentDirectory.observe(viewLifecycleOwner) { dir ->
            Timber.e(dir.absolutePath)
        }

        explorerViewModel.currentSubDirectories.observe(viewLifecycleOwner) { list ->
            checkViewVisibility(list.size)
            recyclerAdapter.submitList(list)
        }
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
    }

    private fun initSearchView() {
        searchView?.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                explorerViewModel.searchCurrentDirectories(newText, true)
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                explorerViewModel.searchCurrentDirectories(null, true)
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                explorerViewModel.searchCurrentDirectories(query, true)
                return false
            }
        })
    }

    private fun recyclerEvent(position: Int, longClick: Boolean) = recyclerAdapter.differ.currentList.let {
        if (position < 0) {
            return@let
        }

        val explorerItem = it[position]

        // ToDo("check selected items")
        if (!longClick) {
            recyclerClickEvent(explorerItem)
        } else {
            // ToDO("select")
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

    private fun checkViewVisibility(listSize: Int) = with(binding) {
        if (listSize == 0) {
            explorerRecycler.hide()
            loadingTextView.show()
        } else {
            explorerRecycler.show()
            loadingTextView.hide()
        }
    }

    private fun onBackPressedCheck(): Boolean {
        return when {
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

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.explorerStatusbarColor))
    }

    override fun onBackPressed(): Boolean = onBackPressedCheck()

    companion object {
        fun newInstance() = ExplorerFragment()
    }
}
