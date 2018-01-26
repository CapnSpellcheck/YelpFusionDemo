package com.letstwinkle.yelpfusiondemo

import android.content.Context
import android.content.res.Resources
import android.util.ArrayMap
import android.util.DisplayMetrics

fun <K, V> arrayMapOf(vararg pairs: Pair<K, V>): ArrayMap<K, V>
    = ArrayMap<K, V>(pairs.size).apply { putAll(pairs) }

fun convertDpToPixel(dp: Float, context: Context): Float {
    val resources = context.resources
    return convertDpToPixel(dp, resources)
}
fun convertDpToPixel(dp: Float, resources: Resources): Float {
    val metrics = resources.displayMetrics
    val d: Float = dp * metrics.densityDpi
    return d / DisplayMetrics.DENSITY_DEFAULT
}