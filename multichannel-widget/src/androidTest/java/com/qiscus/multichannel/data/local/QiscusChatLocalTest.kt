package com.qiscus.multichannel.data.local

import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.sdk.chat.core.QiscusCore
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

@ExtendWith(InstrumentationBaseTest::class)
class QiscusChatLocalTest : InstrumentationBaseTest() {

    private var core: QiscusCore? = null

    @BeforeAll
    fun setUp() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)
        core = mock(QiscusCore::class.java)
    }

    @AfterAll
    fun tearDown() {
        QiscusChatLocal.clearPreferences()
        tearDownComponent()
        core = null
    }

    @Test
    fun runTest() {
        `when`(core!!.apps).thenReturn(application)
        MultichannelConst.setQiscusCore(core)

        QiscusChatLocal.apply {
            setHasMigration(false)
            getHasMigration()
            setRoomId(0L)
            getRoomId()
            getExtras()
            saveExtras("{\"key\" : \"value\"}")
            getExtras()
            saveUserProps(arrayListOf())
            getUserProps()
            saveUserId("")
            getUserId()
            saveAvatar("")
            getAvatar()
        }
    }

}