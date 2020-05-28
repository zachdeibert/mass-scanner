package com.github.zachdeibert.massscanner.ui.scan

import androidx.lifecycle.ViewModel

class FocusViewModel : ViewModel() {
    var cameraNo: Int = 0
    var focusDistance: Float = 0f
    var zoom: Float = 1f
}
