package com.github.zachdeibert.massscanner.ui.scan

import android.hardware.camera2.CameraMetadata
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {
    var scannedPages: Int = 0
    var scannedSize: Long = 0
    var prescanSpaceLeft: Long = 0
    var hasSettings: Boolean = false
    var cameraNum: Int = 0
    var focalDistance: Float = 0f
    var noiseFilter: Int = CameraMetadata.NOISE_REDUCTION_MODE_OFF
    var runScanner: Boolean = true
}
