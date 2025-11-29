package com.droidcon.desktimer.ui

@Composable
fun TimerPanelWrapper(
    panelId: String,
    totalSeconds: Int,
    startTimeMs: Long
) {
    TimerPanel(
        totalSeconds = totalSeconds,
        startTimeMs = startTimeMs,
        timerState = null
    )
}