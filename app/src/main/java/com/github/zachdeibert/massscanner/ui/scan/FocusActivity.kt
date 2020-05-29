package com.github.zachdeibert.massscanner.ui.scan

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.FragmentActivity
import com.github.zachdeibert.massscanner.R
import kotlin.math.abs
import kotlin.math.roundToInt

class FocusActivity : FragmentActivity(), CameraPreviewFragment.CameraListener {
    companion object {
        private const val FLING_VELOCITY_THRESHOLD = 10000
        const val RESULT_CAMERA_NUM = "camera_num"
        const val RESULT_FOCAL_DISTANCE = "focus"
        const val RESULT_NOISE_REDUCTION_FILTER = "noise_filter"
        val NOISE_REDUCTION_PRIORITY = arrayOf(
            CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY,
            CameraMetadata.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG,
            CameraMetadata.NOISE_REDUCTION_MODE_FAST,
            CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL,
            CameraMetadata.NOISE_REDUCTION_MODE_OFF)
    }

    private val model: FocusViewModel by viewModels()
    private var minFocus: Float = 0f
    private var calibrated: Boolean = false
    private var maxZoom: Float = 1f
    private var noiseReduction: Int = CameraMetadata.NOISE_REDUCTION_MODE_OFF
    private var multifingerGesture: Boolean = false
    private lateinit var focusStatus: TextView
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var camera: CameraPreviewFragment

    private fun onFocusChange() {
        camera.setRequestProperty(CaptureRequest.LENS_FOCUS_DISTANCE, model.focusDistance)
        focusStatus.text = getString(R.string.focus_status, 100 / model.focusDistance,
            getString(if (calibrated) R.string.focus_units_calibrated else R.string.focus_units_uncalibrated))
    }

    private fun onZoom() {
        val sensorArray = camera.imageSize
        val width = (sensorArray.width / model.zoom).roundToInt()
        val height = (sensorArray.height / model.zoom).roundToInt()
        camera.setRequestProperty(CaptureRequest.SCALER_CROP_REGION, Rect(
            (sensorArray.width - width) / 2,
            (sensorArray.height - height) / 2,
            (sensorArray.width + width) / 2,
            (sensorArray.height + height) / 2))
    }

    override fun onCameraCharacteristicsUpdated(sender: CameraPreviewFragment) {
        minFocus = camera.getCharacteristic(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f
        model.focusDistance = camera.getCharacteristic(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE) ?: 0f
        val cal = camera.getCharacteristic(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)
        calibrated = cal == CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_CALIBRATED ||
                cal == CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION_APPROXIMATE
        maxZoom = camera.getCharacteristic(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f
        val sizes = camera.getCharacteristic(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(SurfaceTexture::class.java)
        if (sizes != null) {
            camera.imageSize = sizes[0]
        }
        val modes = camera.getCharacteristic(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES)
        noiseReduction = NOISE_REDUCTION_PRIORITY.firstOrNull { modes?.contains(it) == true } ?: CameraMetadata.NOISE_REDUCTION_MODE_OFF
        camera.setRequestProperty(CaptureRequest.NOISE_REDUCTION_MODE, noiseReduction)
        onFocusChange()
    }

    fun nextCamera(@Suppress("UNUSED_PARAMETER") v: View) {
        if (camera.cameraNumber == camera.cameraCount - 1) {
            camera.cameraNumber = 0
        } else {
            ++camera.cameraNumber
        }
        model.cameraNo = camera.cameraNumber
    }

    fun confirmSettings(@Suppress("UNUSED_PARAMETER") v: View) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(RESULT_CAMERA_NUM, model.cameraNo)
            putExtra(RESULT_FOCAL_DISTANCE, model.focusDistance)
            putExtra(RESULT_NOISE_REDUCTION_FILTER, noiseReduction)
        })
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus)
        focusStatus = findViewById(R.id.focus_status)
        camera = supportFragmentManager.findFragmentById(R.id.camera) as CameraPreviewFragment
        camera.cameraListener = this
        camera.setRequestProperty(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                if (!multifingerGesture) {
                    // Up (positive) - decrease diopters, increase focal distance
                    // Down (negative) - increase diopters, decrease focal distance
                    val metrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(metrics)
                    model.focusDistance -= distanceY * minFocus / metrics.heightPixels
                    if (model.focusDistance < 0) {
                        model.focusDistance = 0f
                    }
                    if (model.focusDistance > minFocus) {
                        model.focusDistance = minFocus
                    }
                    onFocusChange()
                    return true
                }
                return false
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                // Up (negative) - decrease diopters to zero, increase focal distance to infinity
                // Down (positive) - increase diopters to max, decrease focal distance to minimum
                if (abs(velocityY) > FLING_VELOCITY_THRESHOLD && !multifingerGesture) {
                    if (velocityY < 0) {
                        model.focusDistance = 0f
                    } else {
                        model.focusDistance = minFocus
                    }
                    onFocusChange()
                    return true
                }
                return false
            }

            override fun onDown(e: MotionEvent?): Boolean {
                multifingerGesture = false
                return false
            }
        })
        gestureDetector.setIsLongpressEnabled(false)
        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                multifingerGesture = true
                model.zoom *= detector!!.scaleFactor
                if (model.zoom > maxZoom) {
                    model.zoom = maxZoom
                }
                if (model.zoom < 1) {
                    model.zoom = 1f
                }
                onZoom()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val scaled = scaleDetector.onTouchEvent(event)
        return when {
            event?.pointerCount == 1 && gestureDetector.onTouchEvent(event) -> true
            scaled -> true
            else -> super.onTouchEvent(event)
        }
    }
}
