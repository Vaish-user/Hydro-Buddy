package com.hydrobuddy.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hydrobuddy.app.ui.screens.customize.CharacterCustomizationScreen
import com.hydrobuddy.app.ui.screens.home.HomeScreen
import com.hydrobuddy.app.ui.screens.settings.SettingsHubScreen
import com.hydrobuddy.app.ui.screens.settings.SettingsSubTab
import com.hydrobuddy.app.ui.screens.splash.AnimatedSplashScreen
import com.hydrobuddy.app.ui.screens.statistics.StatisticsScreen
import com.hydrobuddy.app.ui.screens.test.PipV2TestScreen
import com.hydrobuddy.app.viewmodel.HydroViewModel

@Composable
fun HydroNavHost(
    navController: NavHostController,
    viewModel: HydroViewModel,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    startDestination: String = NavRoute.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(innerPadding),
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable(
            route = NavRoute.SPLASH,
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 150)) }
        ) {
            AnimatedSplashScreen(
                onAnimationComplete = {
                    navController.navigate(NavRoute.HOME) {
                        popUpTo(NavRoute.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = { target ->
                    when (target) {
                        "appearance" -> navController.navigate(NavRoute.APPEARANCE_SETTINGS)
                        "customize", "personality" -> navController.navigate(NavRoute.CUSTOMIZE)
                        "pip_v2_test" -> navController.navigate(NavRoute.PIP_V2_TEST)
                        "stats", "statistics" -> navController.navigate(NavRoute.STATISTICS)
                        else -> navController.navigate(NavRoute.REMINDER_SETTINGS)
                    }
                }
            )
        }

        composable(NavRoute.STATISTICS) {
            StatisticsScreen(viewModel = viewModel)
        }

        composable(NavRoute.CUSTOMIZE) {
            CharacterCustomizationScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoute.SETTINGS) {
            SettingsHubScreen(
                viewModel = viewModel,
                initialTab = SettingsSubTab.REMINDERS,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoute.REMINDER_SETTINGS) {
            SettingsHubScreen(
                viewModel = viewModel,
                initialTab = SettingsSubTab.REMINDERS,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoute.APPEARANCE_SETTINGS) {
            SettingsHubScreen(
                viewModel = viewModel,
                initialTab = SettingsSubTab.APPEARANCE,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoute.PIP_V2_TEST) {
            PipV2TestScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

