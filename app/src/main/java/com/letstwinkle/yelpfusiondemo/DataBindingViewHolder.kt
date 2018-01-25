package com.letstwinkle.yelpfusiondemo

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * Another one of my 'code library' things.
 */
class DataBindingViewHolder : RecyclerView.ViewHolder {
    val binding: ViewDataBinding?

    constructor(binding: ViewDataBinding) : super(binding.root) {
        this.binding = binding
    }

}