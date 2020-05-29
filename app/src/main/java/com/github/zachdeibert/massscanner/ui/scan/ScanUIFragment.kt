package com.github.zachdeibert.massscanner.ui.scan

import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels

import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.ui.UIUtil

class ScanUIFragment : Fragment(), ScanThread.AnalysisListener {
    interface CommandListener {
        fun onScannerStateUpdate(sender: ScanUIFragment, run: Boolean)
        fun onFlashUpdate(sender: ScanUIFragment, useFlash: Boolean)
        fun onFinishScanning(sender: ScanUIFragment)
    }

    private val model: ScanUIViewModel by activityViewModels()
    private lateinit var scanStatus: TextView
    private lateinit var pauseBtn: Button
    private lateinit var flashBtn: Button

    private var _runScanner = false
    var runScanner: Boolean
        get() = _runScanner
        set(value) {
            if (value != runScanner) {
                _runScanner = value
                pauseBtn.background = requireActivity().getDrawable(
                    if (value) R.drawable.ic_pause_circle_filled_white_48dp
                    else R.drawable.ic_play_circle_filled_white_48dp
                )
            }
        }

    private var _useFlash = false
    var useFlash: Boolean
        get() = _useFlash
        set(value) {
            if (value != useFlash) {
                _useFlash = value
                flashBtn.background = requireActivity().getDrawable(
                    if (value) R.drawable.ic_flash_on_white_48dp
                    else R.drawable.ic_flash_off_white_48dp
                )
            }
        }

    var commandListener: CommandListener? = null

    protected fun fireScannerStateUpdate() {
        commandListener?.onScannerStateUpdate(this, runScanner)
    }

    protected fun fireFlashUpdate() {
        commandListener?.onFlashUpdate(this, useFlash)
    }

    protected fun fireFinishScanning() {
        commandListener?.onFinishScanning(this)
    }

    private fun onStatusUpdate() {
        val scannedSize = UIUtil.formatFileSize(model.scannedSize, requireContext())
        val freeSize = UIUtil.formatFileSize(model.prescanSpaceLeft - model.scannedSize, requireContext())
        scanStatus.text = getString(R.string.scan_status, model.scannedPages, scannedSize.a, scannedSize.b, freeSize.a, freeSize.b)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scan_ui, container, false)
    }

    override fun onStart() {
        super.onStart()
        requireView().apply {
            scanStatus = findViewById(R.id.scan_status)
            pauseBtn = findViewById<Button>(R.id.pause_button).apply {
                setOnClickListener {
                    runScanner = !runScanner
                    fireScannerStateUpdate()
                }
            }
            flashBtn = findViewById<Button>(R.id.flash_button).apply {
                setOnClickListener {
                    useFlash = !useFlash
                    fireFlashUpdate()
                }
            }
            findViewById<Button>(R.id.finish_button).setOnClickListener {
                fireFinishScanning()
            }
        }
        onStatusUpdate()
    }

    override fun onAugmentData(sender: ScanThread, points: Array<Point>) {}

    override fun onAugmentLost(sender: ScanThread) {}

    override fun onBitmapSaved(bitmap: Bitmap) {
        ++model.scannedPages
        // TODO handle saving to file
        onStatusUpdate()
    }
}
