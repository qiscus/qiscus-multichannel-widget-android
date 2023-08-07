package com.qiscus.multichannel.util

import android.annotation.SuppressLint
import org.mockito.Mockito
import org.mockito.kotlin.spy
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@SuppressLint("CheckResult")
 fun <T> anyObject(): T {
    Mockito.any<T>()
    return uninitialized()
}
@Suppress("UNCHECKED_CAST")
fun <T> uninitialized(): T = null as T

@Suppress("UNCHECKED_CAST")
fun <T> mockObject(clazz: Class<T>): T {
    val constructor = clazz.declaredConstructors.find { it.parameterCount == 0 }
        ?: throw InstantiationException("class ${clazz.canonicalName} has no empty constructor, " +
                "is it really a Kotlin \"object\"?")

    constructor.isAccessible = true

    val mockedInstance = spy(constructor.newInstance() as T)

    return replaceObjectInstance(clazz, mockedInstance)
}

@Suppress("UNCHECKED_CAST")
private fun <T> replaceObjectInstance(clazz: Class<T>, newInstance: T): T {

    if (!clazz.declaredFields.any {
            it.name == "INSTANCE" && it.type == clazz && Modifier.isStatic(it.modifiers)
        }) {
        throw InstantiationException("clazz ${clazz.canonicalName} does not have a static  " +
                "INSTANCE field, is it really a Kotlin \"object\"?")
    }

    val instanceField = clazz.getDeclaredField("INSTANCE")
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(instanceField, instanceField.modifiers and Modifier.FINAL.inv())

    instanceField.isAccessible = true
    val originalInstance = instanceField.get(null) as T
    instanceField.set(null, newInstance)
    return originalInstance
}