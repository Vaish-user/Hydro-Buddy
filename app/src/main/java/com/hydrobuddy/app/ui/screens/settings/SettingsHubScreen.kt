package com.hydrobuddy.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hydrobuddy.app.viewmodel.HydroViewModel

enum class SettingsSubTab(val title: String) {
    REMINDERS("Reminders"),
    APPEARANCE("Appearance"),
    PERSONALITY("Personality")
}

@Composable
fun SettingsHubScreen(
    viewModel: HydroViewModel,
    initialTab: SettingsSubTab = SettingsSubTab.REMINDERS,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    UnifiedSettingsScreen(
        viewModel = viewModel,
        modifier = modifier,
        onBackClick = onBackClick
    )
}
