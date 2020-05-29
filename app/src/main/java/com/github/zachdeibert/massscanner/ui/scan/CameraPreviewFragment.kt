package com.github.zachdeibert.massscanner.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.util.iface.CameraCaptureSessionStateCallback
import com.github.zachdeibert.massscanner.util.iface.CameraDeviceStateCallback
import com.github.zachdeibert.massscanner.util.iface.convert

class CameraPreviewFragment : Fragment(), SurfaceHolder.Callback, CameraDeviceStateCallback, CameraCaptureSessionStateCallback {
    companion object {
        private const val TAG = "CameraPreviewFragment"
    }

    interface CameraListener {
        fun onCameraCharacteristicsUpdated(sender: CameraPreviewFragment)
    }
    interface CameraListener2 : CameraListener, ImageReader.OnImageAvailableListener

    private val cameraManager by lazy { requireActivity().getSystemService(CameraManager::class.java) }

    private lateinit var holder: SurfaceHolder
    private var camera: CameraDevice? = null
    private var surface: Surface? = null
    private var session: CameraCaptureSession? = null

    private var _cameraNumber: Int = 0
    var cameraNumber: Int
        get() = _cameraNumber
        set(value) {
            _cameraNumber = when {
                value < 0 -> 0
                value >= cameraCount -> cameraCount - 1
                else -> value
            }
            onCameraNumberChanged()
        }

    private var _imageSize: Size = Size(1080, 1920)
    var imageSize: Size
        get() = _imageSize
        set(value) {
            if (value != imageSize) {
                _imageSize = value
                onImageSizeChanged()
            }
        }

    private var _template: Int = CameraDevice.TEMPLATE_PREVIEW
    var template: Int
        get() = _template
        set(value) {
            _template = value
            onRequestParameterChanged()
        }
    val cameraCount: Int
        get() = cameraManager.cameraIdList.size

    private var _cameraListener: CameraListener? = null
    private var imageReader: ImageReader? = null
    var cameraListener: CameraListener?
        get() = _cameraListener
        set(value) {
            _cameraListener = value
            if (value == null) {
                if (imageReader != null) {
                    imageReader?.close()
                    imageReader = null
                    onSessionParameterChanged()
                }
                imageReader?.close()
                imageReader = null
            } else if (value is CameraListener2) {
                if (imageReader == null) {
                    imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, ImageFormat.YUV_420_888, 3)
                    imageReader?.setOnImageAvailableListener(value, null)
                    onSessionParameterChanged()
                } else {
                    imageReader?.setOnImageAvailableListener(value, null)
                }
            }
            if (characteristics != null) {
                fireCameraCharacteristicUpdated()
            }
        }
    protected fun fireCameraCharacteristicUpdated() {
        cameraListener?.onCameraCharacteristicsUpdated(this)
    }

    private val requestProperties = mutableMapOf<String, (CaptureRequest.Builder) -> Unit>()
    fun <T> setRequestProperty(key: CaptureRequest.Key<T>, value: T) {
        if (value == null) {
            requestProperties.remove(key.name)
        } else {
            requestProperties[key.name] = {
                it.set(key, value)
            }
        }
        onRequestParameterChanged()
    }

    private var characteristics: CameraCharacteristics? = null
    fun <T> getCharacteristic(key: CameraCharacteristics.Key<T>): T? = characteristics?.get(key)

    private var requestParameterChanged = false
    fun onRequestParameterChanged() {
        if (!requestParameterChanged) {
            requestParameterChanged = true
            Handler().post {
                requestParameterChanged = false
                val camera = this.camera
                val surface = this.surface
                val session = this.session
                val imageReader = this.imageReader
                if (camera != null && surface != null && session != null) {
                    val req = camera.createCaptureRequest(template).apply {
                        requestProperties.values.forEach { it(this) }
                        addTarget(surface)
                        if (imageReader != null) {
                            addTarget(imageReader.surface)
                        }
                    }.build()
                    session.setRepeatingRequest(req, null, null)
                }
            }
        }
    }

    private var sessionParameterChanged = false
    fun onSessionParameterChanged() {
        if (!sessionParameterChanged) {
            sessionParameterChanged = true
            Handler().post {
                sessionParameterChanged = false
                val camera = this.camera
                val surface = this.surface
                val imageReader = this.imageReader
                session?.close()
                session = null
                if (camera != null && surface != null) {
                    camera.createCaptureSession(if (imageReader == null) listOf(surface) else listOf(surface, imageReader.surface),
                        (this as CameraCaptureSessionStateCallback).convert(), null)
                }
            }
        }
    }

    private var cameraNumberChanged = false
    fun onCameraNumberChanged() {
        if (!cameraNumberChanged) {
            cameraNumberChanged = true
            Handler().post {
                cameraNumberChanged = false
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Missing permissions to operate camera.")
                    return@post
                }
                val id = cameraManager.cameraIdList[cameraNumber]
                characteristics = cameraManager.getCameraCharacteristics(id)
                session?.close()
                session = null
                camera?.close()
                camera = null
                cameraManager.openCamera(id, (this as CameraDeviceStateCallback).convert(), null)
                fireCameraCharacteristicUpdated()
            }
        }
    }

    private var imageSizeChanged = false
    fun onImageSizeChanged() {
        if (!imageSizeChanged) {
            imageSizeChanged = true
            Handler().post {
                imageSizeChanged = false
                holder.setFixedSize(imageSize.width, imageSize.height)
                if (imageReader != null) {
                    imageReader?.close()
                    imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, ImageFormat.YUV_420_888, 3)
                    imageReader?.setOnImageAvailableListener(cameraListener as ImageReader.OnImageAvailableListener, null)
                    onSessionParameterChanged()
                }
            }
        }
    }

    override fun surfaceChanged(_holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surface = holder.surface
        onSessionParameterChanged()
    }

    override fun surfaceDestroyed(_holder: SurfaceHolder?) {
        surface = null
    }

    override fun surfaceCreated(_holder: SurfaceHolder?) {
        surface = holder.surface
        onSessionParameterChanged()
    }

    override fun onOpened(camera: CameraDevice) {
        this.camera = camera
        onSessionParameterChanged()
    }

    override fun onDisconnected(camera: CameraDevice) {
        this.camera = null
        camera.close()
    }

    override fun onError(camera: CameraDevice, error: Int) {
        Log.e(TAG, "Camera error: $error")
        this.camera = null
        camera.close()
    }

    override fun onConfigureFailed(session: CameraCaptureSession) {
        Log.e(TAG, "Camera session configure failed")
        this.session = null
    }

    override fun onConfigured(session: CameraCaptureSession) {
        this.session = session
        onRequestParameterChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera_preview, container, false)
    }

    override fun onStart() {
        super.onStart()
        holder = requireView().findViewById<SurfaceView>(R.id.camera_surface).holder
        holder.addCallback(this)
    }

    override fun onResume() {
        super.onResume()
        onCameraNumberChanged()
        requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onPause() {
        super.onPause()
        session?.close()
        session = null
        camera?.close()
        camera = null
    }
}