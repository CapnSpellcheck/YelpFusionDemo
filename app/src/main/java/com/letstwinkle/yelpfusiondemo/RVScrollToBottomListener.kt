package com.letstwinkle.yelpfusiondemo

import android.support.v7.widget.*

/**
 * Another excerpt from my personal codebase. Hence, a bit terse.
 * A bit more detail: I adapted this from a Java pattern that I researched for my own app.
 */
class RVScrollToBottomListener(private val layoutManager: RecyclerView.LayoutManager,
                               private val loadingAware: LoadingAware,
                               handler: () -> Boolean) :
    RecyclerView.OnScrollListener()
{
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private var visibleThreshold = 1
    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0
    // True if we are still waiting for the last set of data to load.
    private var loading = true
        set(value) {
            field = value
            loadingAware.loading = value
        }
    // Takes no parameters and returns true iff loading more.
    private val scrollToBottomHandler: () -> Boolean = handler

    fun loadingFinished() {
        loading = false
    }

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        if (dy <= 0) return
        if (loading) return

        var lastVisibleItemPosition = 0
        val totalItemCount = layoutManager.itemCount

        if (layoutManager is StaggeredGridLayoutManager) {
            val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
            // get maximum element within the list
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
        } else if (layoutManager is LinearLayoutManager) {
            lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && lastVisibleItemPosition + visibleThreshold >= totalItemCount) {
//            Log.d("RVScrollToBottomListene", "triggered")
            loading = scrollToBottomHandler()
        }
    }

    // Call this method whenever performing new searches
    fun resetState() {
        this.previousTotalItemCount = 0
        this.loading = true
    }

    init {
        if (layoutManager is GridLayoutManager)
            visibleThreshold *= layoutManager.spanCount
        else if (layoutManager is StaggeredGridLayoutManager)
            visibleThreshold *= layoutManager.spanCount
    }

    interface LoadingAware {
        var loading: Boolean
    }
}
