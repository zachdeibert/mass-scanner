package com.github.zachdeibert.massscanner.util

import com.github.zachdeibert.massscanner.AssertHelper
import org.junit.Assert
import org.junit.Test

class Race3Test {
    @Test
    fun raceTest() {
        val race = Race3<Int, Int, Int, Int>()
        val produced = mutableListOf<Int>()
        val closed = mutableListOf<Int>()
        race.apply {
            producerCallback = object : Race3.ProducerCallback<Int, Int, Int, Int>() {
                override fun produce(a: Int, b: Int, c: Int, finish: (Int?) -> Unit) {
                    finish(a * 100 + b * 10 + c)
                }

                override fun close(o: Int, finish: () -> Unit) {
                    closed.add(o)
                    finish()
                }
            }
            consumerCallback = object : RaceBase.ConsumerCallback<Int>() {
                override fun consume(out: Int) {
                    produced.add(out)
                }
            }
            a = 1
            b = 2
            c = 3
            a = 4
            Assert.assertEquals(4, a)
            Assert.assertEquals(2, b)
            Assert.assertEquals(3, c)
            Assert.assertTrue(hasResult)
            AssertHelper.assertListEquals(produced, 123, 423)
            AssertHelper.assertListEquals(closed, 123)
            close()
            AssertHelper.assertThrows { a }
            AssertHelper.assertThrows { b }
            AssertHelper.assertThrows { c }
            Assert.assertFalse(hasResult)
            AssertHelper.assertListEquals(produced, 123, 423)
            AssertHelper.assertListEquals(closed, 123, 423)
            c = 1
            b = 2
            a = 3
            AssertHelper.assertListEquals(produced, 123, 423, 321)
        }
    }
}
