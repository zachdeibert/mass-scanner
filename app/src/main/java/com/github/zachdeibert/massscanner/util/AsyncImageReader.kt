package com.github.zachdeibert.massscanner.util

import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.view.Surface
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class AsyncImageReader(width: Int, height: Int, format: Int, maxImages: Int) : AutoCloseable {
    companion object {
        fun newInstance(width: Int, height: Int, format: Int, maxImages: Int): AsyncImageReader {
            return AsyncImageReader(width, height, format, maxImages)
        }
    }

    interface OnImageAvailableListener {
        fun onImageAvailable(reader: AsyncImageReader)
    }

    private val lock = ReentrantLock()
    private var bufferedImages = 0

    private var childListener: OnImageAvailableListener? = null
    private val listener = ImageReader.OnImageAvailableListener {
        apply {
            lock.withLock {
                if (++bufferedImages < maxImages - 1) {
                    return@apply
                }
            }
            lockedAcquireNextImage()!!.close()
        }
        childListener?.onImageAvailable(this)
    }
    private val reader = ImageReader.newInstance(width, height, format, maxImages).also { it.setOnImageAvailableListener(listener, null) }

    val width
        get() = reader.width

    val height
        get() = reader.height

    val imageFormat
        get() = reader.imageFormat

    val maxImages
        get() = reader.maxImages

    val surface: Surface?
        get() = reader.surface

    fun acquireLatestImage(): Image? {
        lock.withLock {
            val image = reader.acquireLatestImage()
            bufferedImages = 0
            return image
        }
    }

    private fun lockedAcquireNextImage(): Image? {
        val image = reader.acquireNextImage()
        if (image != null) {
            --bufferedImages
        }
        return image
    }

    fun acquireNextImage(): Image? {
        lock.withLock {
            return lockedAcquireNextImage()
        }
    }

    fun setOnImageAvailableListener(listener: OnImageAvailableListener?, handler: Handler?) {
        childListener = listener
        reader.setOnImageAvailableListener(this.listener, handler)
    }

    override fun close() {
        reader.close()
    }
}
