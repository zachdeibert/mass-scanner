package com.github.zachdeibert.massscanner.ui.scan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
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
import com.github.zachdeibert.massscanner.util.Race4
import com.github.zachdeibert.massscanner.util.RaceBase
import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ScanActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ScanActivity"
        private const val PERMISSIONS_ACTIVITY_CODE = 1
        private const val FOCUS_ACTIVITY_CODE = 2
        private const val DEFAULT_IMAGE_WIDTH = 1080
        private const val DEFAULT_IMAGE_HEIGHT = 1920
    }

    private val model: ScanViewModel by viewModels()
    private val request = Race4<CameraDevice, Surface, Surface, CameraCaptureSession, CaptureRequest>().apply {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        producerCallback = object : Race4.ProducerCallback<CameraDevice, Surface, Surface, CameraCaptureSession, CaptureRequest>() {
            override fun produce(camera: CameraDevice, surface1: Surface, surface2: Surface, session: CameraCaptureSession, finish: (CaptureRequest?) -> Unit) {
                try {
                    val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT).apply {
                        set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                        set(CaptureRequest.LENS_FOCUS_DISTANCE, model.focalDistance)
                        set(CaptureRequest.NOISE_REDUCTION_MODE, model.noiseFilter)
                        addTarget(surface1)
                        addTarget(surface2)
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
    private val session = Race3<CameraDevice, Surface, Surface, CameraCaptureSession>().apply {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        producerCallback = object : Race3.ProducerCallback<CameraDevice, Surface, Surface, CameraCaptureSession>() {
            override fun produce(camera: CameraDevice, surface1: Surface, surface2: Surface, finish: (CameraCaptureSession?) -> Unit) {
                try {
                    camera.createCaptureSession(
                        listOf(surface1, surface2),
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
    private var imageReader: ImageReader? = null
    private var scanningThread: Thread? = null
    private lateinit var scanStatus: TextView
    private lateinit var cameraSurfaceHolder: SurfaceHolder
    private lateinit var augmentSurfaceHolder: SurfaceHolder
    private var augmentSurface: Surface? = null
    private val scanThreadLock = ReentrantLock()
    private val scanThreadLockCondition = scanThreadLock.newCondition()

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
            Log.d(TAG, "Opening camera...")
            openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "Camera opened.")
                    val sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(SurfaceTexture::class.java)
                    val width: Int
                    val height: Int
                    if (sizes != null) {
                        width = sizes[0].width
                        height = sizes[0].height
                    } else {
                        width = DEFAULT_IMAGE_WIDTH
                        height = DEFAULT_IMAGE_HEIGHT
                    }
                    cameraSurfaceHolder.setFixedSize(width, height)
                    augmentSurfaceHolder.setFixedSize(width, height)
                    if (imageReader == null) {
                        imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 3).also {
                            it.setOnImageAvailableListener({
                                scanThreadLock.withLock {
                                    scanThreadLockCondition.signalAll()
                                }
                            }, null)
                        }.apply {
                            session.c = surface
                            request.c = surface
                        }
                        scanningThread?.join()
                        scanningThread = null
                    }
                    if (scanningThread == null) {
                        scanningThread = Thread {
                            val native = ScanNative()
                            val bitmapBuf = ByteBuffer.allocateDirect(4 * width * height)
                            val paint = Paint()
                            paint.color = getColor(R.color.scan_augment_box)
                            paint.strokeCap = Paint.Cap.ROUND
                            paint.strokeJoin = Paint.Join.ROUND
                            paint.strokeWidth = 10f
                            paint.style = Paint.Style.STROKE
                            while (imageReader != null) {
                                also {
                                    var image: Image? = null
                                    scanThreadLock.withLock {
                                        if (imageReader == null) {
                                            return@also
                                        }
                                        if (!model.runScanner) {
                                            scanThreadLockCondition.await()
                                            return@also
                                        }
                                        image = imageReader!!.acquireLatestImage()
                                        if (image == null) {
                                            scanThreadLockCondition.await()
                                            return@also
                                        }
                                    }
                                    val res = native.processFrame(image!!, bitmapBuf)
                                    image!!.close()
                                    if (res.augment.size == 4) {
                                        scanThreadLock.withLock {
                                            if (model.runScanner) {
                                                augmentSurface?.apply {
                                                    val c = lockCanvas(null)
                                                    c.drawLines(
                                                        floatArrayOf(
                                                            res.augment[0].x.toFloat(), res.augment[0].y.toFloat(), res.augment[1].x.toFloat(), res.augment[1].y.toFloat(),
                                                            res.augment[1].x.toFloat(), res.augment[1].y.toFloat(), res.augment[2].x.toFloat(), res.augment[2].y.toFloat(),
                                                            res.augment[2].x.toFloat(), res.augment[2].y.toFloat(), res.augment[3].x.toFloat(), res.augment[3].y.toFloat(),
                                                            res.augment[3].x.toFloat(), res.augment[3].y.toFloat(), res.augment[0].x.toFloat(), res.augment[0].y.toFloat()
                                                        ), paint
                                                    )
                                                    unlockCanvasAndPost(c)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            native.close()
                            scanningThread = null
                        }.apply {
                            start()
                        }
                    }
                    session.a = camera
                    request.a = camera
                    this@ScanActivity.camera = camera
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d(TAG, "Camera disconnected.")
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
        imageReader?.apply {
            imageReader = null
            scanThreadLock.withLock {
                scanThreadLockCondition.signalAll()
            }
            scanningThread?.join()
            scanningThread = null
            close()
        }
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
        augmentSurfaceHolder = findViewById<SurfaceView>(R.id.augmentation_surface).holder
        augmentSurfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                if (holder != null) {
                    augmentSurfaceHolder = holder
                    augmentSurface = holder.surface
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                if (holder != null) {
                    augmentSurface = null
                }
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (holder != null) {
                    augmentSurfaceHolder = holder
                    augmentSurface = holder.surface
                }
            }
        })
        val intent = Intent(this, PermissionActivity::class.java)
        startActivityForResult(intent, PERMISSIONS_ACTIVITY_CODE)
    }

    override fun onResume() {
        super.onResume()
        enterFullscreen()
        if (model.hasSettings) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        freeCamera()
    }

    override fun onStop() {
        super.onStop()
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
                    model.hasSettings = true
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
        scanThreadLock.withLock {
            scanThreadLockCondition.signalAll()
            if (!model.runScanner) {
                augmentSurface?.apply {
                    val c = lockCanvas(null)
                    unlockCanvasAndPost(c)
                }
            }
        }
    }

    fun finishScanning(@Suppress("UNUSED_PARAMETER") v: View) {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
