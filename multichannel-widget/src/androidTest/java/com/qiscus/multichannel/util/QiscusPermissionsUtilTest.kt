package com.qiscus.multichannel.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.qiscus.multichannel.R
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.Ignore
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class QiscusPermissionsUtilTest : InstrumentationBaseTest() {

    @BeforeAll
    fun setUp() {
        setUpComponent()
    }

    @BeforeEach
    fun before() {
        setActivity()
        BuildVersionProviderUtil.get().changeVersionTest(Build.VERSION_CODES.M)
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    private fun grantPhonePermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perm = MultichannelConst.FILE_PERMISSION[0]

            getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant ${getApplicationContext<Context>().packageName} $perm"
            ).also {
                action.invoke()
            }
        }
    }

    private fun revokePermission(action: () -> Unit) {
        getInstrumentation().uiAutomation.executeShellCommand(
            "pm reset-permissions"
        ).also {
            action.invoke()
        }
    }

    @Test
    fun hasPermissionsTest() {
        revokePermission {
            val granted =
                QiscusPermissionsUtil.hasPermissions(activity!!, MultichannelConst.FILE_PERMISSION)
            assertFalse(granted)
        }
    }

    @Test
    fun hasPermissionsTestVersionUnderM() {
        BuildVersionProviderUtil.get().changeVersionTest(Build.VERSION_CODES.LOLLIPOP)

        val granted =
            QiscusPermissionsUtil.hasPermissions(activity!!, MultichannelConst.FILE_PERMISSION)
        assertTrue(granted)
    }

   /* @Test
    fun hasPermissionsTestGranted() {
        grantPhonePermission {
            val granted =
                QiscusPermissionsUtil.hasPermissions(
                    activity!!, arrayOf(MultichannelConst.FILE_PERMISSION[0])
                )
            assertTrue(granted)
        }
    }*/

    @Test
    fun requestPermissionsTest() {
        revokePermission {
            try {
                QiscusPermissionsUtil.requestPermissions(
                    activity!!, activity!!.getString(R.string.qiscus_permission_request_title_mc),
                    MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val granted =
                QiscusPermissionsUtil.hasPermissions(activity!!, MultichannelConst.FILE_PERMISSION)
            assertFalse(granted)
        }
    }

    @Test
    fun testRequestPermissionsTest() {
        try {
            QiscusPermissionsUtil.requestPermissions(
                activity!!, activity!!.getString(R.string.qiscus_permission_request_title_mc),
                android.R.string.ok,
                android.R.string.cancel,
                MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun onRequestPermissionsResultTest() {
        val list = intArrayOf(
            PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED
        )

        QiscusPermissionsUtil.onRequestPermissionsResult(
            MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28,
            list, activity!!
        )
    }

    @Test
    fun onRequestPermissionsResultTestDenied() {
        val list = intArrayOf(
            PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_DENIED
        )

        QiscusPermissionsUtil.onRequestPermissionsResult(
            MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28,
            list, activity!!
        )
    }
    @Test
    fun onRequestPermissionsResultTestEmpty() {
        val list = intArrayOf(12000, 100, 1200)

        QiscusPermissionsUtil.onRequestPermissionsResult(
            MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28,
            list, activity!!
        )
    }

//    @Test
//    fun checkDeniedPermissionsNeverAskAgainTest() {
//        QiscusPermissionsUtil.checkDeniedPermissionsNeverAskAgain(
//            activity!!,"perm",
//            R.string.qiscus_grant_mc, R.string.qiscus_denny_mc,
//            MultichannelConst.CAMERA_PERMISSION_28.toList()
//        )
//    }
}