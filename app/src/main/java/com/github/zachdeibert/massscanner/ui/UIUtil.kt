package com.github.zachdeibert.massscanner.ui

import android.content.Context
import com.github.zachdeibert.massscanner.R
import com.github.zachdeibert.massscanner.util.Tuple

object UIUtil {
    fun formatFileSize(bytes: Long): Tuple<Float, Int> {
        val unit: Int
        val unitDiv: Float
        when {
            bytes >= 1000000000000 -> {
                unit = 4
                unitDiv = 1000000000000f
            }
            bytes >= 1000000000 -> {
                unit = 3
                unitDiv = 1000000000f
            }
            bytes >= 1000000 -> {
                unit = 2
                unitDiv = 1000000f
            }
            bytes >= 1000 -> {
                unit = 1
                unitDiv = 1000f
            }
            else -> {
                unit = 0
                unitDiv = 1f
            }
        }
        return Tuple(bytes.toFloat() / unitDiv, unit)
    }

    fun formatFileSize(bytes: Long, context: Context): Tuple<Float, String> {
        val num = formatFileSize(bytes)
        return Tuple(num.a, context.resources.getStringArray(R.array.filesize_units)[num.b])
    }
}