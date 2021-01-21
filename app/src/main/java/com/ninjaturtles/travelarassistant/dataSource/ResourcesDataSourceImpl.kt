package com.ninjaturtles.travelarassistant.dataSource

import android.content.res.Resources
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesDataSourceImpl @Inject constructor(
    private val resources: Resources
): ResourcesDataSource {
    override fun getString(resId: Int, vararg params: Any) = resources.getString(resId, *params)
}