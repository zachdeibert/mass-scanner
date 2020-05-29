package com.github.zachdeibert.massscanner.ui.scan

import android.hardware.camera2.CameraMetadata
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {
    var cameraNum: Int = 0
    var focalDistance: Float = 0f
    var noiseFilter: Int = CameraMetadata.NOISE_REDUCTION_MODE_OFF
    var runScanner: Boolean = true
}
