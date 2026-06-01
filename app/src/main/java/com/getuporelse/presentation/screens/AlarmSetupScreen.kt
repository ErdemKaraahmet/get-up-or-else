package com.getuporelse.presentation.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.getuporelse.core.constants.AlarmUiConstants
import com.getuporelse.domain.models.Alarm
import com.getuporelse.presentation.theme.DarkBackground
import com.getuporelse.presentation.theme.AlarmDimensions
import com.getuporelse.presentation.viewmodels.AlarmViewModel
import com.getuporelse.presentation.components.AlarmListHeader
import com.getuporelse.presentation.components.AlarmScheduleCard
import com.getuporelse.presentation.components.AlarmSetupActions
import com.getuporelse.presentation.components.GetUpOrElseTopBar
import com.getuporelse.presentation.components.GetUpOrElseTimePicker
import com.getuporelse.presentation.components.NoEmergencyDismissalText

@Composable
fun AlarmSetupScreen(
    viewModel: AlarmViewModel,
    showDebugActions: Boolean = false,
    onTriggerAlarm: () -> Unit = {},
    onStopAlarm: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val use24HourFormat = DateFormat.is24HourFormat(context)
    val settings by viewModel.settings.collectAsState()
    val isSettingsLoaded by viewModel.isSettingsLoaded.collectAsState()
    
    var activeEditingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var showAddAlarmPicker by remember { mutableStateOf(false) }

    if (!isSettingsLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        )
        return
    }

    if (activeEditingAlarm != null) {
        val alarm = activeEditingAlarm!!
        GetUpOrElseTimePicker(
            initialHour = alarm.hour,
            initialMinute = alarm.minute,
            is24Hour = use24HourFormat,
            onConfirm = { hour, minute ->
                viewModel.updateAlarm(alarm.id, hour, minute, alarm.isEnabled)
                activeEditingAlarm = null
            },
            onDismiss = { activeEditingAlarm = null },
            onDelete = {
                viewModel.deleteAlarm(alarm.id)
                activeEditingAlarm = null
            }
        )
    }

    if (showAddAlarmPicker) {
        GetUpOrElseTimePicker(
            initialHour = 7,
            initialMinute = 0,
            is24Hour = use24HourFormat,
            onConfirm = { hour, minute ->
                viewModel.addAlarm(hour, minute)
                showAddAlarmPicker = false
            },
            onDismiss = { showAddAlarmPicker = false }
        )
    }

    Scaffold(
        topBar = {
            GetUpOrElseTopBar(
                showDebugActions = showDebugActions,
                onTriggerAlarm = onTriggerAlarm,
                onStopAlarm = onStopAlarm
            )
        },
        floatingActionButton = {
            AlarmSetupActions(
                targetReps = settings.targetReps,
                onEditTargetReps = { reps ->
                    viewModel.updateGlobalReps(reps)
                },
                onAddAlarm = {
                    showAddAlarmPicker = true
                }
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AlarmDimensions.HorizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(AlarmDimensions.SpacingSmall))

            AlarmListHeader(
                onSettingsClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(AlarmDimensions.SpacingNormal))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AlarmDimensions.SpacingNormal)
            ) {
                items(settings.alarms, key = { it.id }) { alarm ->
                    AlarmScheduleCard(
                        hour = alarm.hour,
                        minute = alarm.minute,
                        isEnabled = alarm.isEnabled,
                        use24HourFormat = use24HourFormat,
                        onOpenTimePicker = { activeEditingAlarm = alarm },
                        onEnabledChange = { isEnabled ->
                            viewModel.toggleAlarm(alarm.id, isEnabled)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(AlarmDimensions.SpacingMicro))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        NoEmergencyDismissalText()
                    }
                    Spacer(modifier = Modifier.height(AlarmDimensions.PaddingLarge + AlarmDimensions.RepPickerHeight))
                }
            }
        }
    }
}
