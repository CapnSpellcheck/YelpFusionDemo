package com.letstwinkle.yelpfusiondemo

import android.app.Activity
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.volley.VolleyError
import com.letstwinkle.yelpfusiondemo.databinding.RvSearchresultBinding

class MainActivity : Activity() {
    lateinit var resultsGrid: RecyclerView

    inline fun getAdapter(): SearchAdapter = resultsGrid.adapter as SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultsGrid = this.findViewById(R.id.resultsGrid)
        resultsGrid.adapter = SearchAdapter()
    }

    override fun onStart() {
        super.onStart()

        loadMore()
    }

    private fun loadMore() {
        val offset = getAdapter().itemCount
        FusionAPI.getBusinesses("bananas", "austin tx", offset, object : ResponseHandler<SearchResponse> {
            override fun onResponse(response: SearchResponse) {
                getAdapter().append(response.entries)
            }

            override fun onErrorResponse(error: VolleyError) {
                val dfrag = SimpleDialogFragment()
                dfrag.title = R.string.error_dlog_title
                dfrag.message = R.string.error_dlog_body
                dfrag.showAllowingStateLoss(this@MainActivity.fragmentManager, "error")
            }
        })
    }

    class SearchAdapter : RecyclerView.Adapter<DataBindingViewHolder>() {
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder {
            val binding = RvSearchresultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DataBindingViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
            val binding = holder.binding as RvSearchresultBinding
            binding.business = results[position]
        }

        override fun getItemCount(): Int = results.size
    }
}
