package de.datlag.openfe.recycler.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.datlag.openfe.R
import de.datlag.openfe.commons.*
import de.datlag.openfe.databinding.ExplorerItemBinding
import de.datlag.openfe.extend.ClickRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExplorerRecyclerAdapter : ClickRecyclerAdapter<ExplorerRecyclerAdapter.ViewHolder>() {

    private val diffCallback = object: DiffUtil.ItemCallback<ExplorerItem>() {
        override fun areItemsTheSame(oldItem: ExplorerItem, newItem: ExplorerItem): Boolean {
            return oldItem.fileItem.file.absolutePath == newItem.fileItem.file.absolutePath
        }

        override fun areContentsTheSame(oldItem: ExplorerItem, newItem: ExplorerItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = ExplorerItemBinding.bind(containerView ?: itemView)
        val context: Context = containerView?.context ?: itemView.context

        init {
            binding.explorerRoot.setOnClickListener(this)
            binding.explorerRoot.setOnLongClickListener(this)
            ViewCompat.setTranslationZ(binding.explorerIcon, 1F)
            ViewCompat.setTranslationZ(binding.explorerAppIcon, 2F)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(v ?: containerView ?: itemView, adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            return longClickListener?.invoke(p0 ?: containerView ?: itemView, adapterPosition) ?: true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.explorer_item, parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        val item = differ.currentList[position]
        val file = item.fileItem.file
        val fileIsApk = file.isAPK()
        val fileName = item.fileItem.name ?: file.name
        val fallback = (if (fileIsApk) ContextCompat.getDrawable(context, R.drawable.ic_adb_24dp) else ContextCompat.getDrawable(context, R.drawable.ic_baseline_insert_drive_file_24))?.apply {
            tint(ContextCompat.getColor(context, R.color.explorerIconTint))
        }

        when {
            file.isDirectory -> {
                Glide.with(context)
                    .load(ContextCompat.getDrawable(context, R.drawable.ic_baseline_folder_24)?.apply { tint(ContextCompat.getColor(context, R.color.explorerIconTint)) })
                    .into(binding.explorerIcon)

                Glide.with(context)
                    .asBitmap()
                    .load(item.appItem?.icon?.toBitmap()?.fillTransparent()?.applyBorder(10F, ContextCompat.getColor(context, R.color.explorerFileDefaultColor)))
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.explorerAppIcon)
            }
            fileIsApk -> {
                GlobalScope.launch(Dispatchers.IO) {
                    val icon = file.getAPKImage(context)
                    withContext(Dispatchers.Main) {
                        Glide.with(context)
                            .load(icon)
                            .fallback(fallback)
                            .placeholder(fallback)
                            .error(fallback)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.explorerIcon)
                    }
                }
            }
            else -> {
                Glide.with(context)
                    .load(file.getUri())
                    .fallback(fallback)
                    .placeholder(fallback)
                    .error(fallback)
                    .into(binding.explorerIcon)
            }
        }

        binding.explorerName.text = fileName
        if (item.selectable) {
            binding.explorerCheckbox.visibility = View.VISIBLE
            binding.explorerCheckbox.setOnClickListener { longClickListener?.invoke(it, position) }
        } else {
            binding.explorerCheckbox.visibility = View.INVISIBLE
            binding.explorerCheckbox.isChecked = false
            binding.explorerCheckbox.isClickable = false
            binding.explorerCheckbox.isFocusable = false
        }
        binding.explorerCheckbox.isChecked = item.selected
    }

    fun submitList(list: List<ExplorerItem>) = differ.submitList(list)

    fun addToList(item: ExplorerItem) = submitList(differ.currentList.mutableCopyOf().apply { add(item) })

}