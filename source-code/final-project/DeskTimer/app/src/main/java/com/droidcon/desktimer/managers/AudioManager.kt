package com.droidcon.desktimer.managers

import com.droidcon.desktimer.TimerActivity
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Vector3
import com.meta.spatial.runtime.SceneAudioAsset
import com.meta.spatial.runtime.SceneAudioPlayer
import com.meta.spatial.toolkit.Transform
import com.meta.spatial.toolkit.TransformParent

class AudioManager private constructor() {
    private val activity get() = TimerActivity.getInstance()
    private var timerSound: SceneAudioPlayer? = null
    private var creationSound: SceneAudioAsset? = null
    private val playingAudio = mutableMapOf<Entity, SceneAudioPlayer>()

    companion object {
        val instance: AudioManager by lazy { AudioManager() }
    }

    fun setTimerSound(player: SceneAudioPlayer) {
        timerSound = player
    }
    fun setCreationSound(asset: SceneAudioAsset) {
        creationSound = asset
    }

    fun playCreationSound(position: Vector3) {
        creationSound?.let { asset ->
            activity?.scene?.playSound(asset, position, volume = 1f)
        }
    }

    fun playTimerSound(target: Entity) {
        val targetTimer = target.tryGetComponent<TransformParent>()?.entity ?: return
        timerSound?.let { player ->
            if (targetTimer.hasComponent<Transform>()) {
                val pos = targetTimer.getComponent<Transform>().transform.t
                player.play(pos, volume = 0.2f, looping = true)
                playingAudio[targetTimer] = player
            }
        }
    }

    fun updateAudioPositions() {
        val entries = playingAudio.entries.toList()
        for ((entity, player) in entries) {
            if (entity.hasComponent<Transform>()) {
                val pos = entity.getComponent<Transform>().transform.t
                player.setPosition(pos)
            } else {
                playingAudio.remove(entity)
            }
        }
    }

    fun stopTimerSound(target: Entity) {
        playingAudio[target]?.let { player ->
            player.stop()
            playingAudio.remove(target)
        }
    }
}
