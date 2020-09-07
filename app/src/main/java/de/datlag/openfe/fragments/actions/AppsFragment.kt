package de.datlag.openfe.fragments.actions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferfalk.simplesearchview.SimpleSearchView
import dagger.hilt.android.AndroidEntryPoint
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.AppsActionInfoSheet
import de.datlag.openfe.commons.*
import de.datlag.openfe.databinding.FragmentAppsActionBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import de.datlag.openfe.other.AppsSortType
import de.datlag.openfe.recycler.adapter.AppsActionRecyclerAdapter
import de.datlag.openfe.recycler.data.AppItem
import de.datlag.openfe.viewmodel.AppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@AndroidEntryPoint
class AppsFragment : Fragment(), FragmentOptionsMenu, PopupMenu.OnMenuItemClickListener {

    private val viewModel: AppsViewModel by viewModels()
    private lateinit var binding: FragmentAppsActionBinding

    private var copiedList = listOf<AppItem>()
    private lateinit var adapter: AppsActionRecyclerAdapter
    private var selectedItem: Int = -1

    override fun onResume() {
        super.onResume()
        if (viewModel.apps.value.isNullOrEmpty()) {
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
        binding = FragmentAppsActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AdvancedActivity).setSupportActionBar(appsActionToolbar)
        (activity as AdvancedActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(getColor(R.color.appsActionToolbarIconTint)) })

        appsActionToolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_AppsActionFragment_to_OverviewFragment)
        }

        initRecycler()
        initEditText()
        initBottomNavigation()
        loadAppsAsync()
    }

    private fun initRecycler() = with(binding) {
        appsActionRecycler.layoutManager = GridLayoutManager(saveContext, if(saveContext.packageManager.isTelevision()) 5 else 3)
        adapter = AppsActionRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                appsActionBottomNavigation.visibility = View.VISIBLE
                selectedItem = position
                appsActionLayoutWrapper.requestLayout()
            }
        }
        adapter.submitList(listOf())
        appsActionRecycler.adapter = adapter
        appsActionRecycler.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (dy >= 24) {
                        appsActionBottomNavigation.visibility = View.GONE
                    }
                } else {
                    if(selectedItem >= 0) {
                        appsActionBottomNavigation.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    @ExperimentalContracts
    private fun initEditText() = with(binding) {
        appsActionSearchView.setOnQueryTextListener(object: SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.submitList(listOf())
                if(newText.isNotCleared()) {
                    val newListCopy = copiedList.mutableCopyOf()

                    lifecycleScope.launch(Dispatchers.IO) {
                        val iterator = newListCopy.iterator()
                        while (iterator.hasNext()) {
                            val nextItem = iterator.next()
                            if(!nextItem.name.contains(newText, true) && !nextItem.packageName.contains(newText, true)) {
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
            when(it.itemId) {
                R.id.appsActionBottomUninstallApp -> {
                    requestUninstall()
                    true
                }
                R.id.appsActionBottomLaunchApp -> {
                    requestLaunch()
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
        viewModel.apps.observe(viewLifecycleOwner) { list ->
            if(list.isNotEmpty()) {
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

    private fun requestUninstall() {
        if(selectedItemValid()) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:${adapter.differ.currentList[selectedItem].packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            saveContext.startActivity(intent)
        }
    }

    private fun requestLaunch() {
        if(selectedItemValid()) {
            startActivity(saveContext.packageManager.getLaunchIntentForPackage(adapter.differ.currentList[selectedItem].packageName))
        }
    }

    private fun requestInfo() {
        if(selectedItemValid()) {
            showBottomSheetFragment(AppsActionInfoSheet.newInstance(adapter.differ.currentList[selectedItem]))
        }
    }

    private fun selectedItemValid(customRange: Pair<Int, Int> = Pair(0, adapter.differ.currentList.size-1)): Boolean {
        return ((selectedItem >= customRange.first) && (selectedItem <= customRange.second))
    }

    private fun showPopupMenu(anchor: View) {
        val popupMenu = PopupMenu(saveContext, anchor)
        popupMenu.menuInflater.inflate(R.menu.apps_action_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    private fun setupMenuItemClickListener(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.appsActionFilterItem -> {
                showPopupMenu(item.actionView ?: requireView().rootView.findViewById(item.itemId))
            }
        }
        return false
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        p0?.let {
            when(it.itemId) {
                R.id.appsActionPopupFilterName -> viewModel.sortType = AppsSortType.NAME
                R.id.appsActionPopupFilterInstalled -> viewModel.sortType = AppsSortType.INSTALLED
                R.id.appsActionPopupFilterUpdated -> viewModel.sortType = AppsSortType.UPDATED
                else -> viewModel.sortType = AppsSortType.NAME
            }
        }
        return false
    }

    override fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean = with(binding) {
        inflater.inflate(R.menu.apps_action_toolbar_menu, menu)
        menu?.let {
            appsActionSearchView.setMenuItem(it.findItem(R.id.appsActionSearchItem))
            for(item in it.iterator()) {
                if(item.itemId != R.id.appsActionSearchItem) {
                    item.setOnMenuItemClickListener { menuItem ->
                        return@setOnMenuItemClickListener setupMenuItemClickListener(menuItem)
                    }
                }
            }
        }
        return true
    }

    companion object {
        fun newInstance() = AppsFragment()
    }

}