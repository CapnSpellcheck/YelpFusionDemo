package com.letstwinkle.yelpfusiondemo

import android.databinding.BindingAdapter
import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.*

@BindingAdapter("app:srcUri")
fun bindSrcURI(iv: ImageView, src: String?) {
    if (src == null)
        return
    ImageLoader.getInstance().displayImage(src, iv, MyApp.instance.displayImageOpts)
}
