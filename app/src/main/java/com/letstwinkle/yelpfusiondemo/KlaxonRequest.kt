package com.letstwinkle.yelpfusiondemo

import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser

/**
 * From my code library. I just made a kludge for the Authorization header. (Sorry, I'm focusing on
 * impressing in other ways ;) )
 */
abstract class KlaxonBaseRequest<T> : Request<T> {
    var paramz: Map<String, String>? = null
    var contentType: String? = null

    protected val listener: ResponseHandler<T>

    constructor(method: Int, url: String, listener: ResponseHandler<T>) : super(method, url, listener) {
        this.listener = listener
    }

    override fun getParams(): Map<String, String>? {
        return paramz
    }

    override fun deliverResponse(response: T) {
        listener.onResponse(response)
    }

    override fun getBodyContentType(): String {
        contentType?.let {
            return it
        }
        return super.getBodyContentType()
    }
}

open class KlaxonRequest(method: Int, url: String, listener: ResponseHandler<JsonObject>) :
    KlaxonBaseRequest<JsonObject>(method, url, listener)
{

    override fun parseNetworkResponse(response: NetworkResponse): Response<JsonObject> {
        try {
            val jsonstr = String(response.data)
            val cacheEntry = HttpHeaderParser.parseCacheHeaders(response)
            if (BuildConfig.DEBUG) {
                Log.d(FusionAPI.tag, "response [$url]: $jsonstr")
                Log.d(FusionAPI.tag, "Headers: " + response.headers)
            }
            return Response.success(parser.parse(StringBuilder(jsonstr)) as JsonObject, cacheEntry)
        } catch (ex: Exception) {
            Log.e(FusionAPI.tag, "JSON parse error")
            return Response.error(ParseError(ex))
        }
    }
    
    override fun getHeaders(): MutableMap<String, String> {
        // since we always need the Auth header with this API, I just kludged it here to always set it.
        return mutableMapOf("Accept" to "application/json",
                            "Authorization" to "Bearer ZLULOWJQUGxBYyGJKCyfUQ-apFk9K4ruRe0IFxSrHRNbmWAq-MJXPtQdOyin_Z3jhU5Cd0TsxSNPy7DZg6pdlJqVYYrFjVB0ZPpU3wZAdSXsJtPdD1Nf1TDUgN9nWnYx"
        )
    }

    var parsedNetworkError: String = ""
    override fun parseNetworkError(error: VolleyError): VolleyError {
        try {
            val str = String(error.networkResponse!!.data)
            Log.e(FusionAPI.tag, "parseNetworkError: data=$str")
            parsedNetworkError = str
        } catch (ex: Exception) {}
        return super.parseNetworkError(error)
    }

    companion object {
        val parser: Parser = Parser()
    }
}
