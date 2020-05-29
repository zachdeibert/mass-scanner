package com.github.zachdeibert.massscanner.ui.scan

import android.graphics.Point
import android.media.Image
import android.util.Log
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.Comparator

class ScanNative : AutoCloseable {
    @Suppress("UNUSED")
    enum class Error(val code: Int, val ex: Throwable?) {
        INVALID(0, RuntimeException("Unknown error code")),
        ERR_SUCCESS(0, null),
        ERR_INVALID_PTR(-1, IllegalArgumentException("Pointer is invalid")),
        ERR_NOT_IMPLEMENTED(-2, NotImplementedError("Method is not implemented")),
        ERR_ARGUMENT_NULL(-3, IllegalArgumentException("Argument cannot be null")),
        ERR_INVALID_BUFFER(-4, IllegalArgumentException("Buffer is not direct"));

        companion object {
            fun lookup(code: Int): Error {
                val arr = values()
                val res = arr.binarySearch(INVALID, Comparator<Error> { o1, o2 ->
                    when(INVALID) {
                        o1 -> o2.code.compareTo(code)
                        o2 -> code.compareTo(o1.code)
                        else -> o2.code.compareTo(o1.code)
                    }
                }, 1)
                if (res >= 0) {
                    return arr[res]
                }
                return INVALID
            }
        }
    }

    class ScanResult internal constructor(
        val hasOutputImage: Boolean,
        val imageWidth: Int,
        val imageHeight: Int,
        val augment: Array<Point>)

    companion object {
        private const val TAG = "ScanNative"

        private const val PARAMETERS_SIZE = (19 * 4)
        private const val OUTPUT_FLAG_IMAGE_READY = 0x00000001
        private const val OUTPUT_FLAG_AUGMENT_READY = 0x00000002
        private const val OUTPUT_FLAG_SETUP_COMPLETE = 0x00000004

        private const val PARAMETER_IMAGE_WIDTH = 0
        private const val PARAMETER_IMAGE_HEIGHT = 1
        private const val PARAMETER_Y_PIXEL_STRIDE = 2
        private const val PARAMETER_Y_ROW_STRIDE = 3
        private const val PARAMETER_U_PIXEL_STRIDE = 4
        private const val PARAMETER_U_ROW_STRIDE = 5
        private const val PARAMETER_V_PIXEL_STRIDE = 6
        private const val PARAMETER_V_ROW_STRIDE = 7

        private const val PARAMETER_OUTPUT_FLAGS = 8
        private const val PARAMETER_OUTPUT_IMAGE_WIDTH = 9
        private const val PARAMETER_OUTPUT_IMAGE_HEIGHT = 10
        private const val PARAMETER_AUGMENT_POINT_1X = 11
        private const val PARAMETER_AUGMENT_POINT_1Y = 12
        private const val PARAMETER_AUGMENT_POINT_2X = 13
        private const val PARAMETER_AUGMENT_POINT_2Y = 14
        private const val PARAMETER_AUGMENT_POINT_3X = 15
        private const val PARAMETER_AUGMENT_POINT_3Y = 16
        private const val PARAMETER_AUGMENT_POINT_4X = 17
        private const val PARAMETER_AUGMENT_POINT_4Y = 18

        init {
            System.loadLibrary("mass-scanner-natives")
            Log.d(TAG, "Native code loaded.")
        }
    }

    private external fun setup(params: ByteBuffer): Long
    private external fun setup_error(ptr: Long): Int
    private external fun process_frame(ptr: Long, y: ByteBuffer, u: ByteBuffer, v: ByteBuffer, bitmap: ByteBuffer): Int
    private external fun cleanup(ptr: Long): Int

    private var ptr: Long
    private var params: IntBuffer

    fun processFrame(image: Image, bitmap: ByteBuffer): ScanResult {
        synchronized(params) {
            val planes = image.planes
            params.put(PARAMETER_IMAGE_WIDTH, image.width)
            params.put(PARAMETER_IMAGE_HEIGHT, image.height)
            params.put(PARAMETER_Y_PIXEL_STRIDE, planes[0].pixelStride)
            params.put(PARAMETER_Y_ROW_STRIDE, planes[0].rowStride)
            params.put(PARAMETER_U_PIXEL_STRIDE, planes[1].pixelStride)
            params.put(PARAMETER_U_ROW_STRIDE, planes[1].rowStride)
            params.put(PARAMETER_V_PIXEL_STRIDE, planes[2].pixelStride)
            params.put(PARAMETER_V_ROW_STRIDE, planes[2].rowStride)
            val err = Error.lookup(process_frame(ptr, planes[0].buffer, planes[1].buffer, planes[2].buffer, bitmap))
            if (err != Error.ERR_SUCCESS) {
                throw err.ex!!
            }
            val flags = params.get(PARAMETER_OUTPUT_FLAGS)
            return ScanResult(
                (flags and OUTPUT_FLAG_IMAGE_READY) != 0,
                params.get(PARAMETER_OUTPUT_IMAGE_WIDTH),
                params.get(PARAMETER_OUTPUT_IMAGE_HEIGHT),
                if ((flags and OUTPUT_FLAG_AUGMENT_READY) != 0)
                    arrayOf(
                        Point(params.get(PARAMETER_AUGMENT_POINT_1X),
                            params.get(PARAMETER_AUGMENT_POINT_1Y)),
                        Point(params.get(PARAMETER_AUGMENT_POINT_2X),
                            params.get(PARAMETER_AUGMENT_POINT_2Y)),
                        Point(params.get(PARAMETER_AUGMENT_POINT_3X),
                            params.get(PARAMETER_AUGMENT_POINT_3Y)),
                        Point(params.get(PARAMETER_AUGMENT_POINT_4X),
                            params.get(PARAMETER_AUGMENT_POINT_4Y))
                    )
                else arrayOf()
            )
        }
    }

    override fun close() {
        ptr.apply {
            ptr = 0
            if (this != 0L) {
                val err = Error.lookup(cleanup(this))
                if (err != Error.ERR_SUCCESS) {
                    throw err.ex!!.fillInStackTrace()
                }
            }
        }
    }

    protected fun finalize() = close()

    private fun closeAndThrow(ex: Throwable) {
        ex.fillInStackTrace()
        try {
            close()
        } catch (caused: Throwable) {
            var root = caused
            while (root.cause != null) {
                root = root.cause!!
            }
            root.initCause(ex)
            throw root
        }
        throw ex
    }

    init {
        Log.v(TAG, "Creating new native interface.")
        val buf = ByteBuffer.allocateDirect(PARAMETERS_SIZE)
        ptr = setup(buf)
        buf.order(ByteOrder.BIG_ENDIAN)
        params = buf.asIntBuffer()
        if (params.get(PARAMETER_OUTPUT_FLAGS) != OUTPUT_FLAG_SETUP_COMPLETE) {
            buf.order(ByteOrder.LITTLE_ENDIAN)
            params = buf.asIntBuffer()
            if (params.get(PARAMETER_OUTPUT_FLAGS) != OUTPUT_FLAG_SETUP_COMPLETE) {
                closeAndThrow(RuntimeException("Unable to determine byte order"))
            }
        }
        val err = Error.lookup(setup_error(ptr))
        if (err != Error.ERR_SUCCESS) {
            closeAndThrow(err.ex!!)
        }
    }
}
