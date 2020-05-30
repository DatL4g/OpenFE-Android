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
import de.datlag.openfe.recycler.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExplorerRecyclerAdapter(val context: Context, var fileList: MutableList<FileItem>) : RecyclerView.Adapter<ExplorerRecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private var clickListener: ItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val explorerRoot: ConstraintLayout = itemView.findViewById(R.id.explorerRoot) ?: (itemView as ConstraintLayout)
        val explorerIcon: AppCompatImageView = itemView.findViewById(R.id.explorerIcon)
        val explorerName: AppCompatTextView = itemView.findViewById(R.id.explorerName)

        init {
            explorerRoot.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(v ?: itemView, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.explorer_item, parent, false))
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = fileList[position].file
        val fallback = (if(file.isAPK()) ContextCompat.getDrawable(context, R.drawable.ic_adb_24dp) else ContextCompat.getDrawable(context, R.drawable.ic_baseline_insert_drive_file_24))?.apply { tint() }

        if(file.isDirectory) {
            Glide.with(context)
                .load(
                    ContextCompat.getDrawable(context, R.drawable.ic_baseline_folder_24)
                        ?.apply { tint() })
                .into(holder.explorerIcon)
        } else if(file.isAPK()) {
            GlobalScope.launch(Dispatchers.IO) {
                val icon = file.getAPKImage(context)
                withContext(Dispatchers.Main) {
                    Glide.with(context)
                        .load(icon)
                        .fallback(fallback)
                        .placeholder(fallback)
                        .error(fallback)
                        .into(holder.explorerIcon)
                }
            }
        } else {
            Glide.with(context)
                .load(file.getUri())
                .fallback(fallback)
                .placeholder(fallback)
                .error(fallback)
                .into(holder.explorerIcon)
        }

        holder.explorerName.text = fileList[position].name ?: file.name
    }

    private fun Drawable.tint(): Drawable {
        var wrappedDrawable = this.mutate()
        wrappedDrawable = DrawableCompat.wrap(wrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, R.color.explorerIconTint))
        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
        return wrappedDrawable
    }

    fun setClickListener(listener: ItemClickListener?) {
        clickListener = listener
    }

    fun updateList(list: List<FileItem>) {
        fileList = list.toMutableList()
        notifyDataSetChanged()
    }

    fun addToList(file: FileItem) {
        fileList.add(file)
        notifyItemInserted(fileList.size-1)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int)
    }

}