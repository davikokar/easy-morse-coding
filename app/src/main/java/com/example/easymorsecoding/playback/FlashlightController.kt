package com.example.easymorsecoding.playback

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.util.Log

class FlashlightController(private val context: Context) : MorsePlayer {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: CameraAccessException) {
            Log.e("FlashlightController", "Error accessing camera", e)
        }
    }

    fun hasFlashlight(): Boolean = cameraId != null

    override fun setOutput(active: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, active)
        } catch (e: Exception) {
            Log.e("FlashlightController", "Error setting torch mode", e)
        }
    }

    override fun stop() {
        setOutput(false)
    }
}
