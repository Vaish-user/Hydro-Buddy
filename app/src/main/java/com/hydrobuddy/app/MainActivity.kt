package com.hydrobuddy.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hydrobuddy.app.service.HydrationForegroundTracker
import com.hydrobuddy.app.ui.components.FloatingBottomDock
import com.hydrobuddy.app.ui.components.HydroTopBar
import com.hydrobuddy.app.ui.navigation.HydroNavHost
import com.hydrobuddy.app.ui.navigation.NavRoute
import com.hydrobuddy.app.ui.theme.HydroBuddyTheme
import com.hydrobuddy.app.ui.theme.PureWhiteSurface
import com.hydrobuddy.app.viewmodel.HydroViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import com.hydrobuddy.app.ui.theme.MintSoftBackground

class MainActivity : ComponentActivity() {

    private val viewModel: HydroViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            HydroBuddyTheme(themeMode = themeMode) {
                HydroBuddyApp(viewModel = viewModel)
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("trigger_reminder", false) == true) {
            viewModel.triggerReminderInteraction()
        }
    }

    override fun onStart() {
        super.onStart()
        HydrationForegroundTracker.isAppInForeground = true
    }

    override fun onStop() {
        super.onStop()
        HydrationForegroundTracker.isAppInForeground = false
    }
}

@Composable
fun HydroBuddyApp(viewModel: HydroViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoute.SPLASH

    val reminderSettings by viewModel.reminderSettings.collectAsState()
    val characterCustomization by viewModel.characterCustomization.collectAsState()

    val (screenTitle, screenSubtitle) = when (currentRoute) {
        NavRoute.HOME -> Pair("HydroBuddy", null)
        NavRoute.STATISTICS -> Pair("Statistics", "Hydration rhythm and milestones")
        NavRoute.CUSTOMIZE -> Pair("Pip Wardrobe", "Outfits, accessories and expressions")
        NavRoute.REMINDER_SETTINGS -> Pair("Reminder Schedule", "Intervals and active hours")
        NavRoute.APPEARANCE_SETTINGS -> Pair("Appearance", "Size and animations")
        NavRoute.SETTINGS -> Pair("Settings", "Goals, reminders and personality")
        else -> Pair("HydroBuddy", null)
    }

    var isSplashFinished by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != NavRoute.SPLASH && !isSplashFinished) {
            delay(150)
            isSplashFinished = true
        }
    }

    val showBottomBar = isSplashFinished && currentRoute in listOf(
        NavRoute.HOME,
        NavRoute.STATISTICS,
        NavRoute.CUSTOMIZE,
        NavRoute.SETTINGS,
        NavRoute.REMINDER_SETTINGS,
        NavRoute.APPEARANCE_SETTINGS
    )
    val isSplashScreen = currentRoute == NavRoute.SPLASH
    val isHomeScreen = currentRoute == NavRoute.HOME
    val isPipV2TestScreen = currentRoute == NavRoute.PIP_V2_TEST

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isSplashScreen && !isHomeScreen && !isPipV2TestScreen) {
                HydroTopBar(
                    title = screenTitle,
                    subtitle = screenSubtitle,
                    isReminderRunning = reminderSettings.isRunning,
                    onBackClick = if (currentRoute != NavRoute.HOME) {
                        {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(NavRoute.HOME) {
                                    popUpTo(NavRoute.HOME) { inclusive = true }
                                }
                            }
                        }
                    } else null
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 150)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 200, delayMillis = 150)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 150)) + slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 150)
                )
            ) {
                FloatingBottomDock(
                    currentRoute = currentRoute,
                    onNavigate = { destinationRoute ->
                        navController.navigate(destinationRoute) {
                            popUpTo(NavRoute.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        HydroNavHost(
            navController = navController,
            viewModel = viewModel,
            innerPadding = if (showBottomBar) innerPadding else PaddingValues(0.dp)
        )
    }
}
