package com.hydrobuddy.app.ui.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.data.model.AppearanceSettings
import com.hydrobuddy.app.data.model.CharacterCustomization
import com.hydrobuddy.app.service.FloatingPipService
import com.hydrobuddy.app.ui.theme.HydroBuddyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Singleton manager controlling the floating Pip WindowManager overlay.
 *
 * Sequence:
 * 1. Checks Settings.canDrawOverlays(context).
 * 2. Creates TYPE_APPLICATION_OVERLAY window directly via WindowManager.
 * 3. Mounts FloatingPipView with an isolated OverlayLifecycleOwner.
 * 4. Satisfies Android 15 order-of-operations (visible overlay window exists
 *    BEFORE launching the foreground service under the SYSTEM_ALERT_WINDOW exemption).
 * 5. Safely cleans up upon Pip walking out or user dismissal.
 */
object FloatingPipOverlayManager {

    private const val TAG = "FloatingPipManager"

    @Volatile
    private var isOverlayShowing = false

    private var activeFilterLayout: OverlayTouchFilterLayout? = null
    private var activeLifecycleOwner: OverlayLifecycleOwner? = null

    val isShowing: Boolean
        get() = isOverlayShowing

    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun showFloatingOverlay(context: Context): Boolean {
        if (!canDrawOverlays(context)) {
            Log.w(TAG, "Cannot show floating Pip: SYSTEM_ALERT_WINDOW permission missing")
            return false
        }

        if (isOverlayShowing) {
            Log.d(TAG, "Floating Pip overlay is already active; ignoring duplicate trigger")
            return true
        }

        Handler(Looper.getMainLooper()).post {
            try {
                val appContext = context.applicationContext
                val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                if (windowManager == null) {
                    Log.e(TAG, "WindowManager service unavailable")
                    return@post
                }

                val lifecycleOwner = OverlayLifecycleOwner()
                activeLifecycleOwner = lifecycleOwner

                val filterLayout = OverlayTouchFilterLayout(appContext)
                activeFilterLayout = filterLayout
                lifecycleOwner.attachToView(filterLayout)

                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                val dataStore = HydroDataStore(appContext)

                val composeView = ComposeView(appContext).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                    lifecycleOwner.attachToView(this)

                    setContent {
                        HydroBuddyTheme {
                            var customization by remember { mutableStateOf(CharacterCustomization()) }
                            var appearance by remember { mutableStateOf(AppearanceSettings()) }

                            LaunchedEffect(Unit) {
                                customization = dataStore.characterCustomizationFlow.first()
                                appearance = dataStore.appearanceSettingsFlow.first()
                            }

                            FloatingPipView(
                                characterCustomization = customization,
                                appearanceSettings = appearance,
                                dataStore = dataStore,
                                onBoundsChanged = { rects ->
                                    filterLayout.setInteractiveBounds(rects)
                                },
                                onDismiss = {
                                    dismissFloatingOverlay(appContext)
                                }
                            )
                        }
                    }
                }

                filterLayout.addView(composeView)
                lifecycleOwner.onResume()

                // Step 1: Add visible TYPE_APPLICATION_OVERLAY window
                windowManager.addView(filterLayout, layoutParams)
                isOverlayShowing = true
                Log.d(TAG, "Floating Pip TYPE_APPLICATION_OVERLAY window successfully added to WindowManager")

                // Step 2: Now that a visible overlay window exists, start foreground service (Android 15 compliant)
                try {
                    val serviceIntent = Intent(appContext, FloatingPipService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appContext.startForegroundService(serviceIntent)
                    } else {
                        appContext.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Optional foreground service startup note: ${e.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to display floating overlay", e)
                isOverlayShowing = false
                activeFilterLayout = null
                activeLifecycleOwner = null
            }
        }

        return true
    }

    fun dismissFloatingOverlay(context: Context) {
        Handler(Looper.getMainLooper()).post {
            try {
                val filterLayout = activeFilterLayout
                val lifecycleOwner = activeLifecycleOwner

                if (filterLayout != null && isOverlayShowing) {
                    val appContext = context.applicationContext
                    val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                    windowManager?.removeView(filterLayout)
                    Log.d(TAG, "Floating Pip overlay removed from WindowManager")
                }

                lifecycleOwner?.onDestroy()

                // Stop companion service
                try {
                    val appContext = context.applicationContext
                    appContext.stopService(Intent(appContext, FloatingPipService::class.java))
                } catch (ignored: Exception) {}

            } catch (e: Exception) {
                Log.e(TAG, "Error dismissing floating overlay", e)
            } finally {
                activeFilterLayout = null
                activeLifecycleOwner = null
                isOverlayShowing = false
            }
        }
    }
}
