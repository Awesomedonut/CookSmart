package com.example.cooksmart.utils

import android.view.View

class DebouncedOnClickListener(private val interval: Long, private val doClick: (View) -> Unit) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(view: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= interval) {
            lastClickTime = currentTime
            doClick(view)
        }
    }

    companion object {
        fun setDebouncedOnClickListener(view: View, interval: Long, doClick: (View) -> Unit) {
            val listener = DebouncedOnClickListener(interval, doClick)
            view.setOnClickListener(listener)
        }
    }
}
