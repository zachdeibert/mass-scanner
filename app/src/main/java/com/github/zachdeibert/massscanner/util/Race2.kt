package com.github.zachdeibert.massscanner.util

class Race2<T1, T2, Tout> : RaceBase<Tout, Race2.ProducerCallback<T1, T2, Tout>>() {
    abstract class ProducerCallback<T1, T2, Tout> {
        abstract fun produce(a: T1, b: T2, finish: (Tout?) -> Unit)
        abstract fun close(o: Tout, finish: () -> Unit)
    }

    private var _a: T1? = null
    private var _b: T2? = null
    var a: T1
        get() = _a!!
        set(value) = setter { _a = value }
    var b: T2
        get() = _b!!
        set(value) = setter { _b = value }

    override val hasAllInputs get() = _a != null && _b != null

    override fun produce(callback: ProducerCallback<T1, T2, Tout>, finish: (Tout?) -> Unit)
        = callback.produce(a, b, finish)

    override fun cleanup(callback: ProducerCallback<T1, T2, Tout>, obj: Tout, finish: () -> Unit)
        = callback.close(obj, finish)

    override fun clearInputs() {
        _a = null
        _b = null
    }
}
