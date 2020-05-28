package com.github.zachdeibert.massscanner.ui

import org.junit.Assert
import org.junit.Test

class UIUtilTest {
    private fun case(bytes: Long, res: Float, unit: Int) {
        val fmt = UIUtil.formatFileSize(bytes)
        Assert.assertEquals(res, fmt.a, 0.001f)
        Assert.assertEquals(unit, fmt.b)
    }

    @Test
    fun testFormatFileSize() {
        case(0, 0f, 0)
        case(500, 500f, 0)
        case(1000, 1f, 1)
        case(1234, 1.234f, 1)
        case(500000, 500f, 1)
        case(1000000, 1f, 2)
        case(500000000, 500f, 2)
        case(1000000000, 1f, 3)
        case(500000000000, 500f, 3)
        case(1000000000000, 1f, 4)
        case(500000000000000, 500f, 4)
        case(10000000000000000, 10000f, 4)
    }
}
