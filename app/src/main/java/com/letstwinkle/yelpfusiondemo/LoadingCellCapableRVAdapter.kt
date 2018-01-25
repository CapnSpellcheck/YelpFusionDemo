package com.letstwinkle.yelpfusiondemo

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Another excerpt from my personal codebase. Hence, a bit terse.
 * A bit more detail: I adapted this from a Java pattern that I researched for my own app.
 */
abstract class LoadingCellCapableRVAdapter : RecyclerView.Adapter<DataBindingViewHolder>(),
    RVScrollToBottomListener.LoadingAware
{
    val handler = Handler(Looper.getMainLooper())
    override var loading = false
        set(value) {
            if (field != value) {
                // change field first, this allows subclass to have the change related to loading
                field = value
                if (value) {
                    notifyItemInserted(this.itemCount)
                } else {
                    notifyItemRemoved(this.itemCount - 1)
                }
            }
        }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.recycledViewPool.setMaxRecycledViews(Companion.loadingCellType, 0)
    }

    final override fun getItemViewType(position: Int): Int =
        if (loading && position == this.itemCount - 1) Companion.loadingCellType else getMyItemViewType(position)

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
        if (viewType == Companion.loadingCellType) {
            val cellLoadingView = LayoutInflater.from(parent.context).inflate(R.layout.rv_loading_cell, parent, false)
            return DataBindingViewHolder(cellLoadingView)
        }
        return onMyCreateViewHolder(parent, viewType)
    }

    final override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
        if (holder.itemViewType == Companion.loadingCellType)
            return
        onMyBindViewHolder(holder, position)
    }

    // default impl uses one item type
    open fun getMyItemViewType(position: Int): Int = 0

    abstract fun onMyCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder

    abstract fun onMyBindViewHolder(holder: DataBindingViewHolder, position: Int)

    companion object {
        val loadingCellType: Int = R.layout.rv_loading_cell
    }
}