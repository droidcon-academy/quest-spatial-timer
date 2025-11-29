package com.droidcon.desktimer.systems

import com.droidcon.desktimer.TimeComponent
import com.droidcon.desktimer.TimerActivity
import com.droidcon.desktimer.managers.AudioManager
import com.droidcon.desktimer.managers.TimerStateManager
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Query
import com.meta.spatial.core.SystemBase
import com.meta.spatial.toolkit.Panel

class UpdateTimeSystem : SystemBase() {
    private var lastTime = System.currentTimeMillis()
    private val activity get() = TimerActivity.getInstance()

    override fun execute() {
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastTime) / 1000f
        if (deltaTime < 1f) return                                          // Only run roughly once per second
        lastTime = currentTime

        // Query all entities that have a TimeComponent
        val timers = Query.where { has(TimeComponent.id) }.eval()
        for (entity in timers) {
            if (!entity.hasComponent<TimeComponent>()) continue
            val timeComp = entity.getComponent<TimeComponent>()
            val panel = entity.tryGetComponent<Panel>() ?: continue         // Needs to have a Panel (we update via panelId)
            val panelId = panel.panelRegistrationId.toString()
            updateTimer(panelId, entity, timeComp)                          // (3) do the update for this timer
        }
    }

    private fun updateTimer(panelId: String, entity: Entity, timeComp: TimeComponent) {
        val now = System.currentTimeMillis()
        val timeLeftMs = (timeComp.startTime + timeComp.totalTime * 1000L) - now
        val remainingSeconds = (timeLeftMs / 1000).toInt()

        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60                                 // (4) calculate H:M:S left

        if (!timeComp.complete && timeLeftMs <= 0L) {
            // Timer just finished!
            timeComp.complete = true
            AudioManager.instance.playTimerSound(entity)
            activity?.showSnoozeButton(entity)
            entity.setComponent(timeComp)  // update component state
        }

        // Update the Compose state via state manager
        TimerStateManager.updateTimerState(
            panelId = panelId,
            totalSeconds = timeComp.totalTime,
            hours = hours, minutes = minutes, seconds = seconds
        )
    }
}
