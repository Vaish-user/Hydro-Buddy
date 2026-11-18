package com.hydrobuddy.app.ui.overlay

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Custom FrameLayout for the TYPE_APPLICATION_OVERLAY window.
 *
 * Filters touch events so that only touches directly inside Pip's
 * interactive bounds (mascot + speech bubble) are consumed.
 * Touches everywhere else return false, seamlessly passing through
 * to Instagram, YouTube, WhatsApp, or the Android launcher.
 */
class OverlayTouchFilterLayout(context: Context) : FrameLayout(context) {

    private val touchableBounds = mutableListOf<Rect>()

    fun setInteractiveBounds(boundsList: List<Rect>) {
        touchableBounds.clear()
        touchableBounds.addAll(boundsList)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (touchableBounds.isEmpty()) {
            return false
        }

        val x = ev.x.toInt()
        val y = ev.y.toInt()

        val isInsideInteractiveArea = touchableBounds.any { rect ->
            rect.contains(x, y)
        }

        return if (isInsideInteractiveArea) {
            super.dispatchTouchEvent(ev)
        } else {
            // Touch falls on empty space around Pip -> pass through to underlying app!
            false
        }
    }
}
