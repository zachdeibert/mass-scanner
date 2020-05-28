package com.github.zachdeibert.massscanner.util

class Race4<T1, T2, T3, T4, Tout> : RaceBase<Tout, Race4.ProducerCallback<T1, T2, T3, T4, Tout>>() {
    abstract class ProducerCallback<T1, T2, T3, T4, Tout> {
        abstract fun produce(a: T1, b: T2, c: T3, d: T4, finish: (Tout?) -> Unit)
        abstract fun close(o: Tout, finish: () -> Unit)
    }

    private var _a: T1? = null
    private var _b: T2? = null
    private var _c: T3? = null
    private var _d: T4? = null
    var a: T1
        get() = _a!!
        set(value) = setter { _a = value }
    var b: T2
        get() = _b!!
        set(value) = setter { _b = value }
    var c: T3
        get() = _c!!
        set(value) = setter { _c = value }
    var d: T4
        get() = _d!!
        set(value) = setter { _d = value }

    override val hasAllInputs get() = _a != null && _b != null && _c != null && _d != null

    override fun produce(callback: ProducerCallback<T1, T2, T3, T4, Tout>, finish: (Tout?) -> Unit)
            = callback.produce(a, b, c, d, finish)

    override fun cleanup(callback: ProducerCallback<T1, T2, T3, T4, Tout>, obj: Tout, finish: () -> Unit)
            = callback.close(obj, finish)

    override fun clearInputs() {
        _a = null
        _b = null
        _c = null
        _d = null
    }
}
