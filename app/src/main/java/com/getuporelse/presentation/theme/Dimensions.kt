package com.getuporelse.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Universal layout metrics and design tokens for the GetUpOrElse application.
 * Centralizes spacings, paddings, and component sizes to maintain strict visual harmony.
 */
object AlarmDimensions {
    // Basic Paddings and Spacings (Design Grid)
    val PaddingNone = 0.dp
    val PaddingMicro = 4.dp
    val PaddingSmall = 8.dp
    val PaddingNormal = 16.dp
    val PaddingLarge = 24.dp
    val PaddingExtraLarge = 32.dp

    val SpacingSmall = 8.dp
    val SpacingNormal = 16.dp
    val SpacingLarge = 32.dp
    val SpacingExtraLarge = 48.dp
    val SpacingSuper = 64.dp

    // Screen Layout Metrics
    val HorizontalPadding = 16.dp
    val VerticalPadding = 24.dp

    // Component Heights & Boundaries
    val AppBarHeight = 56.dp
    val FloatingButtonSize = 56.dp
    val AddAlarmIconSize = 30.dp
    val DebugActionButtonHeight = 36.dp
    val StartPushingButtonHeight = 72.dp

    // Alarm Card Token Specs
    val CardCornerRadius = 12.dp
    val CardPadding = 16.dp

    // Rep Picker Specific Layout Specs
    val RepPickerWidth = 280.dp
    val RepPickerHeight = 56.dp
    val RepWheelHeight = 38.dp
    val RepItemWidth = 64.dp
    val CursorLineWidth = 1.5.dp
    val CursorLineHeight = 22.dp
}
