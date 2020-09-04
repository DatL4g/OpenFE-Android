package de.datlag.openfe.recycler.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import de.datlag.openfe.R
import de.datlag.openfe.databinding.LocationItemBinding
import de.datlag.openfe.interfaces.RecyclerAdapterItemClickListener
import de.datlag.openfe.recycler.data.LocationItem
import de.datlag.openfe.util.toHumanReadable
import kotlinx.android.extensions.LayoutContainer

class LocationRecyclerAdapter(private val list: List<LocationItem>) : RecyclerView.Adapter<LocationRecyclerAdapter.ViewHolder>() {

    var clickListener: RecyclerAdapterItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, LayoutContainer {

        override val containerView: View?
            get() = itemView

        val binding = LocationItemBinding.bind(containerView ?: itemView)

        init {
            binding.locationCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(v ?: containerView ?: itemView, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder) {
        binding.locationName.text = list[position].name
        setUsage(binding, list[position])
    }

    private fun setUsage(binding: LocationItemBinding, item: LocationItem) {
        val itemUsage = item.usage
        binding.locationProgress.progress = itemUsage.percentage
        binding.locationProgressText.text = "${itemUsage.percentage.toInt()}%"
        binding.locationUsage.text = "${itemUsage.current.toHumanReadable()} used / ${itemUsage.max.toHumanReadable()}"
    }

}