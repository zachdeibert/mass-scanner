package com.github.zachdeibert.massscanner.ui.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.ui.PermissionActivity
import com.github.zachdeibert.massscanner.ui.UIUtil
import com.github.zachdeibert.massscanner.util.Race2
import com.github.zachdeibert.massscanner.util.Race3
import com.github.zachdeibert.massscanner.util.RaceBase

class ScanActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ScanActivity"
        private const val PERMISSIONS_ACTIVITY_CODE = 1
        private const val FOCUS_ACTIVITY_CODE = 2
    }

    private val model: ScanViewModel by viewModels()
    private val request = Race3<CameraDevice, Surface, CameraCaptureSession, CaptureRequest>().apply {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        producerCallback = object : Race3.ProducerCallback<CameraDevice, Surface, CameraCaptureSession, CaptureRequest>() {
            override fun produce(camera: CameraDevice, surface: Surface, session: CameraCaptureSession, finish: (CaptureRequest?) -> Unit) {
                try {
                    val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT).apply {
                        set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                        set(CaptureRequest.LENS_FOCUS_DISTANCE, model.focalDistance)
                        set(CaptureRequest.NOISE_REDUCTION_MODE, model.noiseFilter)
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
                request.c = session
            }
        }
    }
    private var camera: CameraDevice? = null
    private lateinit var scanStatus: TextView
    private lateinit var cameraSurfaceHolder: SurfaceHolder

    private fun updateStatus() {
        val scannedSize = UIUtil.formatFileSize(model.scannedSize, this)
        val freeSize = UIUtil.formatFileSize(model.prescanSpaceLeft - model.scannedSize, this)
        scanStatus.text = getString(R.string.scan_status, model.scannedPages, scannedSize.a, scannedSize.b, freeSize.a, freeSize.b)
    }

    private fun startCamera() {
        val manager = getSystemService(CameraManager::class.java)
        manager?.apply {
            val cameraId = cameraIdList[model.cameraNum]
            if (ActivityCompat.checkSelfPermission(
                    this@ScanActivity,
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
                    this@ScanActivity.camera = camera
                    val sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(SurfaceTexture::class.java)
                    if (sizes != null) {
                        cameraSurfaceHolder.setFixedSize(sizes[0].width, sizes[0].height)
                    }
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

    private fun enterFullscreen() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        enterFullscreen()
        scanStatus = findViewById(R.id.scan_status)
        cameraSurfaceHolder = findViewById<SurfaceView>(R.id.camera_surface).holder
        cameraSurfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int,width: Int, height: Int) {
                if (holder != null) {
                    cameraSurfaceHolder = holder
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
                    cameraSurfaceHolder = holder
                    session.b = holder.surface
                    request.b = holder.surface
                }
            }
        })
        val intent = Intent(this, PermissionActivity::class.java)
        startActivityForResult(intent, PERMISSIONS_ACTIVITY_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PERMISSIONS_ACTIVITY_CODE -> {
                val intent = Intent(this, FocusActivity::class.java)
                startActivityForResult(intent, FOCUS_ACTIVITY_CODE)
            }
            FOCUS_ACTIVITY_CODE -> {
                data?.apply {
                    model.cameraNum = getIntExtra(FocusActivity.RESULT_CAMERA_NUM, model.cameraNum)
                    model.focalDistance = getFloatExtra(FocusActivity.RESULT_FOCAL_DISTANCE, model.focalDistance)
                    model.noiseFilter = getIntExtra(FocusActivity.RESULT_NOISE_REDUCTION_FILTER, model.noiseFilter)
                    startCamera()
                    updateStatus()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun toggleScanner(v: View) {
        model.runScanner = !model.runScanner
        v.background = getDrawable(if (model.runScanner) R.drawable.ic_pause_circle_filled_white_48dp else R.drawable.ic_play_circle_filled_white_48dp)
    }

    fun finishScanning(@Suppress("UNUSED_PARAMETER") v: View) {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
