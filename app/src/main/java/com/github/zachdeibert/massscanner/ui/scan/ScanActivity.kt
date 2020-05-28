package com.github.zachdeibert.massscanner.ui.scan

import android.content.Intent
import android.hardware.camera2.CameraMetadata
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.ui.PermissionActivity

class ScanActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_ACTIVITY_CODE = 1
        private const val FOCUS_ACTIVITY_CODE = 2
        val NOISE_REDUCTION_PRIORITY = arrayOf(
            CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY,
            CameraMetadata.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG,
            CameraMetadata.NOISE_REDUCTION_MODE_FAST,
            CameraMetadata.NOISE_REDUCTION_MODE_MINIMAL,
            CameraMetadata.NOISE_REDUCTION_MODE_OFF)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        val intent = Intent(this, PermissionActivity::class.java)
        startActivityForResult(intent, PERMISSIONS_ACTIVITY_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PERMISSIONS_ACTIVITY_CODE -> {
                val intent = Intent(this, FocusActivity::class.java)
                startActivityForResult(intent, FOCUS_ACTIVITY_CODE)
            }
            FOCUS_ACTIVITY_CODE -> {

            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
