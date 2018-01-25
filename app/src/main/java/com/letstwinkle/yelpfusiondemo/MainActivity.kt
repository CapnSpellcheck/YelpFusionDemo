package com.letstwinkle.yelpfusiondemo

import android.app.Activity
import android.app.SearchManager
import android.content.*
import android.content.SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ProgressBar
import android.widget.SearchView
import com.android.volley.VolleyError
import com.letstwinkle.yelpfusiondemo.databinding.RvSearchresultBinding


class MainActivity : Activity() {
    lateinit var resultsGrid: RecyclerView
    var currentQuery: String = ""
    var totalResults: Int = 0
    lateinit var scrollListener: RVScrollToBottomListener

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
            if (getAdapter().itemCount < totalResults) {
                loadMore()
                true
            } else false
        }
        resultsGrid.addOnScrollListener(scrollListener)
    }

    override fun onStart() {
        super.onStart()

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
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    private fun startSearch() {
        val suggestions = SearchRecentSuggestions(this, RecentSearchProvider.AUTHORITY, DATABASE_MODE_QUERIES)
        suggestions.saveRecentQuery(currentQuery, null)
        getAdapter().removeAll()
        loadMore()
        val progressBar: View = findViewById(R.id.loadingProgress)
        progressBar.visibility = View.VISIBLE
    }

    private val responseHandler = object : ResponseHandler<SearchResponse> {
        override fun onResponse(response: SearchResponse) {
            // we have a new total results count, so update it
            totalResults = response.total
            val progressBar: View = findViewById(R.id.loadingProgress)
            progressBar.visibility = View.GONE
            getAdapter().append(response.entries)
            scrollListener.loadingFinished()
        }

        override fun onErrorResponse(error: VolleyError) {
            val dfrag = SimpleDialogFragment()
            dfrag.title = R.string.error_dlog_title
            dfrag.message = R.string.error_dlog_body
            dfrag.showAllowingStateLoss(this@MainActivity.fragmentManager, "error")
            scrollListener.loadingFinished()
        }
    }

    // hardcoding the location to Manhattan NY, because there should be results for just about anything there
    private fun loadMore() {
        val offset = getAdapter().itemCount
        FusionAPI.getBusinesses(currentQuery, "10001", offset, responseHandler)
    }

    class SearchAdapter : LoadingCellCapableRVAdapter() {
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
            return DataBindingViewHolder(binding)
        }

        override fun onMyBindViewHolder(holder: DataBindingViewHolder, position: Int) {
            val binding = holder.binding as RvSearchresultBinding
            binding.business = results[position]
        }

        override fun getItemCount(): Int = results.size
    }
}
