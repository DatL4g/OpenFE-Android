package de.datlag.openfe.recycler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.databinding.RecyclerAppsActionItemBinding
import de.datlag.openfe.extend.ClickRecyclerAdapter
import de.datlag.openfe.recycler.data.AppItem
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.extensions.LayoutContainer

@Obfuscate
class AppsActionRecyclerAdapter : ClickRecyclerAdapter<AppsActionRecyclerAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<AppItem>() {
        override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = RecyclerAppsActionItemBinding.bind(containerView ?: itemView)

        init {
            binding.appsActionCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(v ?: containerView ?: itemView, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_apps_action_item, parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        val item = differ.currentList[position]

        binding.appIcon.setImageDrawable(item.icon)
        binding.appName.text = item.name
    }

    fun submitList(list: List<AppItem>) = differ.submitList(list)
}
