package com.github.zachdeibert.massscanner.util

import com.github.zachdeibert.massscanner.AssertHelper
import org.junit.After
import org.junit.Assert
import org.junit.Test

class Race4Test {
    private fun concurrentTest(vararg errors: Int, block: (Race4<Int, Int, Int, Int, Int>, MutableList<Int>, MutableList<Int>, () -> Unit, () -> Unit) -> Unit) {
        test(*errors) { race, produced, closed ->
            val superProducer = race.producerCallback!!
            val threads = arrayOf<Thread?>(null, null)
            race.producerCallback = object : Race4.ProducerCallback<Int, Int, Int, Int, Int>() {
                override fun produce(a: Int, b: Int, c: Int, d: Int, finish: (Int?) -> Unit) {
                    threads[0] = Thread.currentThread()
                    try {
                        Thread.sleep(Long.MAX_VALUE)
                    } catch (_: InterruptedException) {}
                    threads[0] = null
                    superProducer.produce(a, b, c, d, finish)
                }

                override fun close(o: Int, finish: () -> Unit) {
                    threads[1] = Thread.currentThread()
                    try {
                        Thread.sleep(Long.MAX_VALUE)
                    } catch (_: InterruptedException) {}
                    threads[1] = null
                    superProducer.close(o, finish)
                }
            }
            block(race, produced, closed, {
                threads[0]!!.interrupt()
            }, {
                threads[1]!!.interrupt()
            })
        }
    }

    private fun test(vararg errors: Int, block: (Race4<Int, Int, Int, Int, Int>, MutableList<Int>, MutableList<Int>) -> Unit) {
        val race = Race4<Int, Int, Int, Int, Int>()
        val produced = mutableListOf<Int>()
        val closed = mutableListOf<Int>()
        race.producerCallback = object : Race4.ProducerCallback<Int, Int, Int, Int, Int>() {
            override fun produce(a: Int, b: Int, c: Int, d: Int, finish: (Int?) -> Unit) {
                val o = a * 1000 + b * 100 + c * 10 + d
                if (errors.contains(o)) {
                    finish(null)
                } else {
                    finish(o)
                }
            }

            override fun close(o: Int, finish: () -> Unit) {
                closed.add(o)
                finish()
            }
        }
        race.consumerCallback = object : RaceBase.ConsumerCallback<Int>() {
            override fun consume(out: Int) {
                produced.add(out)
            }
        }
        block(race, produced, closed)
    }

    private val threads: MutableList<Thread> = mutableListOf()

    private fun thread(block: () -> Unit) {
        Thread(block).apply {
            start()
            threads.add(this)
        }
        Thread.sleep(10)
    }

    @After
    fun cleanupThreads() {
        threads.forEachIndexed { i, thread ->
            thread.join(100)
            if (thread.isAlive) {
                Assert.fail("Thread $i failed to exit")
            }
        }
    }

    @Test
    fun waitForAllInputs() {
        test { race, outs, _ ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
            }
            AssertHelper.assertListEquals(outs, 1234)
        }
        test { race, outs, _ ->
            race.apply {
                d = 1
                c = 2
                b = 3
                a = 4
            }
            AssertHelper.assertListEquals(outs, 4321)
        }
    }

    @Test
    fun producesMultiple() {
        test { race, outs, _ ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
                a = 5
                b = 6
                c = 7
                d = 8
            }
            AssertHelper.assertListEquals(outs, 1234, 5234, 5634, 5674, 5678)
        }
    }

    @Test
    fun errorsNotPassedToConsumer() {
        test(5234, 5678) { race, outs, _ ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
                a = 5
                b = 6
                c = 7
                d = 8
            }
            AssertHelper.assertListEquals(outs, 1234, 5634, 5674)
        }
    }

    @Test
    fun outputsAreClosed() {
        test { race, outs, closed ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
                a = 5
                b = 6
                c = 7
                d = 8
            }
            AssertHelper.assertListEquals(outs, 1234, 5234, 5634, 5674, 5678)
            AssertHelper.assertListEquals(closed, 1234, 5234, 5634, 5674)
        }
    }

    @Test
    fun outputPropertyCorrect() {
        test { race, _, _ ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
            }
            Assert.assertTrue(race.hasResult)
            Assert.assertEquals(1234, race.out)
        }
    }

    @Test
    fun close() {
        test { race, _, closed ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
                close()
            }
            Assert.assertFalse(race.hasResult)
            AssertHelper.assertListEquals(closed, 1234)
            AssertHelper.assertThrows { race.a }
        }
    }

    @Test
    fun closeWithoutResult() {
        test { race, _, closed ->
            race.apply {
                a = 1
                b = 2
                c = 3
                close()
            }
            Assert.assertFalse(race.hasResult)
            AssertHelper.assertListEquals(closed)
        }
    }

    @Test
    fun noProducer() {
        test { race, _, _ ->
            race.apply {
                producerCallback = null
                a = 1
                b = 2
                c = 3
                d = 4
                a = 5
                b = 6
            }
            Assert.assertFalse(race.hasResult)
            race.close()
            Assert.assertFalse(race.hasResult)
        }
    }

    @Test
    fun outInvalid() {
        test { race, _, _ ->
            AssertHelper.assertThrows { race.out }
        }
    }

    @Test
    fun closeWithProducerRemoved() {
        test { race, _, closed ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
                producerCallback = null
                close()
            }
            Assert.assertFalse(race.hasResult)
            AssertHelper.assertListEquals(closed)
        }
    }

    @Test
    fun setWhileClosing() {
        concurrentTest { race, outs, closed, doProduce, doClose ->
            race.apply {
                a = 1
                b = 2
                c = 3
                thread { d = 4 }
                doProduce()
                Thread.sleep(10)
                thread { close() }
                thread { a = 5 }
            }
            AssertHelper.assertListEquals(outs, 1234)
            AssertHelper.assertListEquals(closed)
            doClose()
            Thread.sleep(10)
            Assert.assertFalse(race.hasResult)
            AssertHelper.assertListEquals(outs, 1234)
            AssertHelper.assertListEquals(closed, 1234)
            AssertHelper.assertThrows { race.a }
        }
    }

    @Test
    fun setWhileProducing() {
        concurrentTest { race, outs, closed, doProduce, doClose ->
            race.apply {
                a = 1
                b = 2
                c = 3
                thread { d = 4 }
                thread { a = 5 }
                thread { b = 6 }
                doProduce()
                Thread.sleep(10)
                doClose()
                Thread.sleep(10)
                doProduce()
                Thread.sleep(10)
                thread { c = 7 }
                doClose()
                Thread.sleep(10)
                doProduce()
                Thread.sleep(10)
            }
            Assert.assertTrue(race.hasResult)
            Assert.assertEquals(5674, race.out)
            AssertHelper.assertListEquals(outs, 5634, 5674)
            AssertHelper.assertListEquals(closed, 1234, 5634)
        }
    }

    @Test
    fun producerLostBeforeClose() {
        test { race, outs, closed ->
            race.apply {
                a = 1
                b = 2
                c = 3
                d = 4
                producerCallback = null
                a = 5
            }
            Assert.assertFalse(race.hasResult)
            race.a
            race.b
            AssertHelper.assertListEquals(outs, 1234)
            AssertHelper.assertListEquals(closed)
        }
    }

    @Test
    fun closeWhileProducing() {
        concurrentTest { race, outs, closed, doProduce, doClose ->
            race.apply {
                a = 1
                b = 2
                c = 3
                thread { d = 4 }
                thread { close() }
                thread { a = 5 }
            }
            AssertHelper.assertListEquals(outs)
            doProduce()
            Thread.sleep(10)
            AssertHelper.assertListEquals(outs)
            AssertHelper.assertListEquals(closed)
            doClose()
            Thread.sleep(10)
            Assert.assertFalse(race.hasResult)
            AssertHelper.assertListEquals(outs)
            AssertHelper.assertListEquals(closed, 1234)
            AssertHelper.assertThrows { race.a }
        }
    }
    @Test
    fun raceTest() {
        val race = Race4<Int, Int, Int, Int, Int>()
        val produced = mutableListOf<Int>()
        val closed = mutableListOf<Int>()
        race.apply {
            producerCallback = object : Race4.ProducerCallback<Int, Int, Int, Int, Int>() {
                override fun produce(a: Int, b: Int, c: Int, d: Int, finish: (Int?) -> Unit) {
                    finish(a * 1000 + b * 100 + c * 10 + d)
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
            d = 4
            a = 5
            Assert.assertEquals(5, a)
            Assert.assertEquals(2, b)
            Assert.assertEquals(3, c)
            Assert.assertEquals(4, d)
            Assert.assertTrue(hasResult)
            AssertHelper.assertListEquals(produced, 1234, 5234)
            AssertHelper.assertListEquals(closed, 1234)
            close()
            AssertHelper.assertThrows { a }
            AssertHelper.assertThrows { b }
            AssertHelper.assertThrows { c }
            AssertHelper.assertThrows { d }
            Assert.assertFalse(hasResult)
            AssertHelper.assertListEquals(produced, 1234, 5234)
            AssertHelper.assertListEquals(closed, 1234, 5234)
            d = 1
            c = 2
            b = 3
            a = 4
            AssertHelper.assertListEquals(produced, 1234, 5234, 4321)
        }
    }
}
