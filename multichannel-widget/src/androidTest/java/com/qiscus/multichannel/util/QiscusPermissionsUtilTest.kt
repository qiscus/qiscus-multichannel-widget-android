package com.qiscus.multichannel.util

import com.qiscus.multichannel.R
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.jupiter.api.AfterAll

import org.junit.jupiter.api.AfterEach
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
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun hasPermissionsTest() {
       val granted =  QiscusPermissionsUtil.hasPermissions(activity!!, MultichannelConst.FILE_PERMISSION)
        assertFalse(granted)
    }

    @Test
    fun requestPermissionsTest() {
        try {
            QiscusPermissionsUtil.requestPermissions(
                activity!!, activity!!.getString(R.string.qiscus_permission_request_title_mc),
                MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val granted =  QiscusPermissionsUtil.hasPermissions(activity!!, MultichannelConst.FILE_PERMISSION)
        assertFalse(granted)
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
    }

    @Test
    fun checkDeniedPermissionsNeverAskAgainTest() {
    }
}