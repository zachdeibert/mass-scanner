package com.github.zachdeibert.massscanner.ui.scan

import org.junit.Assert
import org.junit.Test

class ScanNativeTest {
    @Test
    fun errorBinarySearch() {
        ScanNative.Error.values().forEach {
            if (it != ScanNative.Error.INVALID) {
                Assert.assertEquals(it, ScanNative.Error.lookup(it.code))
            }
        }
    }
}
