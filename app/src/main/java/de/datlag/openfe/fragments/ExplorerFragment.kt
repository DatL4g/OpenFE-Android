package de.datlag.openfe.fragments

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ferfalk.simplesearchview.SimpleSearchView
import de.datlag.openfe.R
import de.datlag.openfe.commons.*
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import de.datlag.openfe.recycler.adapter.ExplorerRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.recycler.data.FileItem
import kotlinx.android.synthetic.main.fragment_explorer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class ExplorerFragment : Fragment(), FragmentBackPressed, FragmentOptionsMenu {

    private lateinit var recyclerAdapter: ExplorerRecyclerAdapter
    private lateinit var fileList: MutableList<ExplorerItem>
    private lateinit var copiedList: MutableList<ExplorerItem>
    private lateinit var currentDirectory: File
    private lateinit var startDirectory: File
    private val selectedItems: MutableList<ExplorerItem> = mutableListOf()
    private val arrayList: MutableList<ApplicationInfo> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(saveContext, R.style.ExplorerFragmentTheme)
        val clonedLayoutInflater = inflater.cloneInContext(contextThemeWrapper)

        return clonedLayoutInflater.inflate(R.layout.fragment_explorer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AdvancedActivity).setSupportActionBar(explorerToolbar)
        (activity as AdvancedActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(getColor(R.color.explorerToolbarIconTint)) })

        explorerToolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_ExplorerFragment_to_OverviewFragment)
        }

        initRecycler()
        initSearchView()

        GlobalScope.launch(Dispatchers.IO) {
            saveContext.packageManager.loadAppsAsync(false) {
                arrayList.add(it)
            }
            withContext(Dispatchers.Main) {
                initStartPath()
            }
        }
    }

    private fun initRecycler() {
        recyclerAdapter = ExplorerRecyclerAdapter(mutableListOf())
        recyclerAdapter.setOnClickListener { _, position ->
            if(selectedItems.size == 0) {
                recyclerClickEvent(position)
            } else {
                if(fileList[position].selectable) {
                    recyclerSelectEvent(position)
                }
            }
        }
        recyclerAdapter.setOnLongClickListener { _, position ->
            if (fileList[position].selectable) {
                recyclerSelectEvent(position)
            }
            true
        }

        explorerRecycler.layoutManager = LinearLayoutManager(saveContext)
        explorerRecycler.adapter = recyclerAdapter
    }

    private fun initStartPath() {
        val file = File(arguments?.getString("filePath") ?: getString(R.string.default_explorer_path))
        startDirectory = if(file.isDirectory) file else file.parentFile ?: File(file.getRootOfStorage())
        moveToPath(startDirectory)
    }

    private fun initSearchView() {
        explorerSearchView?.setOnQueryTextListener(object: SimpleSearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recyclerAdapter.clearData()
                if (newText.isNotCleared()) {
                    val newListCopy = copiedList.mutableCopyOf()
                    GlobalScope.launch(Dispatchers.IO) {
                        val iterator = newListCopy.iterator()

                        while (iterator.hasNext()) {
                            val nextItem = iterator.next()
                            val itemName = nextItem.fileItem.name ?: nextItem.fileItem.file.name

                            if (!itemName.contains(newText, true)) {
                                iterator.remove()
                                continue
                            }

                            withContext(Dispatchers.Main) {
                                recyclerAdapter.addToList(nextItem)
                            }
                        }
                    }
                } else {
                    recyclerAdapter.submitList(copiedList)
                }
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                recyclerAdapter.submitList(copiedList)
                return false
            }
        })
    }

    fun moveToPath(path: File, force: Boolean = false) {
        val newPath = if(path.isInternal() && !force) startDirectory else path

        if(newPath.isDirectory) {
            val tempList = newPath.listFiles()?.toMutableList() ?: mutableListOf()

            fileList = mutableListOf(ExplorerItem(FileItem(newPath.parentFile ?: if(newPath.parent != null) File(newPath.parent!!) else newPath, ".."), null, false))
            recyclerAdapter.submitList(fileList)

            GlobalScope.launch(Dispatchers.IO) {
                tempList.sort()

                val fileIterator = tempList.listIterator()
                while (fileIterator.hasNext()) {
                    val file = fileIterator.next()
                    if(!file.isHidden) {
                        val fileItem = FileItem(file)
                        var image: Drawable? = null

                        for (appItem in arrayList) {
                            val fileName = fileItem.name ?: fileItem.file.name
                            if (fileName.toLower() == appItem.loadLabel(saveContext.packageManager)
                                    .toString()
                                    .toLower() || fileName.toLower() == appItem.packageName.toLower()
                            ) {
                                image = appItem.loadIcon(saveContext.packageManager)
                            }
                        }

                        fileList.add(ExplorerItem(fileItem, image))

                        withContext(Dispatchers.Main) {
                            recyclerAdapter.addToList(fileList[fileList.size-1])
                            copiedList = recyclerAdapter.getData().mutableCopyOf()
                            loadingTextView.visibility = View.GONE
                            explorerRecycler.visibility = View.VISIBLE
                            appBar.visibility = View.VISIBLE
                        }
                    }
                }
            }
            currentDirectory = newPath
        }
    }

    private fun recyclerClickEvent(position: Int) {
        val file = fileList[position].fileItem.file

        if(file.isDirectory) {
            if(fileList[position].fileItem.name != null && fileList[position].fileItem.name == "..") {
                moveToPath(file, true)
            } else {
                moveToPath(file)
            }
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setDataAndType(file.getProviderUri(saveContext) ?: file.getUri(), file.getMime(saveContext))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            }
            val startIntent = Intent.createChooser(intent, "Choose App to open file")
            startActivity(startIntent)
        }
    }

    private fun recyclerSelectEvent(position: Int) {
        if (selectedItems.contains(fileList[position])) {
            recyclerSelectRemove(position)
        } else {
            recyclerSelectAdd(position)
        }
        copiedList = recyclerAdapter.getData().mutableCopyOf()
    }

    private fun recyclerSelectAdd(position: Int) {
        selectedItems.add(fileList[position])
        val holder: ExplorerRecyclerAdapter.ViewHolder = explorerRecycler.findViewHolderForItemId(position.toLong()) as ExplorerRecyclerAdapter.ViewHolder
        with(holder) {
            binding.explorerCheckbox.isChecked = true
        }
        fileList[position].selected = true
        recyclerAdapter.submitList(fileList, false)
        updateToolbar()
    }

    private fun recyclerSelectRemove(position: Int) {
        selectedItems.remove(fileList[position])
        val holder: ExplorerRecyclerAdapter.ViewHolder = explorerRecycler.findViewHolderForItemId(position.toLong()) as ExplorerRecyclerAdapter.ViewHolder
        with(holder) {
            binding.explorerCheckbox.isChecked = false
        }
        fileList[position].selected = false
        recyclerAdapter.submitList(fileList, false)
        updateToolbar()
    }

    private fun updateToolbar() {
        if (selectedItems.size == 0) {
            (activity as AdvancedActivity).supportActionBar?.title = getString(R.string.app_name)
            (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply { tint(getColor(R.color.explorerToolbarIconTint)) })

            explorerToolbar.setNavigationOnClickListener {
                findNavController().navigate(R.id.action_ExplorerFragment_to_OverviewFragment)
            }
        } else {
            (activity as AdvancedActivity).supportActionBar?.title = "${selectedItems.size} selected"
            (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_close_24dp)?.apply { tint(getColor(R.color.explorerToolbarIconTint)) })

            explorerToolbar.setNavigationOnClickListener {
                selectedItems.removeAll(selectedItems)
                (activity as AdvancedActivity).supportActionBar?.title =
                    getString(R.string.app_name)
                (activity as AdvancedActivity).supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_arrow_back_24dp)?.apply {
                    tint(
                        getColor(R.color.explorerToolbarIconTint)
                    )
                })

                for (item in fileList) {
                    item.selected = false
                }
                recyclerAdapter.submitList(fileList)

                explorerToolbar.setNavigationOnClickListener {
                    findNavController().navigate(R.id.action_ExplorerFragment_to_OverviewFragment)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.explorerStatusbarColor))
    }

    override fun onBackPressed(): Boolean {
        return when {
            currentDirectory.absolutePath == "/" -> true
            currentDirectory != startDirectory -> {
                moveToPath(currentDirectory.parentFile ?: currentDirectory)
                false
            }
            else -> true
        }
    }

    override fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean {
        inflater.inflate(R.menu.explorer_toolbar_menu, menu)
        menu?.let { explorerSearchView?.setMenuItem(it.findItem(R.id.explorerSearchItem)) }
        return true
    }

    companion object {
        fun newInstance() = ExplorerFragment()
    }

}