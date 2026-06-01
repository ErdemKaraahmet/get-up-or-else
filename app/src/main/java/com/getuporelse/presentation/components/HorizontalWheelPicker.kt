package com.getuporelse.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import com.getuporelse.presentation.theme.AlarmTypography
import com.getuporelse.presentation.theme.AlarmDimensions

/**
 * A highly premium, 3D sideways Apple-style circular scroll picker in a capsule shape.
 * Fits perfectly next to the floating action button (same thickness: 56.dp).
 * Updates the repository ONLY when the wheel is stationary (stopped scrolling).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CapsuleRepPicker(
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 1..100,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val items = remember(range) { range.toList() }

    // INCREASED SIZE: Spreads numbers and fills space better
    val itemSizeDp = AlarmDimensions.RepItemWidth

    // Center selected index in real-time
    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf -1

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: -1
        }
    }

    // Scroll to initial value on launch or external change
    LaunchedEffect(selectedValue) {
        val targetIndex = items.indexOf(selectedValue)
        if (targetIndex != -1 && centeredIndex != targetIndex) {
            lazyListState.scrollToItem(targetIndex)
        }
    }

    // Trigger state/repository update ONLY when the wheel is stationary
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isNotEmpty()) {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val centeredItem = visibleItemsInfo.minByOrNull {
                    abs((it.offset + it.size / 2) - viewportCenter)
                }
                if (centeredItem != null && centeredItem.index in items.indices) {
                    val settledValue = items[centeredItem.index]
                    if (settledValue != selectedValue) {
                        onValueChange(settledValue)
                    }
                }
            }
        }
    }

    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState)

    Column(
        modifier = modifier
            .height(AlarmDimensions.RepPickerHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val widthDp = maxWidth
            val sidePadding = (widthDp - itemSizeDp) / 2

            LazyRow(
                state = lazyListState,
                flingBehavior = snapFlingBehavior,
                contentPadding = PaddingValues(start = sidePadding, end = sidePadding),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(items) { index, value ->
                    val itemOffset = remember(lazyListState, index) {
                        derivedStateOf {
                            val layoutInfo = lazyListState.layoutInfo
                            val visibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                            if (visibleItem != null) {
                                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                                (visibleItem.offset + visibleItem.size / 2) - center
                            } else {
                                1000
                            }
                        }
                    }

                    val offsetValue = itemOffset.value.toFloat()
                    val maxDistance = with(density) { (widthDp / 2f).toPx() }
                    val fraction = if (offsetValue != 1000f) {
                        (offsetValue / maxDistance).coerceIn(-1.0f, 1.0f)
                    } else {
                        1.0f
                    }

                    // Cylindrical projection for revolving effect
                    val radiusPx = maxDistance * 1.25f 
                    val theta = offsetValue / radiusPx
                    
                    val tx = radiusPx * sin(theta) - offsetValue
                    val ry = -Math.toDegrees(theta.toDouble()).toFloat()

                    // Visual properties: simulate depth with scale and alpha
                    val zDepthFraction = cos(theta).coerceIn(0f, 1f)
                    val scale = (0.7f + 0.3f * zDepthFraction) * (1.0f - 0.1f * abs(fraction))
                    
                    // Fades out slowly: stays opaque longer, then fades at the very edge
                    val alpha = (1.0f - abs(fraction).pow(4f)).coerceIn(0f, 1f)

                    val isCenter = index == centeredIndex

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(itemSizeDp)
                            .graphicsLayer {
                                this.scaleX = scale
                                this.scaleY = scale
                                this.alpha = alpha
                                this.rotationY = ry
                                this.translationX = tx
                                cameraDistance = 12f * density.density
                            }
                    ) {
                        Text(
                            text = "$value",
                            color = Color.White,
                            fontSize = if (isCenter) 20.sp else 15.sp,
                            fontWeight = if (isCenter) FontWeight.Black else FontWeight.Bold,
                            letterSpacing = 0.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AlarmDimensions.PaddingMicro))

        Text(
            text = "rep count",
            color = Color(0xFFBA96DB),
            fontSize = AlarmTypography.SubtitleSize,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}