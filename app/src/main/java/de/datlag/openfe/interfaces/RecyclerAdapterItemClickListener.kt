package de.datlag.openfe.interfaces

import android.view.View

fun interface RecyclerAdapterItemClickListener {
    fun onClick(view: View, position: Int)
}