package com.droidcon.desktimer

import androidx.core.net.toUri
import com.droidcon.desktimer.managers.AudioManager
import com.droidcon.desktimer.managers.TimerStateManager
import com.droidcon.desktimer.ui.SnoozeButton
import com.meta.spatial.compose.composePanel
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Vector3
import com.meta.spatial.isdk.IsdkGrabbable
import com.meta.spatial.toolkit.Grabbable
import com.meta.spatial.toolkit.GrabbableType
import com.meta.spatial.toolkit.Mesh
import com.meta.spatial.toolkit.MeshCollision
import com.meta.spatial.toolkit.Panel
import com.meta.spatial.toolkit.PanelRegistration
import com.meta.spatial.toolkit.Scale
import com.meta.spatial.toolkit.Transform
import com.meta.spatial.toolkit.TransformParent
import com.droidcon.desktimer.ui.TimerColorOption
import com.droidcon.desktimer.ui.TimerPanelWrapper
import com.droidcon.desktimer.utils.addDeleteButton
import com.droidcon.desktimer.utils.getDisposableID
import com.droidcon.desktimer.utils.placeInFront
import com.meta.spatial.toolkit.Visible

// Minimal timer tool: creates a mesh entity and a child panel to show countdown
class TimerTool(
    hours: Int,
    minutes: Int,
    seconds: Int,
    colorOption: TimerColorOption,
    snoozeMinutes: Int = 5 // Custom snooze time in minutes
) {
    private val immA = TimerActivity.getInstance()
    private val totalSeconds = hours * 3600 + minutes * 60 + seconds
    private val panelId = getDisposableID()
    private val snoozePanelId = getDisposableID()
    private var snoozeButton: Entity
    private var timerPanelEntity: Entity
    private val snoozeTimeSeconds = snoozeMinutes * 60 // Store snooze time in seconds

    init {
        val _width = 0.18f
        val _height = 0.18f
        val _dp = 1150f

        immA?.registerPanel(
            PanelRegistration(panelId) {
                config {
                    themeResourceId = R.style.PanelAppThemeTransparent
                    width = _width
                    height = _height
                    layoutWidthInDp = _dp
                    layoutHeightInDp = _dp * (height / width)
                    includeGlass = false
                }
                composePanel {
                    setContent {
                        TimerPanelWrapper(
                            panelId = panelId.toString(),
                            totalSeconds = totalSeconds,
                            startTimeMs = System.currentTimeMillis() + 300
                        )
                    }
                }
            }
        )

        // Create an entity with the timer model
        val timerObj = Entity.create(
            Mesh(
                mesh = when (colorOption) {
                    TimerColorOption.PEACH -> "timer_peach.glb".toUri()
                    TimerColorOption.CYAN -> "timer_cyan.glb".toUri()
                    TimerColorOption.RED -> "timer_red.glb".toUri()
                }
            ),
            Scale(0.1f),
            Grabbable(enabled = true, GrabbableType.PIVOT_Y),
            IsdkGrabbable(billboardOrientation = Vector3(0f, 180f, 0f)),
            Transform(Pose(Vector3(0f), Quaternion(0f, 180f, 0f))),
        )

        // Create an entity with a panel component
        val timerPanel: Entity = Entity.create(
            // hittable property should be NonCollision if we don't want to interact with it, nor
            // block the parent entity
            Panel(panelId).apply { hittable = MeshCollision.NoCollision },
            Transform(Pose(Vector3(0f, 0f, 0.025f), Quaternion(0f, 180f, 0f))),
        )

        // We add a TimeComponent to timer panel to be able to update it. More info in
        // UpdateTimeSystem.kt
        timerPanel.setComponent(
            TimeComponent(
                totalTime = totalSeconds,
                startTime = System.currentTimeMillis(),
            )
        )
        timerObj.setComponent(
            ToolComponent(
                uuid = -1,
                deleteButtonPosition = Vector3(0f, 0.13f, 0f),
            )
        )
        // We make panel entity child to the timer model entity
        timerPanel.setComponent(TransformParent(timerObj))

        // Store reference to timer panel for later use
        timerPanelEntity = timerPanel

        immA?.registerPanel(
            PanelRegistration(snoozePanelId) {
                config {
                    themeResourceId = R.style.PanelAppThemeTransparent
                    width = 0.10f           // 10 cm
                    height = 0.04f          // 4 cm
                    layoutWidthInDp = 400f
                    layoutHeightInDp = 160f
                    includeGlass = false
                }
                composePanel {
                    setContent {
                        SnoozeButton { handleSnooze(timerPanelEntity, snoozeButton) }
                    }
                }
            }
        )

        // Create snooze button panel entity (positioned below the timer)
        snoozeButton = Entity.create(
            Panel(snoozePanelId),
            Transform(Pose(Vector3(0f, -0.15f, 0.03f), Quaternion(0f, 180f, 0f))),
            Visible(false), // Initially hidden
        )
        snoozeButton.setComponent(TransformParent(timerObj))

        // Register the snooze button with the activity for this timer
        immA?.registerSnoozeButton(timerPanel, snoozeButton)


        // We place it in front of the user
        placeInFront(timerObj)
        addDeleteButton(timerObj)

        AudioManager.instance.playCreationSound(timerObj.getComponent<Transform>().transform.t)
    }

    private fun handleSnooze(panelEntity: Entity, snoozeBtn: Entity) {
        // Hide the snooze button
        snoozeBtn.setComponent(Visible(false))

        // Get the timer's parent mesh to stop audio
        val parentMesh = panelEntity.tryGetComponent<TransformParent>()?.entity
        parentMesh?.let { AudioManager.instance.stopTimerSound(it) }
        AudioManager.instance.stopTimerSound(panelEntity)

        // Reset the timer to custom snooze time
        if (panelEntity.hasComponent<TimeComponent>()) {
            val timeComponent = panelEntity.getComponent<TimeComponent>()
            timeComponent.complete = false
            timeComponent.startTime = System.currentTimeMillis()
            timeComponent.totalTime = snoozeTimeSeconds // Use custom snooze time
            panelEntity.setComponent(timeComponent)

            // Update UI state immediately
            val panel = panelEntity.tryGetComponent<Panel>()
            if (panel != null) {
                val panelId = panel.panelRegistrationId.toString()
                val snoozeMinutes = snoozeTimeSeconds / 60
                val snoozeSeconds = snoozeTimeSeconds % 60
                TimerStateManager.updateTimerState(
                    panelId = panelId,
                    totalSeconds = timeComponent.totalTime,
                    hours = 0,
                    minutes = snoozeMinutes,
                    seconds = snoozeSeconds,
                )
            }
        }
    }
}