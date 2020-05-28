package com.github.zachdeibert.massscanner.ui.scan

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.util.Race2
import com.github.zachdeibert.massscanner.util.Race4
import com.github.zachdeibert.massscanner.util.RaceBase

class FocusActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "FocusActivity"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    private val model: FocusViewModel by viewModels()
    private val request = Race4<CameraDevice, Surface, Float, CameraCaptureSession, CaptureRequest>().apply {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        producerCallback = object : Race4.ProducerCallback<CameraDevice, Surface, Float, CameraCaptureSession, CaptureRequest>() {
            override fun produce(camera: CameraDevice, surface: Surface, focus: Float, session: CameraCaptureSession, finish: (CaptureRequest?) -> Unit) {
                Log.d(TAG, "Starting capture request...")
                val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                    set(CaptureRequest.LENS_FOCUS_DISTANCE, focus)
                    addTarget(surface)
                }.build()
                session.setRepeatingRequest(req, null, null)
                finish(req)
            }

            override fun close(req: CaptureRequest, finish: () -> Unit) {
                finish()
            }
        }
    }
    private val session = Race2<CameraDevice, Surface, CameraCaptureSession>().apply {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        producerCallback = object : Race2.ProducerCallback<CameraDevice, Surface, CameraCaptureSession>() {
            override fun produce(camera: CameraDevice, surface: Surface, finish: (CameraCaptureSession?) -> Unit) {
                Log.d(TAG, "Creating session...")
                camera.createCaptureSession(
                    listOf(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e(TAG, "Camera session configuration failed")
                            finish(null)
                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            Log.d(TAG, "Camera session configured")
                            finish(session)
                        }
                    },
                    null
                )
            }

            override fun close(session: CameraCaptureSession, finish: () -> Unit) {
                session.close()
                finish()
            }
        }
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        consumerCallback = object : RaceBase.ConsumerCallback<CameraCaptureSession>() {
            override fun consume(session: CameraCaptureSession) {
                Log.d(TAG, "Proxying session variable")
                request.d = session
            }
        }
    }
    private var camera: CameraDevice? = null

    private fun enterFullscreen() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private fun startCamera() {
        val manager = getSystemService(CameraManager::class.java)
        manager?.apply {
            val cameraId = cameraIdList[model.cameraNo]
            if (ActivityCompat.checkSelfPermission(
                    this@FocusActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Permission check failed")
                return
            }
            openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "Camera opened")
                    session.a = camera
                    request.a = camera
                    this@FocusActivity.camera = camera
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d(TAG, "Camera disconnected")
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    camera.close()
                }
            }, null)
        }
        request.c = model.focusDistance
    }

    private fun freeCamera() {
        request.close()
        session.close()
        camera?.close()
        camera = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus)
        findViewById<SurfaceView>(R.id.camera_surface).holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                Log.d(TAG, "Surface changed")
                if (holder != null) {
                    session.b = holder.surface
                    request.b = holder.surface
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                Log.d(TAG, "Surface destroyed")
                if (holder != null) {
                    session.close()
                    request.close()
                }
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                Log.d(TAG, "Surface created")
                if (holder != null) {
                    session.b = holder.surface
                    request.b = holder.surface
                }
            }
        })
        enterFullscreen()
        val missingPerms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (missingPerms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPerms, PERMISSION_REQUEST_CODE)
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (permissions.size != grantResults.size || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            startCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        enterFullscreen()
        startCamera()
    }

    override fun onPause() {
        super.onPause()
        freeCamera()
    }
}
