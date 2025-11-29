package com.droidcon.desktimer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.droidcon.desktimer.R

enum class TimerColorOption(val displayName: String, val color: Color) {
    PEACH("Peach", Color(0xFFFCD5CD)),
    RED("Red", Color.Red),
    CYAN("Cyan", Color.Cyan),
}

data class TimerMenuSelection(
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val colorOption: TimerColorOption,
    val snoozeMinutes: Int = 5, // Default 5 minutes
)

@Composable
fun TimerMenu(onSelect: (TimerMenuSelection) -> Unit) {
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(10) }
    var seconds by remember { mutableStateOf(0) }
    var selectedColor by remember { mutableStateOf(TimerColorOption.PEACH) }
    var snoozeMinutes by remember { mutableIntStateOf(5) } // Default 5 minutes
    var isExpanded by remember { mutableStateOf(true) }
    var isCreatingTimer by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (!isExpanded) {
            Surface(
                color = colorResource(id = R.color.background_dark),
                shape = RoundedCornerShape(30.dp),
                modifier =
                    Modifier
                        .width(280.dp)
                        .height(60.dp)
                        .clickable { isExpanded = true },
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Row {
                        Text(
                            text = "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = " Start New Timer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter =
                fadeIn(animationSpec = tween(300)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
            exit =
                fadeOut(animationSpec = tween(250)) +
                        scaleOut(targetScale = 0.8f, animationSpec = tween(250)),
        ) {
            Surface(
                color = colorResource(id = R.color.background_dark),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Set Timer",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TimeSpinner(value = hours, range = 0..23, label = "Hours") { hours = it }
                        TimeSpinner(value = minutes, range = 0..59, label = "Minutes") {
                            minutes = it
                        }
                        TimeSpinner(value = seconds, range = 0..59, label = "Seconds") {
                            seconds = it
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Predefined duration chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                DurationChip(
                                    label = "5m",
                                    onClick = {
                                        hours = 0
                                        minutes = 5
                                        seconds = 0
                                    }
                                )
                            }
                            item {
                                DurationChip(
                                    label = "10m",
                                    onClick = {
                                        hours = 0
                                        minutes = 10
                                        seconds = 0
                                    }
                                )
                            }
                            item {
                                DurationChip(
                                    label = "15m",
                                    onClick = {
                                        hours = 0
                                        minutes = 15
                                        seconds = 0
                                    }
                                )
                            }
                            item {
                                DurationChip(
                                    label = "30m",
                                    onClick = {
                                        hours = 0
                                        minutes = 30
                                        seconds = 0
                                    }
                                )
                            }
                            item {
                                DurationChip(
                                    label = "1h",
                                    onClick = {
                                        hours = 1
                                        minutes = 0
                                        seconds = 0
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Timer Color",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(TimerColorOption.values()) { option ->
                            ColorOptionChip(
                                option = option,
                                selected = selectedColor == option,
                                onClick = { selectedColor = option },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Snooze Time Selecto`r
                    Text(
                        text = "Snooze Time",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Decrement button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.background_medium))
                                .clickable {
                                    if (snoozeMinutes > 1) snoozeMinutes--
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "−",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Display snooze time
                        Text(
                            text = "$snoozeMinutes mins",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.timer_golden),
                            modifier = Modifier.width(100.dp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        // Increment button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.background_medium))
                                .clickable {
                                    if (snoozeMinutes < 60) snoozeMinutes++
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .width(240.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))                      // Same rounded shape
                            .background(colorResource(id = R.color.button_red))   // Same background color
                            .debounceClickable {
                                onSelect(
                                    TimerMenuSelection(
                                        hours = hours,
                                        minutes = minutes,
                                        seconds = seconds,
                                        colorOption = selectedColor,
                                        snoozeMinutes = snoozeMinutes,
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start Timer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier =
                            Modifier
                                .width(240.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .border(
                                    3.dp,
                                    colorResource(id = R.color.border_gray),
                                    RoundedCornerShape(28.dp)
                                )
                                .clickable { isExpanded = false },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Hide",
                            fontSize = 20.sp,
                            color = colorResource(id = R.color.text_gray)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOptionChip(option: TimerColorOption, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(option.color)
                .border(
                    width = if (selected) 4.dp else 2.dp,
                    color = if (selected) Color.White else Color.Transparent,
                    shape = CircleShape,
                )
                .clickable { onClick() },
    )
}

@Composable
fun TimeSpinner(
    value: Int,
    range: IntRange,
    label: String,
    onValueChange: (Int) -> Unit
) {
    val itemHeight = 80.dp
    val visibleItemCount = 3 // Show 3 items at once

    val middleIndex = 1000 // Large number for pseudo-infinite scrolling
    val startIndex = middleIndex + (value - range.first)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Track the center item and update value
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstIndex ->
                val centerItemIndex = firstIndex + 1 // Middle of 3 visible items
                val actualValue =
                    range.first + (centerItemIndex - middleIndex + range.count()) % range.count()
                onValueChange(actualValue)
            }
    }

    // Scroll to position when value changes externally
    LaunchedEffect(value) {
        val targetIndex = middleIndex + (value - range.first)
        if (listState.firstVisibleItemIndex != targetIndex - 1) {
            listState.scrollToItem(targetIndex - 1)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(itemHeight * visibleItemCount),
            contentAlignment = Alignment.Center
        ) {
            // Selection indicators (top and bottom lines)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(itemHeight)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(colorResource(id = R.color.border_gray))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(colorResource(id = R.color.border_gray))
                )
            }

            // Scrollable list
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(count = middleIndex * 2) { index ->
                    val actualValue =
                        range.first + (index - middleIndex + range.count()) % range.count()
                    val distanceFromCenter = (index - listState.firstVisibleItemIndex - 1).toFloat()

                    // Calculate alpha and scale based on distance from center
                    val alpha = when (abs(distanceFromCenter).toInt()) {
                        0 -> 1f
                        1 -> 0.5f
                        else -> 0.3f
                    }
                    val scale = when (abs(distanceFromCenter).toInt()) {
                        0 -> 1f
                        1 -> 0.8f
                        else -> 0.6f
                    }

                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .alpha(alpha)
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = actualValue.toString().padStart(2, '0'),
                            fontSize = 56.sp,
                            fontWeight = if (abs(distanceFromCenter) < 0.5f) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            fontSize = 20.sp,
            color = colorResource(id = R.color.text_gray),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DurationChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(id = R.color.background_medium))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 800
)
@Composable
fun TimerMenuPreview() {
    TimerMenu(onSelect = { _ -> })
}

inline fun Modifier.debounceClickable(
    debounceInterval: Long = 400,
    crossinline onClick: () -> Unit,
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }
    clickable() {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastClickTime) < debounceInterval) return@clickable
        lastClickTime = currentTime
        onClick()
    }
}
