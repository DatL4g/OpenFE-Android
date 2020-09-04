package de.datlag.openfe.recycler.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.databinding.AppsActionItemBinding
import de.datlag.openfe.interfaces.RecyclerAdapterItemClickListener
import de.datlag.openfe.recycler.data.AppItem
import kotlinx.android.extensions.LayoutContainer

class AppsActionRecyclerAdapter(private var list: MutableList<AppItem>) : RecyclerView.Adapter<AppsActionRecyclerAdapter.ViewHolder>() {

    var clickListener: RecyclerAdapterItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = AppsActionItemBinding.bind(containerView ?: itemView)

        init {
            binding.appsActionCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(v ?: containerView ?: itemView, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.apps_action_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        binding.appIcon.setImageDrawable(list[position].icon)
        binding.appName.text = list[position].name
    }

    fun updateData(data: List<AppItem>) {
        list = data.toMutableList()
        notifyDataSetChanged()
    }

    fun addData(data: AppItem) {
        list.add(data)
        notifyItemInserted(list.size-1)
    }

    fun getData(): List<AppItem> {
        return list
    }

    fun clearData() {
        list = mutableListOf()
        notifyDataSetChanged()
    }

}