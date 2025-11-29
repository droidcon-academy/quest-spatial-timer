package com.droidcon.desktimer

import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.droidcon.desktimer.managers.AudioManager
import com.droidcon.desktimer.systems.UpdateAudioPositionSystem
import com.droidcon.desktimer.systems.UpdateTimeSystem
import com.droidcon.desktimer.ui.TimerMenu
import com.droidcon.desktimer.utils.addOnSelectListener
import com.droidcon.desktimer.utils.deleteObject
import com.droidcon.desktimer.utils.getHeadEntity
import com.droidcon.desktimer.utils.placeInFront
import com.meta.spatial.castinputforward.CastInputForwardFeature
import com.meta.spatial.compose.ComposeFeature
import com.meta.spatial.compose.composePanel
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.SpatialFeature
import com.meta.spatial.core.SpatialSDKExperimentalAPI
import com.meta.spatial.core.Vector2
import com.meta.spatial.core.Vector3
import com.meta.spatial.datamodelinspector.DataModelInspectorFeature
import com.meta.spatial.debugtools.HotReloadFeature
import com.meta.spatial.isdk.IsdkGrabbable
import com.meta.spatial.isdk.IsdkPanelDimensions
import com.meta.spatial.ovrmetrics.OVRMetricsDataModel
import com.meta.spatial.ovrmetrics.OVRMetricsFeature
import com.meta.spatial.runtime.ReferenceSpace
import com.meta.spatial.runtime.SceneAudioAsset
import com.meta.spatial.runtime.SceneAudioPlayer
import com.meta.spatial.toolkit.AppSystemActivity
import com.meta.spatial.toolkit.Box
import com.meta.spatial.toolkit.Followable
import com.meta.spatial.toolkit.FollowableType
import com.meta.spatial.toolkit.Material
import com.meta.spatial.toolkit.Mesh
import com.meta.spatial.toolkit.PanelRegistration
import com.meta.spatial.toolkit.Transform
import com.meta.spatial.toolkit.Visible
import com.meta.spatial.toolkit.createPanelEntity
import com.meta.spatial.vr.LocomotionSystem
import com.meta.spatial.vr.VRFeature
import com.meta.spatial.vr.VrInputSystemType
import java.lang.ref.WeakReference

class TimerActivity : AppSystemActivity() {

    companion object {
        private lateinit var instance: WeakReference<TimerActivity>
        fun getInstance(): TimerActivity? =
            if (this::instance.isInitialized) instance.get() else null
    }

    private val timerSnoozeButtons = mutableMapOf<Entity, Entity>()

    var deleteButton: Entity? = null
    var currentObjectSelected: Entity? = null

    override fun registerFeatures(): List<SpatialFeature> {
        val features = mutableListOf<SpatialFeature>(
            VRFeature(this, inputSystemType = VrInputSystemType.SIMPLE_CONTROLLER), ComposeFeature()
        )
        if (BuildConfig.DEBUG) {
            features.add(CastInputForwardFeature(this))
            features.add(HotReloadFeature(this))
            features.add(OVRMetricsFeature(this, OVRMetricsDataModel() { numberOfMeshes() }))
            features.add(DataModelInspectorFeature(spatial, this.componentManager))
        }
        return features
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = WeakReference(this)

        // Register custom components (generated from XML schemas)
        componentManager.registerComponent<TimeComponent>(TimeComponent.Companion)
        componentManager.registerComponent<ToolComponent>(ToolComponent.Companion)
        // Also register some built-in Interaction SDK components we plan to use:
        componentManager.registerComponent<IsdkGrabbable>(IsdkGrabbable.Companion)
        componentManager.registerComponent<IsdkPanelDimensions>(IsdkPanelDimensions.Companion)

        systemManager.registerSystem(UpdateTimeSystem())
        systemManager.registerSystem(UpdateAudioPositionSystem())

        AudioManager.instance.setCreationSound(SceneAudioAsset.loadLocalFile("audio/creation_audio.wav"))
        AudioManager.instance.setTimerSound(
            SceneAudioPlayer(
                scene,
                SceneAudioAsset.loadLocalFile("audio/completion_audio.wav")
            )
        )
    }

    private fun createDeleteButton() {
        val button = Entity.create(
            Mesh("mesh://box".toUri()),
            Box(Vector3(-0.015f, -0.015f, 0f), Vector3(0.015f, 0.015f, 0f)),
            Transform(Pose(Vector3(0f))),
            IsdkPanelDimensions(Vector2(0.04f, 0.04f)),
            Material().apply {
                baseTextureAndroidResourceId = R.drawable.delete
                alphaMode = 1
                unlit = true
            },
        )
        button.setComponent(Visible(false))
        deleteButton = button

        addOnSelectListener(button) { deleteObject(currentObjectSelected) }
    }


    override fun onSceneReady() {
        super.onSceneReady()
        // Use LOCAL so reentering works as expected
        scene.setReferenceSpace(ReferenceSpace.LOCAL)
        // Disable locomotion for easier panel interaction in this MVP
        systemManager.findSystem<LocomotionSystem>().enableLocomotion(false)
        // Enable mixed reality passthrough and add simple lighting so meshes are visible
        scene.enablePassthrough(true)
        scene.setLightingEnvironment(
            Vector3(2.5f, 2.5f, 2.5f), // ambient
            Vector3(1.8f, 1.8f, 1.8f), // sun
            -Vector3(1.0f, 3.0f, 2.0f)
        ) // direction

        val timerMenuPanel = Entity.createPanelEntity(
            R.id.timer_menu,
            Transform(Pose(Vector3(0f, 0f, 0f))),
            Visible(true)
        )

        placeInFront(timerMenuPanel)

        getHeadEntity()?.let { head ->
            timerMenuPanel.setComponent(
                Followable(
                    target = head,
                    offset = Pose(Vector3(0f, -0.1f, 0.75f)),
                    type = FollowableType.PIVOT_Y,
                )
            )
        }

        createDeleteButton()
    }

    @OptIn(SpatialSDKExperimentalAPI::class)
    override fun registerPanels(): List<PanelRegistration> {
        val widthMeters = 0.5f
        val heightMeters = 0.5f
        val layoutDpBase = 1800f
        return listOf(
            PanelRegistration(R.id.timer_menu) {
                config {
                    width = widthMeters
                    height = heightMeters
                    layoutWidthInDp = layoutDpBase * width
                    layoutHeightInDp = layoutDpBase * height
                    includeGlass = false
                    themeResourceId = R.style.PanelAppThemeTransparent
                }
                composePanel {
                    setContent {
                        TimerMenu { timerSelection ->
                            TimerTool(
                                hours = timerSelection.hours,
                                minutes = timerSelection.minutes,
                                seconds = timerSelection.seconds,
                                colorOption = timerSelection.colorOption,
                                snoozeMinutes = timerSelection.snoozeMinutes
                            )
                        }
                    }
                }
            }
        )
    }

    fun registerSnoozeButton(panelEntity: Entity, snoozeButton: Entity) {
        timerSnoozeButtons[panelEntity] = snoozeButton
    }

    fun showSnoozeButton(panelEntity: Entity) {
        timerSnoozeButtons[panelEntity]?.setComponent(Visible(true))
    }
}
