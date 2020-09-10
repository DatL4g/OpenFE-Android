package de.datlag.openfe.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.commons.*
import de.datlag.openfe.databinding.FragmentExplorerBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.factory.ExplorerViewModelFactory
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import de.datlag.openfe.recycler.LinearLayoutManagerWrapper
import de.datlag.openfe.recycler.adapter.ExplorerRecyclerAdapter
import de.datlag.openfe.viewmodel.AppsViewModel
import de.datlag.openfe.viewmodel.ExplorerViewModel
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
class ExplorerFragment : Fragment(), FragmentBackPressed, FragmentOptionsMenu {

    private val args: ExplorerFragmentArgs by navArgs()
    private val appsViewModel: AppsViewModel by viewModels()
    private val explorerViewModel: ExplorerViewModel by viewModels { ExplorerViewModelFactory(args, appsViewModel) }

    private lateinit var recyclerAdapter: ExplorerRecyclerAdapter
    private lateinit var binding: FragmentExplorerBinding

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
        (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(getColor(R.color.explorerToolbarIconTint)) })

        explorerToolbar.setNavigationOnClickListener {
            if (onBackPressedCheck()) {
                findNavController().navigate(R.id.action_ExplorerFragment_to_OverviewFragment)
            }
        }

        initRecycler()
        initSearchView()

        explorerViewModel.directory.value?.let {
            if (it != explorerViewModel.currentDirectory) {
                explorerViewModel.directory.value = explorerViewModel.currentDirectory
            }
        }

        explorerViewModel.directory.observe(viewLifecycleOwner) { dir ->
            explorerViewModel.moveToPath(dir)
        }
        explorerViewModel.directories.observe(viewLifecycleOwner) { list ->
            recyclerAdapter.submitList(list)
            loadingTextView.visibility = View.GONE
            explorerRecycler.visibility = View.VISIBLE
            appBar.visibility = View.VISIBLE
        }
    }

    private fun initRecycler() = with(binding) {
        recyclerAdapter = ExplorerRecyclerAdapter()

        recyclerAdapter.setOnClickListener { _, position ->
            recyclerClickEvent(position)
        }

        explorerRecycler.layoutManager = LinearLayoutManagerWrapper(saveContext)
        explorerRecycler.adapter = recyclerAdapter
        explorerRecycler.setHasFixedSize(true)
    }

    private fun recyclerClickEvent(position: Int) = explorerViewModel.directories.value?.let {
        val fileItem = it[position].fileItem
        val file = fileItem.file

        if (file.isDirectory) {
            if (fileItem.name != null && fileItem.name == "..") {
                explorerViewModel.moveToPath(file, true)
            } else {
                explorerViewModel.moveToPath(file)
            }
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setDataAndType(file.getProviderUri(saveContext) ?: file.getUri(), file.getMime(saveContext))
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

    private fun initSearchView() = with(binding) {
        explorerSearchView.setOnQueryTextListener(object: SimpleSearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextCleared(): Boolean {

                return false
            }
        })
    }

    private fun onBackPressedCheck(): Boolean {
        return when {
            explorerViewModel.currentDirectory.absolutePath == "/" -> true
            explorerViewModel.currentDirectory != explorerViewModel.startDirectory -> {
                explorerViewModel.moveToPath(explorerViewModel.currentDirectory.parentFile ?: explorerViewModel.currentDirectory)
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

    override fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean = with(binding) {
        inflater.inflate(R.menu.explorer_toolbar_menu, menu)
        menu?.let { explorerSearchView.setMenuItem(it.findItem(R.id.explorerSearchItem)) }
        return true
    }

    companion object {
        fun newInstance() = ExplorerFragment()
    }

}