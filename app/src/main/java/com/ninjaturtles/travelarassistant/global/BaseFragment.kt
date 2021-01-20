package com.ninjaturtles.travelarassistant.global

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {
    @get:LayoutRes protected abstract val layoutResourceId: Int
}