package com.github.zachdeibert.massscanner.ui.scan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.zachdeibert.massscanner.R

class ScanActivity : AppCompatActivity() {
    companion object {
        private const val FOCUS_ACTIVITY_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        val intent = Intent(this, FocusActivity::class.java)
        startActivityForResult(intent, FOCUS_ACTIVITY_CODE)
    }
}
