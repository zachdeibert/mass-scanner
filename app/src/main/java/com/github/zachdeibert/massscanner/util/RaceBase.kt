package com.github.zachdeibert.massscanner.util

abstract class RaceBase<Tout, TProducer> {
    companion object {
        private const val STATE_IDLE = 0
        private const val STATE_PRODUCING = 1
        private const val STATE_NEEDS_PRODUCING = 2
    }

    abstract class ConsumerCallback<Tout> {
        abstract fun consume(out: Tout)
    }

    private val lock = object {}
    private var _out: Tout? = null
    private var state = STATE_IDLE
    private var closeThread: Thread? = null

    val hasResult: Boolean get() = _out != null
    val out: Tout get() = _out!!
    var producerCallback: TProducer? = null
    var consumerCallback: ConsumerCallback<Tout>? = null

    protected abstract val hasAllInputs: Boolean

    protected abstract fun produce(callback: TProducer, finish: (Tout?) -> Unit)
    protected abstract fun cleanup(callback: TProducer, obj: Tout, finish: () -> Unit)
    protected abstract fun clearInputs()

    private fun doProduce() {
        val producerCallback = this.producerCallback
        if (producerCallback != null) {
            produce(producerCallback) {
                var doProduce = false
                synchronized(lock) {
                    _out = it
                    state = state and STATE_PRODUCING.inv()
                    if ((state and STATE_NEEDS_PRODUCING) != 0) {
                        state = STATE_PRODUCING
                        doProduce = true
                    }
                }
                val closeThread = this.closeThread
                if (closeThread != null) {
                    state = STATE_IDLE
                    closeThread.interrupt()
                    return@produce
                }
                if (doProduce) {
                    closeAndProduce()
                } else if (it != null) {
                    consumerCallback?.consume(it)
                }
            }
        } else {
            state = STATE_IDLE
        }
    }

    private fun closeAndProduce() {
        if (hasResult) {
            val producerCallback = this.producerCallback
            if (producerCallback != null) {
                out.apply {
                    _out = null
                    cleanup(producerCallback, this) {
                        doProduce()
                    }
                }
            } else {
                _out = null
            }
        } else {
            doProduce()
        }
    }

    protected fun setter(block: () -> Unit) {
        if (closeThread == null) {
            var produce = false
            synchronized(lock) {
                block()
                if (hasAllInputs) {
                    state = state or STATE_NEEDS_PRODUCING
                    if (state == STATE_NEEDS_PRODUCING) {
                        state = STATE_PRODUCING
                        produce = true
                    }
                }
            }
            if (produce) {
                closeAndProduce()
            }
        }
    }

    fun close() {
        closeThread = Thread.currentThread()
        synchronized(lock) {}
        while (state != STATE_IDLE) {
            try {
                Thread.sleep(Long.MAX_VALUE)
            } catch (_: InterruptedException) {}
        }
        clearInputs()
        if (hasResult) {
            val producerCallback = this.producerCallback
            if (producerCallback != null) {
                out.apply {
                    _out = null
                    cleanup(producerCallback, this) {
                        closeThread = null
                    }
                }
            } else {
                _out = null
                closeThread = null
            }
        } else {
            closeThread = null
        }
    }
}
