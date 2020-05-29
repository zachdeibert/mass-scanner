package com.github.zachdeibert.massscanner.util.iface

import android.hardware.camera2.CameraDevice

interface CameraDeviceStateCallback {
    fun onOpened(camera: CameraDevice)
    fun onDisconnected(camera: CameraDevice)
    fun onError(camera: CameraDevice, error: Int)
}

fun CameraDeviceStateCallback.convert(): CameraDevice.StateCallback {
    return object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            this@convert.onOpened(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            this@convert.onDisconnected(camera)
        }

        override fun onError(camera: CameraDevice, error: Int) {
            this@convert.onError(camera, error)
        }
    }
}
