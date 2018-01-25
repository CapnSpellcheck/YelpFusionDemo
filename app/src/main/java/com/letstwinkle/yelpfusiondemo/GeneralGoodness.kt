package com.letstwinkle.yelpfusiondemo

import android.util.ArrayMap

fun <K, V> arrayMapOf(vararg pairs: Pair<K, V>): ArrayMap<K, V>
    = ArrayMap<K, V>(pairs.size).apply { putAll(pairs) }
