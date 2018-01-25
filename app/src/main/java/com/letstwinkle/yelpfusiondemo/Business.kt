package com.letstwinkle.yelpfusiondemo

import com.beust.klaxon.*

class Business {
    val mainImageURL: String?
    val name: String?

    constructor(json: JsonObject) {
        mainImageURL = json.string("image_url")
        name = json.string("name")
    }
}