package com.ninjaturtles.travelarassistant.dataSource

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

interface ResourcesDataSource {
    fun getString(@StringRes resId: Int, vararg params: Any): String
    fun getColor(@ColorRes resId: Int): Int
}