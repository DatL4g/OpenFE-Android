package de.datlag.openfe.recycler.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import de.datlag.openfe.R
import de.datlag.openfe.recycler.data.AppItem

class AppsActionRecyclerAdapter(context: Context, private var list: MutableList<AppItem>) : RecyclerView.Adapter<AppsActionRecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private var clickListener: ItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val cardView: CardView = itemView.findViewById(R.id.appsActionCard)
        val iconView: AppCompatImageView = itemView.findViewById(R.id.appIcon)
        val nameView: AppCompatTextView = itemView.findViewById(R.id.appName)

        init {
            cardView.setOnClickListener(this)
            cardView.setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus) {
                    clickListener?.onClick(v ?: itemView, adapterPosition)
                }
            }
        }

        override fun onClick(v: View?) {
            clickListener?.onClick(v ?: itemView, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.apps_action_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.iconView.setImageDrawable(list[position].icon)
        holder.nameView.text = list[position].name
    }

    fun setClickListener(listener: ItemClickListener?) {
        clickListener = listener
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int)
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