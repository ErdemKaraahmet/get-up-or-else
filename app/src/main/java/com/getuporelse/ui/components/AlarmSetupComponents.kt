package com.getuporelse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getuporelse.core.constants.AlarmUiConstants

@Composable
fun GetUpOrElseTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AlarmUiConstants.APP_BAR_HEIGHT_DP.dp)
            .padding(horizontal = AlarmUiConstants.SCREEN_HORIZONTAL_PADDING_DP.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "getuporelse",
            fontSize = AlarmUiConstants.BRAND_FONT_SIZE_SP.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp
        )
    }
}

@Composable
fun AlarmSetupActions(
    targetReps: Int,
    onEditTargetReps: () -> Unit,
    onAddAlarm: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            bottom = AlarmUiConstants.ACTION_BAR_BOTTOM_PADDING_DP.dp,
            end = AlarmUiConstants.ACTION_BAR_END_PADDING_DP.dp
        )
    ) {
        RepTargetBadge(
            targetReps = targetReps,
            onEditTargetReps = onEditTargetReps
        )

        Spacer(modifier = Modifier.width(AlarmUiConstants.ACTION_BAR_ITEM_SPACING_DP.dp))

        FloatingActionButton(
            onClick = onAddAlarm,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            shape = CircleShape,
            modifier = Modifier.size(AlarmUiConstants.ADD_ALARM_BUTTON_SIZE_DP.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Alarm",
                modifier = Modifier.size(AlarmUiConstants.ADD_ALARM_ICON_SIZE_DP.dp)
            )
        }
    }
}

@Composable
fun AlarmListHeader(
    onToggleTimeFormat: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "Alarms",
            fontSize = AlarmUiConstants.SCREEN_TITLE_FONT_SIZE_SP.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        IconButton(onClick = onToggleTimeFormat) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Toggle 24h format",
                tint = Color.White.copy(alpha = AlarmUiConstants.SECONDARY_ICON_ALPHA)
            )
        }
    }
}

@Composable
fun AlarmScheduleCard(
    hour: Int,
    minute: Int,
    isEnabled: Boolean,
    use24HourFormat: Boolean,
    onOpenTimePicker: () -> Unit,
    onEnabledChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AlarmUiConstants.ALARM_CARD_CORNER_RADIUS_DP.dp))
            .background(Color(AlarmUiConstants.ALARM_CARD_COLOR))
            .clickable(onClick = onOpenTimePicker)
            .padding(AlarmUiConstants.ALARM_CARD_PADDING_DP.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = formatAlarmTime(
                        hour = hour,
                        minute = minute,
                        use24HourFormat = use24HourFormat
                    ),
                    fontSize = alarmTimeFontSize(use24HourFormat).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    letterSpacing = 0.sp
                )
                Text(
                    text = "Everyday",
                    fontSize = AlarmUiConstants.ALARM_REPEAT_FONT_SIZE_SP.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(AlarmUiConstants.SWITCH_CHECKED_TRACK_COLOR),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = Color(AlarmUiConstants.SWITCH_UNCHECKED_TRACK_COLOR),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun NoEmergencyDismissalText(
    modifier: Modifier = Modifier
) {
    Text(
        text = "There is no emergency dismissal. No shortcuts.",
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.Bold,
        fontSize = AlarmUiConstants.NO_ESCAPE_FONT_SIZE_SP.sp,
        modifier = modifier
    )
}

@Composable
private fun RepTargetBadge(
    targetReps: Int,
    onEditTargetReps: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(
                horizontal = AlarmUiConstants.REP_BADGE_HORIZONTAL_PADDING_DP.dp,
                vertical = AlarmUiConstants.REP_BADGE_VERTICAL_PADDING_DP.dp
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$targetReps PUSH-UPS",
                fontSize = AlarmUiConstants.REP_BADGE_FONT_SIZE_SP.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(AlarmUiConstants.REP_BADGE_ICON_SPACING_DP.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Reps",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(AlarmUiConstants.REP_BADGE_ICON_SIZE_DP.dp)
                    .clickable(onClick = onEditTargetReps)
            )
        }
    }
}

private fun formatAlarmTime(
    hour: Int,
    minute: Int,
    use24HourFormat: Boolean
): String {
    if (use24HourFormat) {
        return AlarmUiConstants.MINUTES_TWO_DIGITS_FORMAT.format(hour, minute)
    }

    val displayHour = if (hour % AlarmUiConstants.HOURS_PER_HALF_DAY == 0) {
        AlarmUiConstants.HOURS_PER_HALF_DAY
    } else {
        hour % AlarmUiConstants.HOURS_PER_HALF_DAY
    }
    val period = if (hour < AlarmUiConstants.HOURS_PER_HALF_DAY) "AM" else "PM"

    return AlarmUiConstants.TWELVE_HOUR_TIME_FORMAT.format(displayHour, minute, period)
}

private fun alarmTimeFontSize(use24HourFormat: Boolean): Int {
    return if (use24HourFormat) {
        AlarmUiConstants.ALARM_TIME_24_HOUR_FONT_SIZE_SP
    } else {
        AlarmUiConstants.ALARM_TIME_12_HOUR_FONT_SIZE_SP
    }
}
