package com.github.zachdeibert.massscanner.util.iface

import android.hardware.camera2.CameraCaptureSession

interface CameraCaptureSessionStateCallback {
    fun onConfigureFailed(session: CameraCaptureSession)
    fun onConfigured(session: CameraCaptureSession)
}

fun CameraCaptureSessionStateCallback.convert(): CameraCaptureSession.StateCallback {
    return object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            this@convert.onConfigureFailed(session)
        }

        override fun onConfigured(session: CameraCaptureSession) {
            this@convert.onConfigured(session)
        }
    }
}
