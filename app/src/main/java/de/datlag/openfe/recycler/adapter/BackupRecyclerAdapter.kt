package de.datlag.openfe.recycler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.commons.getIcon
import de.datlag.openfe.databinding.RecyclerBackupItemBinding
import de.datlag.openfe.db.Backup
import de.datlag.openfe.extend.ClickRecyclerAdapter
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.extensions.LayoutContainer
import java.io.File
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class BackupRecyclerAdapter : ClickRecyclerAdapter<BackupRecyclerAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Backup>() {
        override fun areItemsTheSame(oldItem: Backup, newItem: Backup): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Backup, newItem: Backup): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener,
        LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = RecyclerBackupItemBinding.bind(containerView ?: itemView)

        init {
            binding.backupCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(v ?: containerView ?: itemView, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_backup_item, parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        val item = differ.currentList[position]
        val context = holder.containerView?.context ?: holder.itemView.context
        val file = File(item.directoryPath)

        binding.backupName.text = file.name
        binding.backupIcon.setImageDrawable(null)

        file.getIcon(context, Pair(first = false, second = false), R.color.coloredIconTint) {
            binding.backupIcon.setImageDrawable(it)
        }
    }

    fun submitList(list: List<Backup>) = differ.submitList(list)
}
