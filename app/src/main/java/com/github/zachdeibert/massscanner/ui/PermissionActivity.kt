package com.github.zachdeibert.massscanner.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.zachdeibert.massscanner.R

class PermissionActivity : AppCompatActivity() {
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val PERMISSION_REQUEST_CODE = 1
    }

    private var viewInflated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acceptPermissions(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewInflated = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (permissions.size != grantResults.size || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            if (!viewInflated) {
                setContentView(R.layout.activity_permission)
                viewInflated = true
            }
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    fun acceptPermissions(@Suppress("UNUSED_PARAMETER") v: View?) {
        val missingPerms = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (missingPerms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPerms, PERMISSION_REQUEST_CODE)
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
