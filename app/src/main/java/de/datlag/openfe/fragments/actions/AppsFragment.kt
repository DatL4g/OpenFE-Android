package de.datlag.openfe.fragments.actions

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.AppsActionInfoSheet
import de.datlag.openfe.commons.*
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.RecyclerAdapterItemClickListener
import de.datlag.openfe.recycler.adapter.AppsActionRecyclerAdapter
import de.datlag.openfe.recycler.data.AppItem
import kotlinx.android.synthetic.main.fragment_apps_action.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AppsFragment : Fragment() {

    private var copiedList = listOf<AppItem>()
    private lateinit var adapter: AppsActionRecyclerAdapter
    private var selectedItem: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        initToolbar()
        initRecycler()
        initEditText()
        initBottomNavigation()
        loadAppsAsync()
    }

    private fun initToolbar() {
        (activity as AdvancedActivity).setSupportActionBar(toolBar)
        toolBar.background.alpha = 0
        toolBar.setBackgroundColor(Color.TRANSPARENT)

        appsActionSearchBack.setOnClickListener {
            if(!appsActionSearchEdit.text.isNullOrEmpty() && !appsActionSearchEdit.text.isNullOrBlank()) {
                appsActionSearchEdit.text?.clear()
            } else {
                activity?.onBackPressed()
            }
        }
    }

    private fun initRecycler() {
        appsActionRecycler.layoutManager = GridLayoutManager(saveContext, if(saveContext.packageManager.isTelevision()) 5 else 3)
        adapter = AppsActionRecyclerAdapter(mutableListOf()).apply {
            clickListener = RecyclerAdapterItemClickListener { _, position ->
                appsActionBottomNavigation.visibility = View.VISIBLE
                selectedItem = position
                appsActionLayoutWrapper.requestLayout()
            }
        }
        appsActionRecycler.adapter = adapter
        appsActionRecycler.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (dy >= 24) {
                        appsActionSearch.visibility = View.GONE
                        appsActionBottomNavigation.visibility = View.GONE
                    }
                } else {
                    appsActionSearch.visibility = View.VISIBLE
                    if(selectedItem >= 0) {
                        appsActionBottomNavigation.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun initEditText() {
        appsActionSearchEdit.setOnClickListener {
            it.requestFocus()
        }
        appsActionSearchIcon.setOnClickListener {
            appsActionSearchEdit.requestFocus()
        }
        appsActionSearchEdit.onFocusChangeListener =
            View.OnFocusChangeListener { _, p1 ->
                appsActionSearchEdit.setHintTextColor(if(!p1) Color.TRANSPARENT else Color.GRAY)
            }
        appsActionSearchEdit.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) { }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.clearData()
                if (!p0.isNullOrEmpty() && !p0.isNullOrBlank()) {

                    val newListCopy = copiedList.mutableCopyOf()
                    GlobalScope.launch {

                        val iterator = newListCopy.iterator()
                        while (iterator.hasNext()) {
                            val nextItem = iterator.next()
                            if(!nextItem.name.contains(p0, true)) {
                                iterator.remove()
                                continue
                            }

                            withContext(Dispatchers.Main) {
                                adapter.addData(nextItem)
                            }
                        }
                    }
                    appsActionSearchBack.setImageDrawable(getDrawable(R.drawable.ic_close_24dp))
                } else {
                    adapter.updateData(copiedList)
                    appsActionSearchBack.setImageDrawable(getDrawable(R.drawable.ic_arrow_back_24dp))
                }
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
        GlobalScope.launch {
            val apps: MutableList<ApplicationInfo> = saveContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            Collections.sort(apps, ApplicationInfo.DisplayNameComparator(saveContext.packageManager))

            val iterator = apps.iterator()
            while(iterator.hasNext()) {
                val nextItem = iterator.next()
                if(nextItem.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                    iterator.remove()
                    continue
                } else if (nextItem.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) {
                    iterator.remove()
                    continue
                }

                try {
                    if(saveContext.packageManager.getLaunchIntentForPackage(nextItem.packageName) == null) {
                        iterator.remove()
                        continue
                    }
                } catch (ignored: Exception) { }

                withContext(Dispatchers.Main) {
                    adapter.addData(AppItem(nextItem.loadIcon(saveContext.packageManager),
                        nextItem.loadLabel(saveContext.packageManager).toString(), nextItem.packageName))
                    copiedList = adapter.getData().mutableCopyOf()
                    loadingTextView.visibility = View.GONE
                    appsActionSearch.visibility = View.VISIBLE
                    appsActionRecycler.visibility = View.VISIBLE
                    appsActionLayoutWrapper.visibility = View.VISIBLE
                }
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

    companion object {
        fun newInstance() = AppsFragment()
    }

}