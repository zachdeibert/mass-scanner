package com.github.zachdeibert.massscanner.ui.scan

import android.graphics.Bitmap
import android.graphics.Point
import android.media.Image
import android.util.Log
import android.util.Size
import com.github.zachdeibert.massscanner.util.AsyncImageReader
import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ScanThread : Thread("Scanning Worker"), AsyncImageReader.OnImageAvailableListener {
    companion object {
        private const val TAG = "ScanThread"
        private const val STATE_RESUMED = 1
        private const val STATE_PAUSED = 2
        private const val STATE_STOPPING = 3
        private const val STATE_STOPPED = 4
    }

    interface AnalysisListener {
        fun onAugmentData(sender: ScanThread, points: Array<Point>)
        fun onAugmentLost(sender: ScanThread)
        fun onBitmapSaved(bitmap: Bitmap)

        companion object {
            fun join(vararg listeners: AnalysisListener): AnalysisListener {
                return object : AnalysisListener {
                    override fun onAugmentData(sender: ScanThread, points: Array<Point>) {
                        listeners.forEach { it.onAugmentData(sender, points) }
                    }

                    override fun onAugmentLost(sender: ScanThread) {
                        listeners.forEach { it.onAugmentLost(sender) }
                    }

                    override fun onBitmapSaved(bitmap: Bitmap) {
                        listeners.forEach { it.onBitmapSaved(bitmap) }
                    }
                }
            }
        }
    }

    private val native = ScanNative()
    private val lock = ReentrantLock()
    private val cond = lock.newCondition()
    private var state = STATE_RESUMED

    private var _maxImageSize: Size = Size(1080, 1920)
    private var bitmapBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * maxImageSize.width * maxImageSize.height)
    var maxImageSize: Size
        get() = _maxImageSize
        set(value) {
            if (value != maxImageSize) {
                _maxImageSize = value
                bitmapBuffer = ByteBuffer.allocateDirect(4 * maxImageSize.width * maxImageSize.height)
            }
        }

    var analysisListener: AnalysisListener? = null

    protected fun fireAugmentData(points: Array<Point>) {
        analysisListener?.onAugmentData(this, points)
    }

    protected fun fireAugmentLost() {
        analysisListener?.onAugmentLost(this)
    }

    protected fun fireBitmapSaved(bitmap: Lazy<Bitmap>) {
        analysisListener?.apply {
            onBitmapSaved(bitmap.value)
        }
    }

    private var hadAugment = false
    protected fun fireAugment(points: Array<Point>) {
        when {
            points.size == 4 -> {
                hadAugment = true
                fireAugmentData(points)
            }
            hadAugment -> {
                hadAugment = false
                fireAugmentLost()
            }
        }
    }

    private fun lockedEnsureRunning() {
        when (state) {
            STATE_STOPPING -> throw IllegalStateException("Scanning thread is stopping")
            STATE_STOPPED -> throw IllegalStateException("Scanning thread is stopped")
        }
    }

    fun onPause() {
        lock.withLock {
            lockedEnsureRunning()
            state = STATE_PAUSED
            cond.signalAll()
        }
    }

    fun onResume() {
        lock.withLock {
            lockedEnsureRunning()
            state = STATE_RESUMED
            cond.signalAll()
        }
    }

    private fun lockedDoStop() {
        lockedEnsureRunning()
        state = STATE_STOPPING
        cond.signalAll()
    }

    fun onStop() {
        lock.withLock {
            lockedDoStop()
        }
    }

    private var imageReader: AsyncImageReader? = null
    override fun onImageAvailable(reader: AsyncImageReader) {
        imageReader = reader
        lock.withLock {
            cond.signalAll()
        }
    }

    override fun run() {
        apply {
            while (true) {
                also {
                    var readImage: Image? = null
                    lock.withLock {
                        when (state) {
                            STATE_RESUMED -> {
                                readImage = imageReader?.acquireLatestImage()
                                if (readImage == null) {
                                    cond.await()
                                    return@also
                                }
                            }
                            STATE_PAUSED -> {
                                fireAugment(arrayOf())
                                cond.await()
                                return@also
                            }
                            STATE_STOPPING -> return@apply
                            else -> {
                                Log.wtf(TAG, "Unknown state")
                                return@apply
                            }
                        }
                    }
                    val image = readImage
                    if (image == null) {
                        Log.wtf(TAG, "Synchronization logic error")
                    } else {
                        val bitmap = bitmapBuffer
                        val res = native.processFrame(image, bitmap)
                        image.close()
                        fireAugment(res.augment)
                        if (res.hasOutputImage) {
                            fireBitmapSaved(lazy {
                                Bitmap.createBitmap(res.imageWidth, res.imageHeight, Bitmap.Config.ARGB_8888).apply {
                                    copyPixelsFromBuffer(bitmap)
                                }
                            })
                        }
                    }
                }
            }
        }
        state = STATE_STOPPED
        close()
    }

    fun close() {
        lock.withLock {
            if (state == STATE_RESUMED || state == STATE_PAUSED) {
                lockedDoStop()
            }
        }
        if (state != STATE_STOPPED) {
            join()
        }
        native.close()
    }
}
