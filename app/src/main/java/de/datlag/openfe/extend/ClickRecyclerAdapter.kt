package de.datlag.openfe.extend

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ClickRecyclerAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    protected var clickListener: ((view: View, position: Int) -> Unit)? = null
    protected var longClickListener: ((view: View, position: Int) -> Boolean)? = null

    fun setOnClickListener(listener: ((view: View, position: Int) -> Unit)) {
        clickListener = listener
    }

    fun setOnLongClickListener(listener: ((view: View, position: Int) -> Boolean)) {
        longClickListener = listener
    }

}