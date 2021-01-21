package com.ninjaturtles.travelarassistant.global

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("visible")
fun View.bindVisible(visible: Boolean) {
    this.visibility = if(visible) View.VISIBLE else View.GONE
}