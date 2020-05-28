package com.github.zachdeibert.massscanner.util

import com.github.zachdeibert.massscanner.AssertHelper
import org.junit.Assert
import org.junit.Test

class Race2Test {
    @Test
    fun raceTest() {
        val race = Race2<Int, Int, Int>()
        val produced = mutableListOf<Int>()
        val closed = mutableListOf<Int>()
        race.apply {
            producerCallback = object : Race2.ProducerCallback<Int, Int, Int>() {
                override fun produce(a: Int, b: Int, finish: (Int?) -> Unit) {
                    finish(a * 10 + b)
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
            a = 3
            Assert.assertEquals(3, a)
            Assert.assertEquals(2, b)
            Assert.assertTrue(hasResult)
            AssertHelper.assertListEquals(produced, 12, 32)
            AssertHelper.assertListEquals(closed, 12)
            close()
            AssertHelper.assertThrows { a }
            AssertHelper.assertThrows { b }
            Assert.assertFalse(hasResult)
            AssertHelper.assertListEquals(produced, 12, 32)
            AssertHelper.assertListEquals(closed, 12, 32)
            b = 1
            a = 2
            AssertHelper.assertListEquals(produced, 12, 32, 21)
        }
    }
}
