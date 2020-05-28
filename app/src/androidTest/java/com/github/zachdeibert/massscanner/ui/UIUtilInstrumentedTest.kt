package com.github.zachdeibert.massscanner.ui

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UIUtilInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun setupContext() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    private fun case(bytes: Long, res: Float, unit: String) {
        val fmt = UIUtil.formatFileSize(bytes, context)
        Assert.assertEquals(res, fmt.a, 0.001f)
        Assert.assertEquals(unit, fmt.b)
    }

    @Test
    fun testFormatFileSize() {
        case(0, 0f, "B")
        case(500, 500f, "B")
        case(500000, 500f, "KB")
        case(500000000, 500f, "MB")
        case(500000000000, 500f, "GB")
        case(500000000000000, 500f, "TB")
        case(10000000000000000, 10000f, "TB")
    }
}
