package com.letstwinkle.yelpfusiondemo

import com.android.volley.Response

interface ResponseHandler<t> : Response.Listener<t>, Response.ErrorListener