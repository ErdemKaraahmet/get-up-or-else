package com.getuporelse.presentation.screens

import androidx.compose.runtime.Composable
import com.getuporelse.presentation.viewmodels.AlarmViewModel
import com.getuporelse.ui.components.AlarmRingingContent

@Composable
fun AlarmRingingScreen(
    viewModel: AlarmViewModel
) {
    AlarmRingingContent(
        onStartExercise = {
            // TODO: Navigate to ExerciseScreen in Phase 3
        }
    )
}
