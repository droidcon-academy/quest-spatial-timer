package com.droidcon.desktimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import com.droidcon.desktimer.R

// State holder for timer display data
data class TimerState(
    val totalSeconds: Int = 0,
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0
)

@Composable
fun TimerPanel(
    totalSeconds: Int,
    startTimeMs: Long,
    timerState: TimerState? = null // Accept external state
) {
    // If external state is provided, use it; otherwise calculate locally
    val displayState = timerState ?: run {
        val now = System.currentTimeMillis()
        val end = startTimeMs + totalSeconds * 1000L
        val timeLeft = (end - now).coerceAtLeast(0)
        val remainingSeconds = (timeLeft / 1000L).toInt()
        val h = remainingSeconds / 3600
        val m = (remainingSeconds % 3600) / 60
        val s = remainingSeconds % 60
        TimerState(totalSeconds, h, m, s)
    }

    // Calculate progress (0.0 to 1.0)
    val remainingSeconds = displayState.hours * 3600 + displayState.minutes * 60 + displayState.seconds
    val progress = if (displayState.seconds > 0) remainingSeconds.toFloat() / displayState.totalSeconds.toFloat() else 0f

    Surface(color = Color.Transparent, tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress bar - Decreased by 10% from 1200dp
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(1080.dp), // 1200dp - 10% = 1080dp
                color = colorResource(id = R.color.timer_golden),
                backgroundColor = colorResource(id = R.color.background_medium),
                strokeWidth = 43.dp // 48dp - 10% ≈ 43dp
            )

            // Content inside the circle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Total time at the top - Decreased by 10% from 150sp
                val totalHours = displayState.totalSeconds / 3600
                val totalMins = (displayState.totalSeconds % 3600) / 60
                val totalSecs = displayState.totalSeconds % 60

                val totalTimeText = buildString {
                    if (totalHours > 0) append("${totalHours}h ")
                    if (totalMins > 0) append("${totalMins}m ")
                    if (totalSecs > 0 || (totalHours == 0 && totalMins == 0)) append("${totalSecs}s")
                }.trim()

                Text(
                    text = totalTimeText,
                    color = colorResource(id = R.color.text_gray),
                    fontSize = 135.sp, // 150sp - 10% = 135sp
                    fontWeight = FontWeight.Normal
                )

                // Spacer - Decreased by 10% from 60dp
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(54.dp)) // 60dp - 10% = 54dp

                // Time display with HH:MM:SS format
                Text(
                    text = buildString {
                        if (displayState.seconds < 0 || displayState.minutes < 0 || displayState.hours < 0) {
                            append("-")
                        }
                        if (displayState.hours.absoluteValue > 0) {
                            append(displayState.hours.toString().padStart(2, '0'))
                            append(":")
                        }
                        append(displayState.minutes.absoluteValue.toString().padStart(2, '0'))
                        append(":")
                        append(displayState.seconds.absoluteValue.toString().padStart(2, '0'))
                    },
                    color = colorResource(id = R.color.timer_golden),
                    fontSize = 200.sp, // Decreased to 270sp to accommodate HH:MM:SS format
                    fontWeight = FontWeight.Bold
                )

                // Spacer - Decreased by 10% from 60dp (to space from expected finish)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(54.dp)) // 60dp - 10% = 54dp

                // Expected finish time below
                val finishMillis = System.currentTimeMillis() +
                        (displayState.hours * 3600 + displayState.minutes * 60 + displayState.seconds) * 1000L

                val finishDate = Date(finishMillis)
                val finishFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val finishTimeStr = finishFormat.format(finishDate)

                Text(
                    text = "Ends $finishTimeStr",
                    color = colorResource(id = R.color.text_gray),
                    fontSize = 80.sp, // Sized to appear distinct, not overpower
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = colorResource(id = R.color.timer_golden),
    backgroundColor: Color = colorResource(id = R.color.background_medium),
    strokeWidth: androidx.compose.ui.unit.Dp = 43.dp // Default decreased by 10% from 48dp
) {
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokeWidthPx = strokeWidth.toPx()
        val topLeft = Offset(
            (size.width - diameter) / 2f + strokeWidthPx / 2f,
            (size.height - diameter) / 2f + strokeWidthPx / 2f
        )
        val adjustedSize = Size(diameter - strokeWidthPx, diameter - strokeWidthPx)

        // Draw background circle
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = adjustedSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // Draw progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = adjustedSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 1000,
    heightDp = 1000
)
@Composable
fun TimerPanelPreview() {
    TimerPanel(
        totalSeconds = 25 * 60,
        startTimeMs = System.currentTimeMillis(),
        timerState = TimerState(25 * 60, 0, 25, 0)
    )
}
