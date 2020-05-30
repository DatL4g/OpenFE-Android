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
import de.datlag.openfe.recycler.data.LocationItem
import de.datlag.openfe.util.toHumanReadable

class LocationRecyclerAdapter(context: Context, private val list: List<LocationItem>) : RecyclerView.Adapter<LocationRecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private var clickListener: ItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val card: CardView = itemView.findViewById(R.id.locationCard)
        val progressBar: CircularProgressBar = itemView.findViewById(R.id.locationProgress)
        val progressBarText: AppCompatTextView = itemView.findViewById(R.id.locationProgressText)
        val name: AppCompatTextView = itemView.findViewById(R.id.locationName)
        val usage: AppCompatTextView = itemView.findViewById(R.id.locationUsage)

        init {
            card.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(v ?: itemView, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.location_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = list[position].name
        setUsage(holder, list[position])
    }

    private fun setUsage(holder: ViewHolder, item: LocationItem) {
        val itemUsage = item.usage
        holder.progressBar.progress = itemUsage.percentage
        holder.progressBarText.text = "${itemUsage.percentage.toInt()}%"
        holder.usage.text = "${itemUsage.current.toHumanReadable()} used / ${itemUsage.max.toHumanReadable()}"
    }

    fun setClickListener(listener: ItemClickListener?) {
        clickListener = listener
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int)
    }

}