package com.qiscus.multichannel.basetest

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.qiscus.multichannel.ui.test.BlankForTestActivity
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.Extension
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class InstrumentationBaseTest : Extension {

    var scenario: ActivityScenario<BlankForTestActivity>? = null
    var context: Context? = null
    var application: Application? = null
    var activity: Activity? = null

    open fun setUpComponent() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        application = context?.applicationContext as Application
    }

    fun tearDownComponent() {
        if (scenario != null) scenario!!.close()
        application = null
        context = null
    }

    fun setActivity() {
        scenario = ActivityScenario.launch(BlankForTestActivity::class.java)
            .onActivity { activity -> this.activity = activity }
    }

    fun runOnMainThread(runnable: Runnable?) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> extractConstructor(o: Class<T>, vararg parameterTypes: Class<*>): Constructor<T> {
        val constructor = o.getDeclaredConstructor(*parameterTypes)
        constructor.isAccessible = true
        return constructor as Constructor<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> extractField(o: Any, name: String): T? {
        var value: T? = null

        try {
            value = extractFieldOnly(o, name)!![o] as T
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return value
    }

    fun extractFieldOnly(o: Any, name: String): Field? {
        try {
            val f = o.javaClass.getDeclaredField(name)
            f.isAccessible = true
            return f
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun extractMethode(o: Any, names: String, size: Int? = null): KFunction<*> {
        var result: KFunction<*>? = null
        val list = o::class.declaredMemberFunctions.filter {
            it.name == names
        }

        for (i in list.indices) {
            result = list[i]
            if (size != null && size == result.parameters.size - 1) break
        }

        return result!!.apply {
            isAccessible = true
        }
    }

}
