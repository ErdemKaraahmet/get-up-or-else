package com.getuporelse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.getuporelse.core.constants.AlarmUiConstants

@Composable
fun AlarmRingingContent(
    onStartExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AlarmUiConstants.SCREEN_VERTICAL_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WAKE UP",
            fontSize = AlarmUiConstants.RINGING_TITLE_FONT_SIZE_SP.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(AlarmUiConstants.RINGING_TITLE_SPACING_DP.dp))

        Text(
            text = "There is no emergency dismissal. No shortcuts.",
            fontSize = AlarmUiConstants.RINGING_MESSAGE_FONT_SIZE_SP.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AlarmUiConstants.RINGING_BUTTON_SPACING_DP.dp))

        Button(
            onClick = onStartExercise,
            modifier = Modifier.height(AlarmUiConstants.START_PUSHING_BUTTON_HEIGHT_DP.dp)
        ) {
            Text(
                text = "Start Pushing",
                fontSize = AlarmUiConstants.START_PUSHING_FONT_SIZE_SP.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
