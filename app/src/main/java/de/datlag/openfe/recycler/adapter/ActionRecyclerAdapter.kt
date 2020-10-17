package de.datlag.openfe.recycler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.databinding.ActionItemBinding
import de.datlag.openfe.extend.ClickRecyclerAdapter
import de.datlag.openfe.recycler.data.ActionItem
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.extensions.LayoutContainer

@Obfuscate
class ActionRecyclerAdapter : ClickRecyclerAdapter<ActionRecyclerAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<ActionItem>() {
        override fun areItemsTheSame(oldItem: ActionItem, newItem: ActionItem): Boolean {
            return oldItem.actionId == newItem.actionId
        }

        override fun areContentsTheSame(oldItem: ActionItem, newItem: ActionItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = ActionItemBinding.bind(containerView ?: itemView)

        init {
            binding.actionCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(v ?: containerView ?: itemView, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.action_item, parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        val item = differ.currentList[position]

        binding.actionIcon.setImageDrawable(item.icon)
        binding.actionName.text = item.name
    }

    fun submitList(list: List<ActionItem>) = differ.submitList(list)
}
