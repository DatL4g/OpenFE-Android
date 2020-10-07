package de.datlag.openfe.recycler.adapter

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
import de.datlag.openfe.commons.applyBorder
import de.datlag.openfe.commons.fillTransparent
import de.datlag.openfe.commons.getAPKImage
import de.datlag.openfe.commons.isAPK
import de.datlag.openfe.commons.tint
import de.datlag.openfe.commons.toBitmap
import de.datlag.openfe.commons.uri
import de.datlag.openfe.databinding.ExplorerItemBinding
import de.datlag.openfe.extend.ClickRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class ExplorerRecyclerAdapter(private val coroutineScope: CoroutineScope) : ClickRecyclerAdapter<ExplorerRecyclerAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<ExplorerItem>() {
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
        val context = holder.containerView?.context ?: holder.itemView.context
        val fallback = (if (fileIsApk) ContextCompat.getDrawable(context, R.drawable.ic_adb_24dp) else ContextCompat.getDrawable(context, R.drawable.ic_baseline_insert_drive_file_24))?.apply {
            tint(ContextCompat.getColor(context, R.color.explorerIconTint))
        }

        // this fixes random icons for files of previous folders
        binding.explorerIcon.setImageDrawable(null)
        binding.explorerAppIcon.setImageDrawable(null)

        when {
            file.isDirectory -> {
                Glide.with(context)
                    .load(ContextCompat.getDrawable(context, R.drawable.ic_baseline_folder_24)?.apply { tint(ContextCompat.getColor(context, R.color.explorerIconTint)) })
                    .into(binding.explorerIcon)

                Glide.with(context)
                    .asBitmap()
                    .placeholder(null)
                    .load(item.appItem?.icon?.toBitmap()?.fillTransparent()?.applyBorder(10F, ContextCompat.getColor(context, R.color.explorerFileDefaultColor)))
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.explorerAppIcon)
            }
            fileIsApk -> {
                coroutineScope.launch(Dispatchers.IO) {
                    val icon = file.getAPKImage(context, true)
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
                    .load(file.uri)
                    .fallback(fallback)
                    .placeholder(fallback)
                    .error(fallback)
                    .into(binding.explorerIcon)
            }
        }

        binding.explorerName.text = fileName
        if (item.selectable) {
            binding.explorerCheckbox.visibility = View.VISIBLE
        } else {
            binding.explorerCheckbox.visibility = View.INVISIBLE
            binding.explorerCheckbox.isChecked = false
        }
        binding.explorerCheckbox.isChecked = item.selected
    }

    fun submitList(list: List<ExplorerItem>) = differ.submitList(list)
}
