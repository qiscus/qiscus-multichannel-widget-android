package com.qiscus.multichannel.basetest

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.qiscus.multichannel.ui.test.BlankForTestActivity
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.Extension
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class InstrumentationBaseTest : Extension {

    var scenario: ActivityScenario<BlankForTestActivity>? = null
    var context: Context? = null
    var application: Application? = null
    var activity: Activity? = null

    fun setUpComponent() {
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
    fun <T> extractField(o: Any, name: String): T? {
        var value: T? = null

        try {
            val f = o.javaClass.getDeclaredField(name)
            f.isAccessible = true
            value = f[o] as T
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return value
    }

    @Suppress("UNCHECKED_CAST")
    fun extractMethode(o: Any, names: String, size: Int? = null): KFunction<*> {
        var result : KFunction<*>? = null
        val list = o::class.declaredMemberFunctions.filter {
            it.name == names
        }

        for (i in list.indices) {
            result = list[i]
            if (size!= null && size == result.parameters.size - 1) break
        }

        return result!!.apply {
            isAccessible = true
        }
    }
}
