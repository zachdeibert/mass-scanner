package com.github.zachdeibert.massscanner.ui.scan

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.gesture.Gesture
import android.hardware.camera2.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.util.Race2
import com.github.zachdeibert.massscanner.util.Race4
import com.github.zachdeibert.massscanner.util.RaceBase
import kotlinx.android.synthetic.main.activity_focus.view.*
import java.lang.Exception
import java.lang.IllegalStateException
import kotlin.math.abs

class FocusActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "FocusActivity"
        private const val PERMISSION_REQUEST_CODE = 1
        private const val FLING_VELOCITY_THRESHOLD = 10000
    }

    private val model: FocusViewModel by viewModels()
    private val request = Race4<CameraDevice, Surface, Float, CameraCaptureSession, CaptureRequest>().apply {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        producerCallback = object : Race4.ProducerCallback<CameraDevice, Surface, Float, CameraCaptureSession, CaptureRequest>() {
            override fun produce(camera: CameraDevice, surface: Surface, focus: Float, session: CameraCaptureSession, finish: (CaptureRequest?) -> Unit) {
                try {
                    val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                        set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                        set(CaptureRequest.LENS_FOCUS_DISTANCE, focus)
                        addTarget(surface)
                    }.build()
                    session.setRepeatingRequest(req, null, null)
                    finish(req)
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not create capture request", ex)
                    finish(null)
                }
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
                try {
                    camera.createCaptureSession(
                        listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.e(TAG, "Camera session configuration failed")
                                finish(null)
                            }

                            override fun onConfigured(session: CameraCaptureSession) {
                                finish(session)
                            }
                        },
                        null
                    )
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not open capture session", ex)
                    finish(null)
                }
            }

            override fun close(session: CameraCaptureSession, finish: () -> Unit) {
                session.close()
                finish()
            }
        }
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        consumerCallback = object : RaceBase.ConsumerCallback<CameraCaptureSession>() {
            override fun consume(session: CameraCaptureSession) {
                request.d = session
            }
        }
    }
    private var camera: CameraDevice? = null
    private var minFocus: Float = 0f
    private var hyperFocal: Float = 0f
    private var calibrated: Boolean = false
    private var focusStatus: TextView? = null
    private lateinit var gestureDetector: GestureDetectorCompat

    private fun enterFullscreen() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private fun refocus() {
        request.c = model.focusDistance
        focusStatus?.text = getString(R.string.focus_status, 100 / model.focusDistance,
            getString(if (calibrated) R.string.focus_units_calibrated else R.string.focus_units_uncalibrated))
    }

    private fun startCamera() {
        val manager = getSystemService(CameraManager::class.java)
        manager?.apply {
            if (model.cameraNo >= cameraIdList.size) {
                model.cameraNo = 0
            }
            val cameraId = cameraIdList[model.cameraNo]
            if (ActivityCompat.checkSelfPermission(
                    this@FocusActivity,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Permission check failed")
                return
            }
            val characteristics = getCameraCharacteristics(cameraId)
            openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    session.a = camera
                    request.a = camera
                    this@FocusActivity.camera = camera
                    minFocus = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f
                    hyperFocal = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE) ?: 0f
                    val cal = characteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)
                    calibrated = cal == CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED ||
                            cal == CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE
                    model.focusDistance = hyperFocal
                    refocus()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    camera.close()
                }
            }, null)
        }
    }

    private fun freeCamera() {
        request.close()
        session.close()
        camera?.close()
        camera = null
    }

    fun nextCamera(v: View) {
        ++model.cameraNo
        camera?.close()
        camera = null
        startCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus)
        focusStatus = findViewById(R.id.focus_status)
        enterFullscreen()
        findViewById<SurfaceView>(R.id.camera_surface).holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                if (holder != null) {
                    session.b = holder.surface
                    request.b = holder.surface
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                if (holder != null) {
                    session.close()
                    request.close()
                }
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (holder != null) {
                    session.b = holder.surface
                    request.b = holder.surface
                }
            }
        })
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
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                // Up (positive) - decrease diopters, increase focal distance
                // Down (negative) - increase diopters, decrease focal distance
                var metrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(metrics)
                model.focusDistance -= distanceY * minFocus / metrics.heightPixels
                if (model.focusDistance < 0) {
                    model.focusDistance = 0f
                }
                if (model.focusDistance > minFocus) {
                    model.focusDistance = minFocus
                }
                refocus()
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                // Up (negative) - decrease diopters to zero, increase focal distance to infinity
                // Down (positive) - increase diopters to max, decrease focal distance to minimum
                if (abs(velocityY) > FLING_VELOCITY_THRESHOLD) {
                    if (velocityY < 0) {
                        model.focusDistance = 0f
                    } else {
                        model.focusDistance = minFocus
                    }
                    refocus()
                }
                return true
            }
        })
        gestureDetector.setIsLongpressEnabled(false)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (gestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
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
