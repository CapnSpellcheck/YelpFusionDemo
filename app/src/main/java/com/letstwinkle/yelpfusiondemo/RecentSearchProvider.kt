package com.letstwinkle.yelpfusiondemo

import android.content.SearchRecentSuggestionsProvider

class RecentSearchProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, DATABASE_MODE_QUERIES)
    }
    companion object {
        const val AUTHORITY = "com.letstwinkle.yelpfusiondemo.RecentSearches"
    }
}