package de.datlag.openfe.recycler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.commons.getFolderIcon
import de.datlag.openfe.commons.getIcon
import de.datlag.openfe.commons.invisible
import de.datlag.openfe.commons.isAPK
import de.datlag.openfe.commons.show
import de.datlag.openfe.databinding.ExplorerItemBinding
import de.datlag.openfe.extend.ClickRecyclerAdapter
import de.datlag.openfe.recycler.data.ExplorerItem
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.CoroutineScope
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
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

        // this fixes random icons for files of previous folders
        binding.explorerIcon.setImageDrawable(null)
        binding.explorerAppIcon.setImageDrawable(null)

        file.getIcon(context, Pair(true, fileIsApk), R.color.coloredIconTint) {
            binding.explorerIcon.setImageDrawable(it)
        }

        item.appItem?.getFolderIcon(context, R.color.explorerCardDefaultColor) {
            binding.explorerAppIcon.setImageDrawable(it)
        }

        binding.explorerName.text = fileName
        if (item.selectable) {
            binding.explorerCheckbox.show()
        } else {
            binding.explorerCheckbox.invisible()
            binding.explorerCheckbox.isChecked = false
        }
        binding.explorerCheckbox.isChecked = item.selected
    }

    fun submitList(list: List<ExplorerItem>) = differ.submitList(list)
}
