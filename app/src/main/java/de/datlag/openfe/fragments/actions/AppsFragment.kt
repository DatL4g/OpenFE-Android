package de.datlag.openfe.fragments.actions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ferfalk.simplesearchview.SimpleSearchView
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.AppsActionInfoSheet
import de.datlag.openfe.commons.*
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import de.datlag.openfe.recycler.adapter.AppsActionRecyclerAdapter
import de.datlag.openfe.recycler.data.AppItem
import kotlinx.android.synthetic.main.fragment_apps_action.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class AppsFragment : Fragment(), FragmentOptionsMenu {

    private var copiedList = listOf<AppItem>()
    private lateinit var adapter: AppsActionRecyclerAdapter
    private var selectedItem: Int = -1

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.appsActionStatusbarColor))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apps_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AdvancedActivity).setSupportActionBar(appsActionToolbar)
        (activity as AdvancedActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(getColor(R.color.explorerToolbarIconTint)) })

        appsActionToolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_AppsActionFragment_to_OverviewFragment)
        }

        initRecycler()
        initEditText()
        initBottomNavigation()
        loadAppsAsync()
    }

    private fun initRecycler() {
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
    private fun initEditText() {
        appsActionSearchView?.setOnQueryTextListener(object: SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.submitList(listOf())
                if(newText.isNotCleared()) {
                    val newListCopy = copiedList.mutableCopyOf()
                    GlobalScope.launch {
                        val iterator = newListCopy.iterator()
                        while (iterator.hasNext()) {
                            val nextItem = iterator.next()
                            if(!nextItem.name.contains(newText, true)) {
                                iterator.remove()
                                continue
                            }
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

    private fun loadAppsAsync() {
        GlobalScope.launch(Dispatchers.IO) {
            saveContext.packageManager.loadAppsAsync(true) {
                adapter.addToList(AppItem(it.loadIcon(saveContext.packageManager), it.loadLabel(saveContext.packageManager).toString(), it.packageName))
                copiedList = adapter.differ.currentList.mutableCopyOf()
                loadingTextView?.visibility = View.GONE
                appBar?.visibility = View.VISIBLE
                appsActionRecycler?.visibility = View.VISIBLE
                appsActionLayoutWrapper?.visibility = View.VISIBLE
                statusBarColor(getColor(R.color.appsActionStatusbarColor))
            }
        }
    }

    private fun requestUninstall() {
        if(selectedItemValid()) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:${copiedList[selectedItem].packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            saveContext.startActivity(intent)
        }
    }

    private fun requestLaunch() {
        if(selectedItemValid()) {
            startActivity(saveContext.packageManager.getLaunchIntentForPackage(copiedList[selectedItem].packageName))
        }
    }

    private fun requestInfo() {
        if(selectedItemValid()) {
            showBottomSheetFragment(AppsActionInfoSheet.newInstance(copiedList[selectedItem]))
        }
    }

    private fun selectedItemValid(customRange: Pair<Int, Int> = Pair(0, copiedList.size-1)): Boolean {
        return ((selectedItem >= customRange.first) && (selectedItem <= customRange.second))
    }

    override fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean {
        inflater.inflate(R.menu.apps_action_toolbar_menu, menu)
        menu?.let { appsActionSearchView?.setMenuItem(it.findItem(R.id.appsActionSearchItem)) }
        return true
    }

    companion object {
        fun newInstance() = AppsFragment()
    }

}