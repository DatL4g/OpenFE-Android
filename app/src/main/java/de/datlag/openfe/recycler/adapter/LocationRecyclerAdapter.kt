package de.datlag.openfe.recycler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.databinding.RecyclerLocationItemBinding
import de.datlag.openfe.extend.ClickRecyclerAdapter
import de.datlag.openfe.recycler.data.LocationItem
import de.datlag.openfe.util.toHumanReadable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.extensions.LayoutContainer

@Obfuscate
class LocationRecyclerAdapter : ClickRecyclerAdapter<LocationRecyclerAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<LocationItem>() {
        override fun areItemsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = RecyclerLocationItemBinding.bind(containerView ?: itemView)

        init {
            binding.locationCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.invoke(v ?: containerView ?: itemView, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_location_item, parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        val item = differ.currentList[position]

        binding.locationName.text = item.name
        setUsage(binding, item)
    }

    private fun setUsage(binding: RecyclerLocationItemBinding, item: LocationItem) {
        val itemUsage = item.usage

        binding.locationProgress.progress = itemUsage.percentage
        binding.locationProgressText.text = "${itemUsage.percentage.toInt()}%"
        binding.locationUsage.text = "${itemUsage.current.toHumanReadable()} used / ${itemUsage.max.toHumanReadable()}"
    }

    fun submitList(list: List<LocationItem>) = differ.submitList(list)
}
