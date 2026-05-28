package com.getuporelse.presentation.screens

import androidx.compose.runtime.Composable
import com.getuporelse.presentation.viewmodels.AlarmViewModel
import com.getuporelse.ui.components.AlarmRingingContent

@Composable
fun AlarmRingingScreen(
    viewModel: AlarmViewModel,
    showDebugActions: Boolean = false,
    onTriggerAlarm: () -> Unit = {},
    onStopAlarm: () -> Unit = {}
) {
    AlarmRingingContent(
        showDebugActions = showDebugActions,
        onTriggerAlarm = onTriggerAlarm,
        onStopAlarm = onStopAlarm,
        onStartExercise = {
            viewModel.startExercise()
        }
    )
}
