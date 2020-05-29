package com.github.zachdeibert.massscanner.ui.scan

import android.app.Activity
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.ui.PermissionActivity

class ScanActivity : FragmentActivity(), CameraPreviewFragment.CameraListener2, ScanUIFragment.CommandListener {
    companion object {
        private const val TAG = "ScanActivity"
        private const val PERMISSIONS_ACTIVITY_CODE = 1
        private const val FOCUS_ACTIVITY_CODE = 2
    }

    private val model: ScanViewModel by viewModels()
    private lateinit var preview: CameraPreviewFragment
    private lateinit var augment: ScanAugmentView
    private lateinit var ui: ScanUIFragment
    private lateinit var thread: ScanThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        supportFragmentManager.apply {
            preview = findFragmentById(R.id.camera) as CameraPreviewFragment
            augment = findViewById(R.id.augment)
            ui = findFragmentById(R.id.ui) as ScanUIFragment
        }
        preview.template = CameraDevice.TEMPLATE_VIDEO_SNAPSHOT
        preview.cameraListener = this
        preview.setRequestProperty(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
        ui.commandListener = this
        val intent = Intent(this, PermissionActivity::class.java)
        startActivityForResult(intent, PERMISSIONS_ACTIVITY_CODE)
    }

    override fun onStart() {
        super.onStart()
        thread = ScanThread()
        thread.analysisListener = ScanThread.AnalysisListener.join(augment, ui)
        thread.start()
    }

    private fun loadCameraSettings() {
        preview.cameraNumber = model.cameraNum
        preview.setRequestProperty(CaptureRequest.NOISE_REDUCTION_MODE, model.noiseFilter)
        preview.setRequestProperty(CaptureRequest.LENS_FOCUS_DISTANCE, model.focalDistance)
    }

    override fun onResume() {
        super.onResume()
        loadCameraSettings()
        ui.runScanner = model.runScanner
        if (model.runScanner) {
            thread.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        thread.onPause()
    }

    override fun onStop() {
        super.onStop()
        thread.onStop()
        thread.close()
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
                    loadCameraSettings()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCameraCharacteristicsUpdated(sender: CameraPreviewFragment) {
        val sizes = preview.getCharacteristic(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(SurfaceTexture::class.java)
        if (sizes != null) {
            preview.imageSize = sizes[0]
            augment.imageSize = sizes[0]
            thread.maxImageSize = sizes[0]
            Log.i(TAG, "Capture size: ${sizes[0].width}x${sizes[0].height}")
        }
    }

    override fun onImageAvailable(reader: ImageReader?) {
        thread.onImageAvailable(reader)
    }

    override fun onScannerStateUpdate(sender: ScanUIFragment, run: Boolean) {
        model.runScanner = run
        if (run) {
            thread.onResume()
        } else {
            thread.onPause()
        }
    }

    override fun onFinishScanning(sender: ScanUIFragment) {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
