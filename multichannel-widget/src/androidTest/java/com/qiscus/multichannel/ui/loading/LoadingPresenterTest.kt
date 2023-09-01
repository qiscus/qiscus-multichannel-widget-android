package com.qiscus.multichannel.ui.loading

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.ui.loading.LoadingPresenter
import com.qiscus.multichannel.util.MultichanelChatWidget
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.anyObject
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers

import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

@ExtendWith(InstrumentationBaseTest::class)
internal class LoadingPresenterTest: InstrumentationBaseTest() {

    /*private var presenter: LoadingPresenter? = null
    private var widget: MultichanelChatWidget? = null

    @BeforeAll
    fun setUp() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)
        widget = mock()

        val core: QiscusCore = mock()
        whenever(core.apps).thenReturn(application)
        MultichannelConst.setQiscusCore(core)

    }

    private fun getView(): LoadingPresenter.LoadingView {
        return object : LoadingPresenter.LoadingView {
            override fun onError(message: String) {
                // ignored
            }

            override fun onSuccess(room: QChatRoom) {
                // ignored
            }

        }
    }

    @BeforeEach
    fun before() {
        clearInvocations(widget)
//        presenter = LoadingPresenter(widget!!)
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
        presenter?.detach()
        widget = null
        presenter = null
    }

    // without view
    @Test
    fun initiateChatHasMigrationWithoutView() {
        QiscusChatLocal.setHasMigration(true)

        presenter?.initiateChat(
            "username" , "userId" , "avatar" , "extras", null, arrayListOf()
        )

        val onSuccess = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        /*verify(widget)?.loginMultiChannel(
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(),
            onSuccess.capture(), onError.capture()
        )*/

        onSuccess.lastValue.invoke(QAccount())
    }

    @Test
    fun initiateChatWithoutView() {
        QiscusChatLocal.setHasMigration(false)

        presenter?.initiateChat(
            "username" , "userId" , "avatar" , "extras", null, arrayListOf()
        )

        val onSuccess = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        /*verify(widget)?.loginMultiChannel(
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(),
            onSuccess.capture(), onError.capture()
        )*/

        onSuccess.lastValue.invoke(QAccount())
    }

    @Test
    fun initiateChatErrorWithoutView() {
        presenter?.initiateChat(
            "username" , "userId" , "avatar" , "extras" , null, arrayListOf()
        )

        val onSuccess = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        /*verify(widget)?.loginMultiChannel(
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), 
            onSuccess.capture(), onError.capture()
        )*/

        onError.lastValue.invoke(Throwable("msg"))
    }

    @Test
    fun openRoomByIdWithoutView() {
        QiscusChatLocal.setRoomId(100L)

        presenter?.openRoomById()

        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(
            ArgumentMatchers.eq(100L), onSuccess.capture(), onError.capture()
        )

        onSuccess.lastValue.invoke(QChatRoom())
    }

    @Test
    fun openRoomByIdErrorWithoutView() {
        QiscusChatLocal.setRoomId(100L)

        presenter?.openRoomById()

        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(
            ArgumentMatchers.eq(100L), onSuccess.capture(), onError.capture()
        )

        onError.lastValue.invoke(Throwable("msg"))
    }

    // with view
    @Test
    fun initiateChatHasMigrationWithView() {
        presenter?.attachView(getView())

        QiscusChatLocal.setHasMigration(true)

        presenter?.initiateChat(
            "username" , "userId" , "avatar" , "extras" , null, arrayListOf()
        )

        val onSuccess = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        /*verify(widget)?.loginMultiChannel(
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), 
            onSuccess.capture(), onError.capture()
        )*/

        onSuccess.lastValue.invoke(QAccount())
    }
    @Test
    fun initiateChatWithView() {
        presenter?.attachView(getView())

        QiscusChatLocal.setHasMigration(false)

        presenter?.initiateChat(
            "username" , "userId" , "avatar" , "extras" , null, arrayListOf()
        )

        val onSuccess = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        /*verify(widget)?.loginMultiChannel(
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(),
            onSuccess.capture(), onError.capture()
        )*/

        onSuccess.lastValue.invoke(QAccount())
    }

    @Test
    fun initiateChatErrorWithView() {
        presenter?.attachView(getView())

        presenter?.initiateChat(
            "username" , "userId" , "avatar" , "extras" , null, arrayListOf()
        )

        val onSuccess = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        /*verify(widget)?.loginMultiChannel(
            anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(),
            onSuccess.capture(), onError.capture()
        )*/

        onError.lastValue.invoke(Throwable("msg"))
    }

    @Test
    fun openRoomByIdWithView() {
        presenter?.attachView(getView())
        QiscusChatLocal.setRoomId(100L)

        presenter?.openRoomById()

        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(
            ArgumentMatchers.eq(100L), onSuccess.capture(), onError.capture()
        )

        onSuccess.lastValue.invoke(QChatRoom())
    }

    @Test
    fun openRoomByIdErrorWithView() {
        presenter?.attachView(getView())
        QiscusChatLocal.setRoomId(100L)

        presenter?.openRoomById()

        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(
            ArgumentMatchers.eq(100L), onSuccess.capture(), onError.capture()
        )

        onError.lastValue.invoke(Throwable("msg"))
    }*/
}