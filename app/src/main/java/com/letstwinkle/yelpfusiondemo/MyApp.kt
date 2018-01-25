package com.letstwinkle.yelpfusiondemo

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import com.nostra13.universalimageloader.core.*
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import java.io.File

class MyApp : Application() {
    lateinit var displayImageOpts: DisplayImageOptions
    lateinit var requestQueue: RequestQueue

    override fun onCreate() {
        super.onCreate()
        instance = this

        val stack = HurlStack()
        val cacheDir = File(this.cacheDir, "volley")
        val network = BasicNetwork(stack)
        requestQueue = RequestQueue(DiskBasedCache(cacheDir), network)
        requestQueue.start()

        // ImageLoader
        val ilConfig = ImageLoaderConfiguration.Builder(this)
        if (BuildConfig.DEBUG) {
            ilConfig.writeDebugLogs()
        }
        val displayImageOptsBuilder = DisplayImageOptions.Builder()
        displayImageOptsBuilder.cacheInMemory(true).cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .considerExifParams(true)
            .resetViewBeforeLoading(true)
            .displayer(FadeInBitmapDisplayer(200))
        displayImageOpts = displayImageOptsBuilder.build()
        ilConfig.defaultDisplayImageOptions(displayImageOpts)
        ImageLoader.getInstance().init(ilConfig.build())
    }

    companion object {
        lateinit var instance: MyApp
    }
}