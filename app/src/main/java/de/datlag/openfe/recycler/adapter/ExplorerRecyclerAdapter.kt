package de.datlag.openfe.recycler.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.datlag.openfe.R
import de.datlag.openfe.commons.getAPKImage
import de.datlag.openfe.commons.getUri
import de.datlag.openfe.commons.isAPK
import de.datlag.openfe.commons.tint
import de.datlag.openfe.databinding.ExplorerItemBinding
import de.datlag.openfe.interfaces.RecyclerAdapterItemClickListener
import de.datlag.openfe.recycler.data.FileItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExplorerRecyclerAdapter(var fileList: MutableList<FileItem>) : RecyclerView.Adapter<ExplorerRecyclerAdapter.ViewHolder>() {

    var clickListener: RecyclerAdapterItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = ExplorerItemBinding.bind(containerView ?: itemView)
        val context: Context = containerView?.context ?: itemView.context

        init {
            binding.explorerRoot.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(v ?: containerView ?: itemView, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.explorer_item, parent, false))
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        val file = fileList[position].file
        val fallback = (if(file.isAPK()) ContextCompat.getDrawable(context, R.drawable.ic_adb_24dp) else ContextCompat.getDrawable(context, R.drawable.ic_baseline_insert_drive_file_24))?.apply { tint(context) }

        if(file.isDirectory) {
            Glide.with(context)
                .load(
                    ContextCompat.getDrawable(context, R.drawable.ic_baseline_folder_24)
                        ?.apply { tint(context) })
                .into(binding.explorerIcon)
        } else if(file.isAPK()) {
            GlobalScope.launch(Dispatchers.IO) {
                val icon = file.getAPKImage(context)
                withContext(Dispatchers.Main) {
                    Glide.with(context)
                        .load(icon)
                        .fallback(fallback)
                        .placeholder(fallback)
                        .error(fallback)
                        .into(binding.explorerIcon)
                }
            }
        } else {
            Glide.with(context)
                .load(file.getUri())
                .fallback(fallback)
                .placeholder(fallback)
                .error(fallback)
                .into(binding.explorerIcon)
        }

        binding.explorerName.text = fileList[position].name ?: file.name
    }

    fun updateList(list: List<FileItem>) {
        fileList = list.toMutableList()
        notifyDataSetChanged()
    }

    fun addToList(file: FileItem) {
        fileList.add(file)
        notifyItemInserted(fileList.size-1)
    }

}