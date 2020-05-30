package de.datlag.openfe.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.datlag.openfe.R
import de.datlag.openfe.commons.*
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.recycler.adapter.ExplorerRecyclerAdapter
import de.datlag.openfe.recycler.data.FileItem
import kotlinx.android.synthetic.main.fragment_explorer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExplorerFragment : Fragment(), FragmentBackPressed {

    private lateinit var recyclerAdapter: ExplorerRecyclerAdapter
    private lateinit var fileList: MutableList<FileItem>
    private lateinit var currentFile: File
    private val previousFiles: MutableSet<File> = mutableSetOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explorer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerAdapter = ExplorerRecyclerAdapter(saveContext, mutableListOf()).apply {
            this.setClickListener(object: ExplorerRecyclerAdapter.ItemClickListener{
                override fun onClick(view: View, position: Int) {
                    val file = fileList[position].file

                    if(file.isDirectory) {
                        moveToPath(file)
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
                        startActivity(intent)
                    }
                }
            })
        }

        val file = File(arguments?.getString("filePath") ?: getString(R.string.default_explorer_path))
        val startDirectory = if(file.isDirectory) file else file.parentFile
        currentFile = startDirectory
        moveToPath(startDirectory)

        explorerRecycler.layoutManager = LinearLayoutManager(saveContext)
        explorerRecycler.adapter = recyclerAdapter
    }

    fun moveToPath(path: File) {
        if(path.isDirectory) {
            val tempList = path.listFiles()?.toMutableList() ?: mutableListOf()

            fileList = mutableListOf(FileItem(path.parentFile ?: if(path.parent != null) File(path.parent!!) else path, ".."))
            recyclerAdapter.updateList(fileList)

            GlobalScope.launch {
                tempList.sort()

                val fileIterator = tempList.listIterator()
                while (fileIterator.hasNext()) {
                    val file = fileIterator.next()
                    if(!file.isHidden) {
                        fileList.add(FileItem(file))
                        withContext(Dispatchers.Main) {
                            recyclerAdapter.addToList(fileList[fileList.size-1])
                        }
                    }
                }
            }
            if(path.parentFile == currentFile) {
                previousFiles.add(path)
            }
            currentFile = path
        }
    }

    companion object {
        fun newInstance() = ExplorerFragment()
    }

    override fun onBackPressed(): Boolean {
        return if(previousFiles.size > 1) {
            val moveFile = previousFiles.elementAt(previousFiles.size-2)
            previousFiles.remove(previousFiles.elementAt(previousFiles.size-1))
            previousFiles.remove(previousFiles.elementAt(previousFiles.size-1))
            moveToPath(moveFile)
            false
        } else {
            true
        }
    }

}