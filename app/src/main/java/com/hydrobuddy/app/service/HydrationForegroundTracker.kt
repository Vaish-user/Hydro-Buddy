package com.hydrobuddy.app.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global singleton tracking whether HydroBuddy is in the foreground,
 * and routing incoming reminder triggers directly to the active UI.
 */
object HydrationForegroundTracker {
    @Volatile
    var isAppInForeground: Boolean = false

    private val _foregroundTriggerEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val foregroundTriggerEvents: SharedFlow<Unit> = _foregroundTriggerEvents.asSharedFlow()

    fun notifyReminderTriggered(): Boolean {
        return if (isAppInForeground) {
            _foregroundTriggerEvents.tryEmit(Unit)
            true
        } else {
            false
        }
    }
}
