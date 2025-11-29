
package com.droidcon.desktimer.systems

import com.droidcon.desktimer.managers.AudioManager
import com.meta.spatial.core.SystemBase

class UpdateAudioPositionSystem : SystemBase() {
    override fun execute() {
        AudioManager.instance.updateAudioPositions()
    }
}
