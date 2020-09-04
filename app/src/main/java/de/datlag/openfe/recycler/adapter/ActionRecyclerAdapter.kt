package de.datlag.openfe.recycler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.databinding.ActionItemBinding
import de.datlag.openfe.interfaces.RecyclerAdapterItemClickListener
import de.datlag.openfe.recycler.data.ActionItem
import kotlinx.android.extensions.LayoutContainer

class ActionRecyclerAdapter(private val list: List<ActionItem>) : RecyclerView.Adapter<ActionRecyclerAdapter.ViewHolder>() {

    var clickListener: RecyclerAdapterItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = ActionItemBinding.bind(containerView ?: itemView)

        init {
            binding.actionCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(v ?: containerView ?: itemView, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.action_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        binding.actionIcon.setImageDrawable(list[position].icon)
        binding.actionName.text = list[position].name
    }

}