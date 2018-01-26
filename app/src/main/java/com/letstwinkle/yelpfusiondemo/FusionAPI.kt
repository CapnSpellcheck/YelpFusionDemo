package com.letstwinkle.yelpfusiondemo

import android.net.Uri
import com.android.volley.Request
import com.android.volley.VolleyError
import com.beust.klaxon.*

object FusionAPI {
    const val tag = "FusionAPI"
    const val MAX_RESULTS = 1000

    fun getBusinesses(query: String, location: String, offset: Int,
                      handler: ResponseHandler<SearchResponse>): KlaxonRequest
    {
        val params = arrayMapOf("term" to query, "location" to location, "offset" to offset)
        val url = buildURL("/businesses/search", params)
        val req = KlaxonRequest(Request.Method.GET, url, SearchResponseAdapter(handler, offset))
        
        MyApp.instance.requestQueue.add(req)
        return req
    }

    private const val serviceAuthority = "api.yelp.com"

    private fun buildURL(path: String, queryParams: Map<String, Any> = arrayMapOf()): String {
        val builder = Uri.Builder()
            .scheme("https").encodedAuthority(serviceAuthority).path("/v3$path")
        for ((key, value) in queryParams) {
            builder.appendQueryParameter(key, value.toString())
        }
        return builder.build().toString()
    }
}

abstract private class AbstractResponseAdapter<Model>(val respHandler: ResponseHandler<Model>) :
    ResponseHandler<JsonObject>
{
    abstract fun adapt(obj: JsonObject) : Model
    override fun onResponse(response: JsonObject) {
        respHandler.onResponse(adapt(response))
    }
    override fun onErrorResponse(error: VolleyError) {
        respHandler.onErrorResponse(error)
    }
}

private class SearchResponseAdapter(rh: ResponseHandler<SearchResponse>, val offset: Int)
    : AbstractResponseAdapter<SearchResponse>(rh)
{
    override fun adapt(obj: JsonObject): SearchResponse {
        val businessArray: List<JsonObject>? = obj.array("businesses")
        val businesses = businessArray?.map { obj -> Business(obj) } ?: emptyList()
        val total = obj.int("total") ?: 0

        return SearchResponse(offset, total, businesses)
    }
}

data class SearchResponse(val offset: Int, val total: Int, val entries: List<Business>)