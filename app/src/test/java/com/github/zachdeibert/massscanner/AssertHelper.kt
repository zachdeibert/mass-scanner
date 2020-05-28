package com.github.zachdeibert.massscanner

import org.junit.Assert
import java.lang.Exception

object AssertHelper {
    fun assertThrows(block: () -> Unit) {
        try {
            block()
            Assert.fail()
        } catch (_: Exception) {}
    }

    inline fun <reified T> assertListEquals(actual: List<T>, vararg expected: T) {
        Assert.assertArrayEquals(expected, actual.toTypedArray())
    }
}
