package com.droidcon.desktimer.utils

import com.droidcon.desktimer.TimerActivity
import com.droidcon.desktimer.ToolComponent
import com.droidcon.desktimer.managers.AudioManager
import com.droidcon.desktimer.managers.TimerStateManager
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Query
import com.meta.spatial.core.Vector3
import com.meta.spatial.isdk.IsdkGrabbable
import com.meta.spatial.runtime.ButtonDownEventArgs
import com.meta.spatial.runtime.ControllerButton
import com.meta.spatial.toolkit.AvatarAttachment
import com.meta.spatial.toolkit.Panel
import com.meta.spatial.toolkit.Transform
import com.meta.spatial.toolkit.TransformParent
import com.meta.spatial.toolkit.Visible

var temporalID: Int = 0

fun getDisposableID(): Int {
    temporalID += 1
    return temporalID
}

fun addDeleteButton(entity: Entity) {
    addOnSelectListener(entity) { selectElement(entity) }
}

fun selectElement(ent: Entity) {
    val activity = TimerActivity.getInstance() ?: return
    val deleteButton = activity.deleteButton ?: return
    val toolComponent = ent.tryGetComponent<ToolComponent>() ?: return

    activity.currentObjectSelected = ent

    val billboardOrientationEuler: Vector3 =
        ent.tryGetComponent<IsdkGrabbable>()?.billboardOrientation ?: Vector3(0f, 0f, 0f)

    deleteButton.setComponent(
        Transform(
            Pose(
                toolComponent.deleteButtonPosition,
                Quaternion(
                    billboardOrientationEuler.x,
                    billboardOrientationEuler.y,
                    billboardOrientationEuler.z,
                ),
            )))

    deleteButton.setComponent(TransformParent(ent))
    deleteButton.setComponent(Visible(true))
}

fun deleteObject(entity: Entity?) {
    val activity = TimerActivity.getInstance() ?: return
    val target = entity ?: return
    val deleteButton = activity.deleteButton

    if (activity.currentObjectSelected == target) {
        activity.currentObjectSelected = null
    }

    deleteButton?.setComponent(TransformParent())
    deleteButton?.setComponent(Visible(false))

    fun removeTimerState(ent: Entity) {
        ent.tryGetComponent<Panel>()?.let { panel ->
            TimerStateManager.removeTimer(panel.panelRegistrationId.toString())
        }
    }

    removeTimerState(target)

    val children = getChildren(target)
    for (i in children.count() - 1 downTo 0) {
        val child = children[i]
        removeTimerState(child)
        AudioManager.instance.stopTimerSound(child)
        child.destroy()
    }

    val toolComponent = target.tryGetComponent<ToolComponent>()
    if (toolComponent != null) {
        AudioManager.instance.stopTimerSound(target)
    }

    target.destroy()
}

fun getChildren(parent: Entity): MutableList<Entity> {
    val children: MutableList<Entity> = mutableListOf()
    val candidates = Query.where { has(TransformParent.id) }.eval()
    for (child in candidates) {
        val transformParent = child.tryGetComponent<TransformParent>() ?: continue
        if (transformParent.entity == parent) {
            children.add(child)
        }
    }
    return children
}


fun addOnSelectListener(entity: Entity, onClick: () -> Unit) {
    entity.registerEventListener<ButtonDownEventArgs>(ButtonDownEventArgs.EVENT_NAME) { _, eventArgs ->
        if (
            eventArgs.button == ControllerButton.A ||
            eventArgs.button == ControllerButton.X ||
            eventArgs.button == ControllerButton.RightTrigger ||
            eventArgs.button == ControllerButton.LeftTrigger ||
            eventArgs.button == ControllerButton.RightSqueeze ||
            eventArgs.button == ControllerButton.LeftSqueeze
        ) {
            onClick()
        }
    }
}

// Function to get the pose of the user's head
fun getHeadEntity(): Entity? {
        return Query.where { has(AvatarAttachment.id) }
            .eval()
            .filter { it.isLocal() && it.getComponent<AvatarAttachment>().type == "head" }
            .firstOrNull()
}

fun getHeadPose(): Pose {
    val head =
        Query.where { has(AvatarAttachment.id) }
            .eval()
            .filter { it.isLocal() && it.getComponent<AvatarAttachment>().type == "head" }
            .first()
    return head.getComponent<Transform>().transform
}

// Approximate "place in front": use head attachment pose and offset a bit forward
fun placeInFront(entity: Entity?, offset: Vector3 = Vector3(0f)) {
    val headPose: Pose = getHeadPose()

    val height = 0.1f
    val distanceFromUser =  0.7f

    // Having the users position, we place the entity in front of it, at a particular distance and
    // height
    var newPos = headPose.t + headPose.q * Vector3.Forward * distanceFromUser
    newPos.y = headPose.t.y - height

    // If there is an offset vector, we place the object at the vector position (using user's position
    // as reference)
    if (offset != Vector3(0f)) {
        newPos =
            headPose.t + headPose.q * Vector3.Right * offset.x + headPose.q * Vector3.Forward * offset.z

        newPos.y = headPose.t.y + offset.y
    }

    // Add rotation to look in same vector direction as user
    var newRot = Quaternion.lookRotation(newPos - headPose.t)
    val billboardOrientationEuler: Vector3 =
        entity?.tryGetComponent<IsdkGrabbable>()?.billboardOrientation ?: Vector3(0f, 0f, 0f)

    newRot *= Quaternion(
        billboardOrientationEuler.x,
        billboardOrientationEuler.y,
        billboardOrientationEuler.z)

    entity?.setComponent(Transform(Pose(newPos, newRot)))
}