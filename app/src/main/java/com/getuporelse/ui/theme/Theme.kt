package com.getuporelse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun GetUpOrElseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GetUpOrElseColorScheme,
        content = content
    )
}
