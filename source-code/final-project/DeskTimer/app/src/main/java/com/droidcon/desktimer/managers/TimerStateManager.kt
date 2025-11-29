package com.droidcon.desktimer.managers

import com.droidcon.desktimer.ui.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

object TimerStateManager {
    private val timerStates = ConcurrentHashMap<String, MutableStateFlow<TimerState>>()

    fun getTimerState(panelId: String): StateFlow<TimerState> {
        return timerStates.getOrPut(panelId) { MutableStateFlow(TimerState()) }.asStateFlow()
    }

    fun updateTimerState(panelId: String, totalSeconds: Int, hours: Int, minutes: Int, seconds: Int) {
        timerStates.getOrPut(panelId) { MutableStateFlow(TimerState()) }
            .value = TimerState(totalSeconds, hours, minutes, seconds)
    }

    fun removeTimer(panelId: String) {
        timerStates.remove(panelId)
    }
}
