package com.qiscus.multichannel

import android.content.Context
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig.*
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.util.MultichannelNotificationListener
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class QiscusMultichannelWidgetConfigTest : InstrumentationBaseTest() {

    private var config: QiscusMultichannelWidgetConfig? = null

    @BeforeAll
    fun setUp() {
        setUpComponent()
        config = QiscusMultichannelWidgetConfig()
        config?.prepare(context!!)
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
        config = null
    }

    @Test
    fun getPref() {
        val pref1 = config?.getPref()
        assertNotNull(pref1)
    }

    @Test
    fun `setRoomSubtitle$Multichannel_sample_multichannel_widget_main`() {
        config?.let {
            it.setRoomSubtitle(
                RoomSubtitle.DISABLE, "subTitle"
            )
            it.setRoomSubtitle(
                RoomSubtitle.ENABLE, "subTitle"
            )
            it.setRoomSubtitle(
                RoomSubtitle.ENABLE, "subTitle"
            )
        }
    }

    @Test
    fun `getChannelId$Multichannel_sample_multichannel_widget_main_0`() {
        config?.setChannelId(0)
        assertEquals(null, config?.getChannelId())
    }

    @Test
    fun `getChannelId$Multichannel_sample_multichannel_widget_main_not_0`() {
        config?.setChannelId(10)
        assertEquals(10, config?.getChannelId())
    }

    @Test
    fun `isEnableLog$Multichannel_sample_multichannel_widget_main`() {
        config?.isEnableLog()?.let {
            assertFalse(it)
        }

        config?.setEnableLog(true)
        config?.isEnableLog()?.let {
            assertTrue(it)
        }
    }

    @Test
    fun `isEnableNotification$Multichannel_sample_multichannel_widget_main`() {
        config?.setEnableNotification(false)
        config?.isEnableNotification()?.let {
            assertFalse(it)
        }

        config?.setEnableNotification(true)
        config?.isEnableNotification()?.let {
            assertTrue(it)
        }
    }

    @Test
    fun `getNotificationListener$Multichannel_sample_multichannel_widget_main`() {
        config?.setNotificationListener(null)
        assertNull(
            config?.getNotificationListener()
        )

        config?.setNotificationListener(
            object : MultichannelNotificationListener {
                override fun handleMultichannelListener(context: Context?, qiscusComment: QMessage?) {

                }

            }
        )
        assertNotNull(
            config?.getNotificationListener()
        )
    }

    @Test
    fun `getNotificationIcon$Multichannel_sample_multichannel_widget_main`() {
        config?.setNotificationIcon(R.drawable.ic_notification)
        assertNotNull(
            config?.getNotificationIcon()
        )
    }

    @Test
    fun `isSessional$Multichannel_sample_multichannel_widget_main`() {
        config?.isSessional()?.let {
            assertTrue(it)
        }

        config?.setSessional(true)
        config?.isSessional()?.let {
            assertTrue(
                it
            )
        }
    }

    @Test
    fun `getRoomTitle$Multichannel_sample_multichannel_widget_main`() {
        assertNotNull(
            config?.getRoomTitle()
        )

        config?.setRoomTitle("title")
        assertNotNull(
            config?.getRoomTitle()
        )
    }

    @Test
    fun `getRoomSubtitle$Multichannel_sample_multichannel_widget_main`() {
        assertNotNull(
            config?.getRoomSubtitle()
        )

        config?.setRoomSubtitle(RoomSubtitle.EDITABLE, "subtitle")
        assertNotNull(
            config?.getRoomSubtitle()
        )
    }

    @Test
    fun `getRoomSubtitleType$Multichannel_sample_multichannel_widget_main`() {
        config?.setRoomSubtitle(RoomSubtitle.EDITABLE, "subtitle")
        assertEquals(RoomSubtitle.EDITABLE, config?.getRoomSubtitleType())

        config?.setRoomSubtitle(RoomSubtitle.DISABLE, "subtitle")
        assertEquals(RoomSubtitle.DISABLE, config?.getRoomSubtitleType())

        config?.setRoomSubtitle(RoomSubtitle.ENABLE, "subtitle")
        assertEquals(RoomSubtitle.ENABLE, config?.getRoomSubtitleType())

    }

    @Test
    fun `isShowSystemMessage$Multichannel_sample_multichannel_widget_main`() {
        config?.isShowSystemMessage()?.let {
            assertTrue(it)
        }

        config?.setShowSystemMessage(true)
        config?.isShowSystemMessage()?.let {
            assertTrue(it)
        }
    }

    @Test
    fun `isAvatarActived$Multichannel_sample_multichannel_widget_main`() {
        config?.setAvatar(Avatar.ENABLE)
        config?.isAvatarActived()?.let {
            assertTrue(it)
        }

        config?.setAvatar(Avatar.DISABLE)
        config?.isAvatarActived()?.let {
            assertFalse(it)
        }

    }
}