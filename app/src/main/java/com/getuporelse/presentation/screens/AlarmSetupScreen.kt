package com.getuporelse.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.getuporelse.core.constants.AlarmUiConstants
import com.getuporelse.presentation.viewmodels.AlarmViewModel
import com.getuporelse.ui.components.AlarmListHeader
import com.getuporelse.ui.components.AlarmScheduleCard
import com.getuporelse.ui.components.AlarmSetupActions
import com.getuporelse.ui.components.GetUpOrElseTopBar
import com.getuporelse.ui.components.GetUpOrElseTimePicker
import com.getuporelse.ui.components.NoEmergencyDismissalText

@Composable
fun AlarmSetupScreen(
    viewModel: AlarmViewModel
) {
    val settings by viewModel.settings.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        GetUpOrElseTimePicker(
            initialHour = settings.hour,
            initialMinute = settings.minute,
            is24Hour = settings.use24HourFormat,
            onConfirm = { hour, minute ->
                viewModel.updateAlarm(hour, minute, settings.targetReps, settings.isEnabled)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    Scaffold(
        topBar = { GetUpOrElseTopBar() },
        floatingActionButton = {
            AlarmSetupActions(
                targetReps = settings.targetReps,
                onEditTargetReps = {
                    // TODO: Implement rep count picker
                },
                onAddAlarm = {
                    // Placeholder for adding more alarms
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AlarmUiConstants.SCREEN_HORIZONTAL_PADDING_DP.dp)
        ) {
            Spacer(modifier = Modifier.height(AlarmUiConstants.SECTION_TOP_SPACING_DP.dp))

            AlarmListHeader(
                onToggleTimeFormat = viewModel::toggle24HourFormat
            )

            Spacer(modifier = Modifier.height(AlarmUiConstants.SECTION_ITEM_SPACING_DP.dp))

            AlarmScheduleCard(
                hour = settings.hour,
                minute = settings.minute,
                isEnabled = settings.isEnabled,
                use24HourFormat = settings.use24HourFormat,
                onOpenTimePicker = { showTimePicker = true },
                onEnabledChange = { isEnabled ->
                    viewModel.updateAlarm(
                        settings.hour,
                        settings.minute,
                        settings.targetReps,
                        isEnabled
                    )
                }
            )

            Spacer(modifier = Modifier.height(AlarmUiConstants.NO_ESCAPE_TOP_SPACING_DP.dp))

            NoEmergencyDismissalText(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
