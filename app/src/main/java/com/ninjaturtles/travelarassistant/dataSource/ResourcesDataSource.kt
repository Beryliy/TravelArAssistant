package com.ninjaturtles.travelarassistant.dataSource

import androidx.annotation.StringRes

interface ResourcesDataSource {
    fun getString(@StringRes resId: Int, vararg params: Any): String
}