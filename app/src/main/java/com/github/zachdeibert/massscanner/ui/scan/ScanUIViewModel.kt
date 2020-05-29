package com.github.zachdeibert.massscanner.ui.scan

import androidx.lifecycle.ViewModel

class ScanUIViewModel : ViewModel() {
    var scannedPages: Int = 0
    var scannedSize: Long = 0
    var prescanSpaceLeft: Long = 0
}
