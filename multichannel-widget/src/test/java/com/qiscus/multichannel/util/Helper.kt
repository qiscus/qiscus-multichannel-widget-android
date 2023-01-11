package com.qiscus.multichannel.util

//inline fun <reified T : Any> argumentCaptor() = ArgumentCaptor.forClass(T::class.java)!!
//inline fun <reified T : Any> capture(captor: ArgumentCaptor<T>): T = captor.capture() ?: createInstance<T>()
/*
inline fun <reified T : Any> capture(noinline consumer: (T) -> Unit): T {
    var times = 0
    return argThat { if (++times == 1) consumer.invoke(this); true }
}
 */


/*
fun <T : Any> argumentCaptor(): KArgumentCaptor<T> {
    return KArgumentCaptor(ArgumentCaptor.forClass(T::class.java), T::class)
}

/**
 * Creates 2 [KArgumentCaptor]s for given types.
 */
inline fun <reified A : Any, reified B : Any> argumentCaptor(
    a: KClass<A> = A::class,
    b: KClass<B> = B::class
): Pair<KArgumentCaptor<A>, KArgumentCaptor<B>> {
    return Pair(
        KArgumentCaptor(ArgumentCaptor.forClass(a.java), a),
        KArgumentCaptor(ArgumentCaptor.forClass(b.java), b)
    )
}

/**
 * Creates 3 [KArgumentCaptor]s for given types.
 */
inline fun <reified A : Any, reified B : Any, reified C : Any> argumentCaptor(
    a: KClass<A> = A::class,
    b: KClass<B> = B::class,
    c: KClass<C> = C::class
): Triple<KArgumentCaptor<A>, KArgumentCaptor<B>, KArgumentCaptor<C>> {
    return Triple(
        KArgumentCaptor(ArgumentCaptor.forClass(a.java), a),
        KArgumentCaptor(ArgumentCaptor.forClass(b.java), b),
        KArgumentCaptor(ArgumentCaptor.forClass(c.java), c)
    )
}

class ArgumentCaptorHolder4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {

    operator fun component1() = first
    operator fun component2() = second
    operator fun component3() = third
    operator fun component4() = fourth
}

class ArgumentCaptorHolder5<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
) {

    operator fun component1() = first
    operator fun component2() = second
    operator fun component3() = third
    operator fun component4() = fourth
    operator fun component5() = fifth
}


/**
 * Creates 4 [KArgumentCaptor]s for given types.
 */
inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any> argumentCaptor(
    a: KClass<A> = A::class,
    b: KClass<B> = B::class,
    c: KClass<C> = C::class,
    d: KClass<D> = D::class
): ArgumentCaptorHolder4<KArgumentCaptor<A>, KArgumentCaptor<B>, KArgumentCaptor<C>, KArgumentCaptor<D>> {
    return ArgumentCaptorHolder4(
        KArgumentCaptor(ArgumentCaptor.forClass(a.java), a),
        KArgumentCaptor(ArgumentCaptor.forClass(b.java), b),
        KArgumentCaptor(ArgumentCaptor.forClass(c.java), c),
        KArgumentCaptor(ArgumentCaptor.forClass(d.java), d)
    )
}

/**
 * Creates 4 [KArgumentCaptor]s for given types.
 */
inline fun <reified A : Any, reified B : Any, reified C : Any, reified D : Any, reified E : Any> argumentCaptor(
    a: KClass<A> = A::class,
    b: KClass<B> = B::class,
    c: KClass<C> = C::class,
    d: KClass<D> = D::class,
    e: KClass<E> = E::class
): ArgumentCaptorHolder5<KArgumentCaptor<A>, KArgumentCaptor<B>, KArgumentCaptor<C>, KArgumentCaptor<D>, KArgumentCaptor<E>> {
    return ArgumentCaptorHolder5(
        KArgumentCaptor(ArgumentCaptor.forClass(a.java), a),
        KArgumentCaptor(ArgumentCaptor.forClass(b.java), b),
        KArgumentCaptor(ArgumentCaptor.forClass(c.java), c),
        KArgumentCaptor(ArgumentCaptor.forClass(d.java), d),
        KArgumentCaptor(ArgumentCaptor.forClass(e.java), e)
    )
}

/**
 * Creates a [KArgumentCaptor] for given type, taking in a lambda to allow fast verification.
 */
inline fun <reified T : Any> argumentCaptor(f: KArgumentCaptor<T>.() -> Unit): KArgumentCaptor<T> {
    return argumentCaptor<T>().apply(f)
}

/**
 * Creates a [KArgumentCaptor] for given nullable type.
 */
inline fun <reified T : Any> nullableArgumentCaptor(): KArgumentCaptor<T?> {
    return KArgumentCaptor(ArgumentCaptor.forClass(T::class.java), T::class)
}

/**
 * Creates a [KArgumentCaptor] for given nullable type, taking in a lambda to allow fast verification.
 */
inline fun <reified T : Any> nullableArgumentCaptor(f: KArgumentCaptor<T?>.() -> Unit): KArgumentCaptor<T?> {
    return nullableArgumentCaptor<T>().apply(f)
}

/**
 * Alias for [ArgumentCaptor.capture].
 */
inline fun <reified T : Any> capture(captor: ArgumentCaptor<T>): T {
    return captor.capture() ?: createInstance()
}

class KArgumentCaptor<out T : Any?>(
    private val captor: ArgumentCaptor<T>,
    private val tClass: KClass<*>
) {

    /**
     * The first captured value of the argument.
     * @throws IndexOutOfBoundsException if the value is not available.
     */
    val firstValue: T
        get() = captor.firstValue

    /**
     * The second captured value of the argument.
     * @throws IndexOutOfBoundsException if the value is not available.
     */
    val secondValue: T
        get() = captor.secondValue

    /**
     * The third captured value of the argument.
     * @throws IndexOutOfBoundsException if the value is not available.
     */
    val thirdValue: T
        get() = captor.thirdValue

    /**
     * The last captured value of the argument.
     * @throws IndexOutOfBoundsException if the value is not available.
     */
    val lastValue: T
        get() = captor.lastValue

    val allValues: List<T>
        get() = captor.allValues

    @Suppress("UNCHECKED_CAST")
    fun capture(): T {
        return captor.capture() ?: createInstance(tClass) as T
    }
}

val <T> ArgumentCaptor<T>.firstValue: T
    get() = allValues[0]

val <T> ArgumentCaptor<T>.secondValue: T
    get() = allValues[1]

val <T> ArgumentCaptor<T>.thirdValue: T
    get() = allValues[2]

val <T> ArgumentCaptor<T>.lastValue: T
    get() = allValues.last()
*/