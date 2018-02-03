package com.letstwinkle.yelpfusiondemo

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
import android.graphics.Point
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import com.android.volley.VolleyError
import com.letstwinkle.yelpfusiondemo.databinding.RvSearchresultBinding
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader

private const val tag_ = "MainActivity"

class MainActivity : Activity(), SearchResultActions {
    lateinit var resultsGrid: RecyclerView
    var currentQuery: String = ""
    var totalResults: Int = 0
    lateinit var scrollListener: RVScrollToBottomListener
    lateinit var searchView: SearchView

    inline fun getAdapter(): SearchAdapter = resultsGrid.adapter as SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultsGrid = this.findViewById(R.id.resultsGrid)
        resultsGrid.adapter = SearchAdapter()
        resultsGrid.setHasFixedSize(true)

        val layoutManager = resultsGrid.layoutManager as GridLayoutManager
        // span size lookup for the loading cell indicator -- should be on its own row.
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (getAdapter().getItemViewType(position) == LoadingCellCapableRVAdapter.loadingCellType)
                    return layoutManager.spanCount
                return 1
            }
        }

        scrollListener = RVScrollToBottomListener(resultsGrid.layoutManager, getAdapter()) {
            val currentCount = getAdapter().itemCount
            if (currentCount < totalResults && currentCount < FusionAPI.MAX_RESULTS) {
                loadMore()
                true
            } else false
        }
        resultsGrid.addOnScrollListener(scrollListener)
    }

    override fun onNewIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            currentQuery = intent.getStringExtra(SearchManager.QUERY)
            startSearch()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.menu_main, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        // it seems surprisingly difficult to locate the SearchView in the action bar later…
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    // show the biz photo in a lightbox-ish thing. basically just a dialog.
    override fun businessClicked(biz: Business) {
        // utilize the custom view of the dialog. Create an ImageView from scratch real quick.
        val iv = ImageView(this)
        val screenSize = Point()
        this.windowManager.defaultDisplay.getSize(screenSize)
        // I tried constraining the ImageView size further, but it doesn't work due to the way
        // dialogs render -- some deeper digging would be necessary
//        iv.maxWidth = convertDpToPixel(screenSize.x - 80f, this).toInt()
//        iv.minimumHeight = convertDpToPixel(0.5f*screenSize.y, this).toInt()
//        iv.maxHeight = convertDpToPixel(screenSize.y - 150f, this).toInt()
        iv.adjustViewBounds = true
        iv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                 ViewGroup.LayoutParams.WRAP_CONTENT)
        ImageLoader.getInstance().displayImage(biz.mainImageURL, iv, MyApp.instance.displayImageOpts)

        val dfrag = SimpleDialogFragment()
        dfrag.vieww = iv

        dfrag.show(this.fragmentManager, "bizphoto")

        // if Image is clicked, just close it - so tapping anywhere on screen closes.
        iv.setOnClickListener {
            dfrag.dismiss()
        }
    }

    private fun startSearch() {
        val suggestions = SearchRecentSuggestions(this, RecentSearchProvider.AUTHORITY, DATABASE_MODE_QUERIES)
        suggestions.saveRecentQuery(currentQuery, null)
        // it would be nice to only set the query if it was selected from the history, but it seems like
        // the simplest way to do that is to use OnSuggestionListener. I could hook that up, but
        // I'm at the point of "being done". Anyway, when you setQuery, the suggestion list seems to pop up.
//        searchView.setQuery(currentQuery, false)
        getAdapter().removeAll()
        loadMore()
        val progressBar: View = findViewById(R.id.loadingProgress)
        progressBar.visibility = View.VISIBLE
    }

    private val responseHandler = object : ResponseHandler<SearchResponse> {
        override fun onResponse(response: SearchResponse) {
            Log.d(tag_, "Total results: ${response.total}")
            // we have a new total results count, so update it
            totalResults = response.total
            getAdapter().append(response.entries)
            finishRegardless()
        }

        override fun onErrorResponse(error: VolleyError) {
            val dfrag = SimpleDialogFragment()
            dfrag.title = R.string.error_dlog_title
            dfrag.message = R.string.error_dlog_body
            dfrag.showAllowingStateLoss(this@MainActivity.fragmentManager, "error")
            finishRegardless()
        }

        fun finishRegardless() {
            scrollListener.loadingFinished()
            val progressBar: View = findViewById(R.id.loadingProgress)
            progressBar.visibility = View.GONE
        }
    }

    // hardcoding the location to Manhattan NY, because there should be results for just about anything there
    private fun loadMore() {
        val offset = getAdapter().itemCount
        FusionAPI.getBusinesses(currentQuery, "10001", offset, responseHandler)
    }

    inner class SearchAdapter : LoadingCellCapableRVAdapter() {
        private val results: MutableList<Business> = mutableListOf()

        fun removeAll() {
            val removed = results.size
            results.clear()
            notifyItemRangeRemoved(0, removed)
        }

        fun append(businesses: List<Business>) {
            val oldCount = results.size
            results.addAll(businesses)
            notifyItemRangeInserted(oldCount + 1, businesses.size)
        }

        override fun onMyCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
            val binding = RvSearchresultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.actions = this@MainActivity
            return DataBindingViewHolder(binding)
        }

        override fun onMyBindViewHolder(holder: DataBindingViewHolder, position: Int) {
            val binding = holder.binding as RvSearchresultBinding
            binding.business = results[position]
        }

        override fun getItemCount(): Int = results.size + if (this.loading) 1 else 0
    }
}
