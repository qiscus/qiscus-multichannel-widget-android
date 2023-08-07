package com.qiscus.multichannel.ui.chat

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.util.Pair
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.data.model.response.ResponseInitiateChat
import com.qiscus.multichannel.data.repository.QiscusChatRepository
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.anyObject
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore
import com.qiscus.sdk.chat.core.data.model.*
import com.qiscus.sdk.chat.core.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi
import com.qiscus.sdk.chat.core.event.QMessageDeletedEvent
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusConst
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import retrofit2.HttpException
import retrofit2.Response
import rx.Observable
import java.io.File
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor

@Suppress("UNCHECKED_CAST")
@ExtendWith(InstrumentationBaseTest::class)
internal class ChatRoomPresenterTest : InstrumentationBaseTest() {

    private lateinit var manager: QiscusCacheManager
    private lateinit var dataStore: QiscusDataStore
    private lateinit var account: QAccount
    private lateinit var core: QiscusCore
    private var presenter: ChatRoomPresenter? = null
    private var repository: QiscusChatRepository? = null

    private val roomId: Long = 100L
    private val userSenderId = "user@mail.com"
    private val userSenderName = "userName"

    @BeforeAll
    fun setUp() {
        setUpComponent()
        setupMock()
        presenter = ChatRoomPresenter(getRoom(), repository!!).apply {
            detachView()
            attachView(getView())
        }
    }

    private fun setupMock() {
        MockitoAnnotations.openMocks(this)
        repository = mock()
        core = mock()
        account = mock()
        val apiPusher = mock<QiscusPusherApi>()
        dataStore = mock()
        manager = mock()
        val api = mock<QiscusApi>()

        whenever(
            api.getChatRoomWithMessages(ArgumentMatchers.eq(100L))
        ).thenReturn(
            Observable.error(Throwable("msg"))
        )
        whenever(core.taskExecutor).thenReturn(
            ScheduledThreadPoolExecutor(5)
        )
        whenever(core.appsHandler).thenReturn(
            Handler(Looper.getMainLooper())
        )

        whenever(account.id).thenReturn(userSenderId)
        whenever(account.avatarUrl).thenReturn("avatarUrl.com")
        whenever(account.extras).thenReturn(JSONObject())
        whenever(account.name).thenReturn(userSenderName)

        whenever(core.apps).thenReturn(application!!)
        whenever(core.api).thenReturn(api)
        whenever(core.cacheManager).thenReturn(manager)
        whenever(manager.lastChatActivity).thenReturn(
            Pair.create(true, roomId)
        )
        whenever(core.pusherApi).thenReturn(apiPusher)
        whenever(core.dataStore).thenReturn(dataStore)
        /*whenever(core.qiscusAccount).thenReturn(
            QAccount().apply {

            }
        )*/
        whenever(core.qiscusAccount).thenReturn(account)

        QiscusConst.setApps(application!!)
        MultichannelConst.setQiscusCore(core)
        QiscusAndroidUtil(core)
    }

    private fun getView(): ChatRoomPresenter.ChatRoomView {
        return object : ChatRoomPresenter.ChatRoomView {
            override fun showLoading() {
            }

            override fun dismissLoading() {
            }

            override fun showError(message: String) {
            }

            override fun initRoomData(comments: List<QMessage>, qiscusChatRoom: QChatRoom) {
            }

            override fun onSuccessSendComment(comment: QMessage) {
            }

            override fun onFailedSendComment(comment: QMessage) {
            }

            override fun onLoadMoreComments(comments: List<QMessage>) {
            }

            override fun onNewComment(comment: QMessage) {
            }

            override fun onCommentDeleted(comment: QMessage) {
            }

            override fun onSendingComment(comment: QMessage) {
            }

            override fun updateLastDeliveredComment(lastDeliveredCommentId: Long) {
            }

            override fun updateLastReadComment(lastReadCommentId: Long) {
            }

            override fun updateComment(comment: QMessage) {
            }

            override fun onUserTyping(email: String?, isTyping: Boolean) {
            }

            override fun onFileDownloaded(file: File, mimeType: String?) {
            }

            override fun showNewChatButton(it: Boolean) {
            }

            override fun refreshComments() {
            }

            override fun openWebview(url: String) {
            }

            override fun onSessionalChange(isSessional: Boolean) {
            }

            override fun onLoadReply(targetComment: QMessage) {
            }

        }
    }

    private fun getRoom(): QChatRoom = QChatRoom().apply {
        id = roomId
        participants = arrayListOf(
            QParticipant().apply {
                id = userSenderId
                name = userSenderName
            }
        )
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
        presenter?.let {
            it.attachView(getView())
            it.detachView()
        }
        presenter = null
    }

    @Test
    fun onChangeLastDeliveredTest() {
        presenter?.onChangeLastDelivered(roomId)
    }

    @Test
    fun onChangeLastReadTest() {
        presenter?.onChangeLastRead(roomId)
    }

    @Test
    fun onUserTypingTest() {
        presenter?.onUserTypng(userSenderId, true)
    }

    @Test
    fun onChatRoomNameChangedTest() {
        presenter?.onChatRoomNameChanged(null)
    }

    @Test
    fun onChatRoomMemberRemovedTest() {
        presenter?.onChatRoomMemberRemoved(null)
    }

    @Test
    fun onChatRoomMemberAddedTest() {
        presenter?.onChatRoomMemberAdded(null)
    }

    @Test
    fun onHandleIsResolvedMsgNotLinkTest() {
        val onHandle = extractMethode(presenter!!, "handleIsResolvedMsg", 1)
        onHandle.call(
            presenter!!, QMessage().apply {
                id = roomId
                rawType = "text"
                text = "message"
            }
        )
    }

    @Test
    fun onHandleIsResolvedMsgLinkTest() {
        val onHandle = extractMethode(presenter!!, "handleIsResolvedMsg", 1)
        onHandle.call(
            presenter!!, QMessage().apply {
                id = roomId
                rawType = "text"
                text = "http://www.qiscus.com"
                timestamp = Date()
                extras = JSONObject().put("survey_link", "http://www.qiscus.com")
            }
        )
    }

    @Test
    fun onCommentReceivedEventTest() {
        val sendMessage =  QMessage().apply {
            id = 101
            chatRoomId = roomId
            rawType = "text"
            text = "message"
            sender = QUser().apply {
                id = userSenderId
            }
            status = 2
        }

        whenever(dataStore.getChatRoom(ArgumentMatchers.eq(roomId))).thenReturn(
            QChatRoom().apply {
                id = roomId
                lastMessage = sendMessage
            }
        )

        presenter?.onCommentReceivedEvent(
            QMessageReceivedEvent(
               sendMessage
            )
        )
    }

    @Test
    fun onCommentReceivedEventRoomIdNotSameTest() {
        presenter?.onCommentReceivedEvent(
            QMessageReceivedEvent(
                QMessage().apply {
                    id = 101
                    chatRoomId = 200
                }
            )
        )
    }

    @Test
    fun onRoomReceivedEventTest() {
        presenter?.onRoomReceivedEvent(
            QiscusChatRoomEvent().apply {
                event = QiscusChatRoomEvent.Event.CUSTOM
                commentUniqueId = "uniqueId"
            }
        )
    }

    @Test
    fun onRoomReceivedEventMessageNullTest() {
        whenever(core.dataStore.getComment(anyObject())).thenReturn(null)

        presenter?.onRoomReceivedEvent(
            QiscusChatRoomEvent().apply {
                event = QiscusChatRoomEvent.Event.READ
                commentUniqueId = "uniqueId"
            }
        )
    }

    @Test
    fun onRoomReceivedEventSuccessTest() {
        whenever(core.dataStore.getComment(anyObject())).thenReturn(
            QMessage().apply {
                id = 201
            }
        )

        presenter?.onRoomReceivedEvent(
            QiscusChatRoomEvent().apply {
                event = QiscusChatRoomEvent.Event.READ
                commentUniqueId = "uniqueId"
            }
        )
    }

    @Test
    fun onRoomReceivedEventNullTest() {
        whenever(core.dataStore.getComment(anyObject())).thenReturn(
            QMessage().apply {
                id = 201
            }
        )

        presenter?.onRoomReceivedEvent(
            QiscusChatRoomEvent().apply {
                event = null
                commentUniqueId = "uniqueId"
            }
        )
    }

    @Test
    fun onMessageDeletedTest() {
        presenter?.onMessageDeleted(
            QMessageDeletedEvent(
                QMessage().apply {
                    id = 201
                }
            )
        )
    }

    @Test
    fun sendCommentTest() {
        val message = mock<QMessage>()
        whenever(dataStore.isContains(ArgumentMatchers.eq(message))).thenReturn(false)
        whenever(core.api.sendMessage(anyObject())).thenReturn(Observable.just(message))
        presenter?.sendComment( "text")
    }

    @Test
    fun sendCommentTextEmptyTest() {
        presenter?.sendComment(
            QMessage().apply {
                chatRoomId = roomId
                id = 201
                rawType = "text"
                text = ""
            }
        )
    }

    @Test
    fun sendCommentTypeNotTextEmptyTest() {
        val message = QMessage().apply {
            chatRoomId = roomId
            id = 201
            rawType = "reply"
            text = ""
        }

        whenever(core.api.sendMessage(ArgumentMatchers.eq(message))).thenReturn(Observable.just(message))

        presenter?.sendComment(message)
    }

    @Test
    fun sendCommentTextNotEmptyTest() {
        val message = QMessage().apply {
            chatRoomId = roomId
            id = 201
            rawType = "text"
            text = "not empty"
        }

        whenever(core.api.sendMessage(ArgumentMatchers.eq(message))).thenReturn(Observable.just(message))

        presenter?.sendComment(message)
    }

    @Test
    fun sendCommentRoomIdNotSameTest() {
        val message = QMessage().apply {
            chatRoomId = roomId + 120
            id = 201
            rawType = "text"
            text = "not empty"
        }
        whenever(core.api.sendMessage(ArgumentMatchers.eq(message))).thenReturn(Observable.just(message))

        presenter?.sendComment(message)
    }
    @Test
    fun sendCommentErrorTextTest() {
        val message =  QMessage().apply {
            chatRoomId = roomId
            id = 201
            rawType = "text"
            text = "not empty"
        }

        whenever(core.api.sendMessage(ArgumentMatchers.eq(message))).thenReturn(Observable.error(Throwable("message")))

        presenter?.sendComment(message)
    }

    @Test
    fun sendCommentErrorTextNotSameRoomIdTest() {
        val message =  QMessage().apply {
            chatRoomId = roomId + 1219
            id = 201
            rawType = "text"
            text = "not empty"
        }

        whenever(core.api.sendMessage(ArgumentMatchers.eq(message))).thenReturn(Observable.error(Throwable("message")))

        presenter?.sendComment(message)
    }

    @Test
    fun sendLocationTest() {
        val location =  QiscusLocation().apply {
            name = "maps_name"
            address = "mapas_address"
            latitude = 1.01
            longitude = 2.02
        }
        val message = mock<QMessage>()
        whenever(core.api.sendMessage(anyObject())).thenReturn(Observable.just(message))

        presenter?.sendLocation(location)
    }

    @Test
    fun sendReplyCommentTest() {
        val message = QMessage().apply {
            chatRoomId = roomId
            id = 102
            text = "text"
            sender = QUser().apply {
                id = "sender@mail.id"
                name = "nameSender"
            }
            rawType = "text"
            payload = "{ }"
        }
        whenever(core.api.sendMessage(anyObject())).thenReturn(Observable.just(message))

        presenter?.sendReplyComment("Reply_content", message)
    }

    @Test
    fun commentSuccessLocalNullTest() {
        whenever(dataStore.getComment(anyObject())).thenReturn(null)
        val onSuccess = extractMethode(presenter!!, "commentSuccess")
        onSuccess.call(presenter!!, QMessage().apply {

        })
    }

    @Test
    fun commentSuccessLocalStatusSentTest() {
        whenever(dataStore.getComment(anyObject())).thenReturn(
            QMessage().apply {
                status = QMessage.STATE_SENT
            }
        )
        val onSuccess = extractMethode(presenter!!, "commentSuccess")
        onSuccess.call(presenter!!, QMessage().apply {
            status = QMessage.STATE_SENT
        })
    }

    @Test
    fun commentSuccessLocalStatusReadTest() {
        whenever(dataStore.getComment(anyObject())).thenReturn(
            QMessage().apply {
                status = QMessage.STATE_READ
            }
        )
        val onSuccess = extractMethode(presenter!!, "commentSuccess")
        onSuccess.call(presenter!!, QMessage().apply {
            status = QMessage.STATE_SENT
        })
    }

    @Test
    fun commentFailTest() {
        val message = QMessage().apply {
            id = 32012
            uniqueId = "uniqueId"
            rawType = "text"
            text = "text"
            status = QMessage.STATE_FAILED
        }
        whenever(dataStore.isContains(ArgumentMatchers.eq(message))).thenReturn(true)
        whenever(dataStore.getComment(anyObject())).thenReturn(message)

        val error = HttpException(
            Response.error<ResponseBody>(500 ,
                ResponseBody.create("plain/text".toMediaTypeOrNull(),"some content"))
        )
        val commentFail = extractMethode(presenter!!, "commentFail")
        commentFail.call(presenter!!, error, message)
    }

    @Test
    fun commentFailErrorHttpTest() {
        val message = QMessage().apply {
            id = 32012
            uniqueId = "uniqueId"
            rawType = "text"
            text = "text"
            status = QMessage.STATE_FAILED
        }
        whenever(dataStore.isContains(ArgumentMatchers.eq(message))).thenReturn(true)
        whenever(dataStore.getComment(anyObject())).thenReturn(message)

        val error = HttpException(
            Response.error<ResponseBody>(
                ResponseBody.create("plain/text".toMediaTypeOrNull(),"some content"),
                okhttp3.Response.Builder()
                    .code(300)
                    .message("Response.multipleChoice()")
                    .protocol(Protocol.HTTP_1_1)
                    .request(Request.Builder().url("http://test-url/").build())
                    .receivedResponseAtMillis(1619053449513)
                    .sentRequestAtMillis(1619053443814)
                    .build())
        )
        val commentFail = extractMethode(presenter!!, "commentFail")
        commentFail.call(presenter!!, error, message)
    }

    @Test
    fun commentFailISAttachmentTest() {
        val message = QMessage().apply {
            id = 32012
            uniqueId = "uniqueId"
            rawType = "file_attachment"
            text = "[file] attachment [/file]"
            status = QMessage.STATE_FAILED

        }
        whenever(dataStore.isContains(ArgumentMatchers.eq(message))).thenReturn(true)
        whenever(dataStore.getComment(anyObject())).thenReturn(message)

        val commentFail = extractMethode(presenter!!, "commentFail")
        commentFail.call(presenter!!, Throwable("msg"), message)
    }

    @Test
    fun commentFailLocalNotContainTest() {
        val message = QMessage().apply {
            id = 32012
            uniqueId = "uniqueId"
            rawType = "text"
            text = "text"
            status = QMessage.STATE_FAILED
        }
        whenever(dataStore.isContains(ArgumentMatchers.eq(message))).thenReturn(false)

        val commentFail = extractMethode(presenter!!, "commentFail")
        commentFail.call(presenter!!, Throwable("msg"), message)
    }

    @Test
    fun commentFailLocalMessageNullTest() {
        val message = QMessage().apply {
            id = 32012
            uniqueId = "uniqueId"
            rawType = "text"
            text = "text"
            status = QMessage.STATE_FAILED
        }
        whenever(dataStore.isContains(ArgumentMatchers.eq(message))).thenReturn(true)
        whenever(dataStore.getComment(anyObject())).thenReturn(null)

        val error = JSONException(Throwable("msg"))
        val commentFail = extractMethode(presenter!!, "commentFail")
        commentFail.call(presenter!!, error, message)
    }

    @Test
    fun commentFailLocalMessageStatusSentTest() {
        val message = QMessage().apply {
            id = 32012
            uniqueId = "uniqueId"
            rawType = "text"
            text = "text"
            status = QMessage.STATE_SENT
        }
        whenever(dataStore.isContains(ArgumentMatchers.eq(message))).thenReturn(true)
        whenever(dataStore.getComment(anyObject())).thenReturn(message)

        val commentFail = extractMethode(presenter!!, "commentFail")
        commentFail.call(presenter!!, Throwable("msg"), message)
    }

    @Test
    fun getLocalCommentsTest() {
        getLocalComments(true, 1)
    }

    @Test
    fun getLocalCommentsListEmptyTest() {
        getLocalComments(false, 1)
    }

    private fun getLocalComments(isNoEmpty: Boolean, count: Int, isSuccess: Boolean = true): ArrayList<QMessage> {
        val result = arrayListOf<QMessage>()
       if (isNoEmpty) {

           val cal = Calendar.getInstance()
           result.add(
               QMessage().apply {
                   id = 102
                   chatRoomId = roomId
                   rawType = "text"
                   text = "text"
                   timestamp = cal.time
               }
           )

           if (count > 1) {
               val date = Calendar.getInstance().apply {
                   add(Calendar.DATE, -1)
               }
               result.add(
                   QMessage().apply {
                       id = 103
                       chatRoomId = roomId
                       rawType = "text"
                       text = "text"
                       timestamp = date.time
                   }
               )

               val date2 = Calendar.getInstance().apply {
                   add(Calendar.DATE, -2)
               }
               result.add(
                   QMessage().apply {
                       id = 104
                       chatRoomId = roomId
                       rawType = "text"
                       text = "text"
                       timestamp = date2.time
                   }
               )
           }
       }

        whenever(dataStore.getObservableComments(
            ArgumentMatchers.eq(roomId), ArgumentMatchers.eq(2 * count)
        )).thenReturn(
            if (isSuccess) Observable.just(result.toList()) else Observable.error(Throwable("msg"))
        )

        val getLocalComments = extractMethode(presenter!!, "getLocalComments")
        val value = getLocalComments.call(presenter!!, count)

        val expectation = if (count > 1) {
            result.removeAt(2)
            result
        } else result

        if (isSuccess) {
            val listResultValue = (value as Observable<*>).toBlocking().single()
            assertEquals(
                listResultValue,
                expectation
            )
        } else{
            (value as Observable<*>).toBlocking()
        }
        return expectation
    }

    @Test
    fun loadCommentsTest() {
        val count = 2

        val list = getLocalComments(true, count)
        getInitRoomData(list.toList())

        presenter?.loadComments(count)
    }

    @Test
    fun loadCommentsErrorTest() {
        val count = 2

        val list = getLocalComments(true, count, false)
        getInitRoomData(list.toList(), false)

        presenter?.loadComments(count)
    }

    private fun getInitRoomData(list: List<QMessage>, isSuccess: Boolean = true) {
        val pair = Pair.create(
            QChatRoom().apply {
                id = roomId
                participants = emptyList()
            }, list
        )

        whenever(
            core.api.getChatRoomWithMessages(
                ArgumentMatchers.eq(roomId)
            )
        ).thenReturn(
            if (isSuccess) Observable.just(pair) else Observable.error(Throwable("msg"))
        )

        val getInitRoomData = extractMethode(presenter!!, "getInitRoomData")
        val value = getInitRoomData.call(presenter!!)
        if (isSuccess) assertEquals(
            (value as Observable<*>).toBlocking().first(),
            pair
        )
    }

    @Test
    fun getCommentsFromNetworktest() {
        val lastCommentId: Long = 102

        whenever(
            core.api.getPreviousMessagesById(
                ArgumentMatchers.eq(roomId),
                ArgumentMatchers.eq(20),
                ArgumentMatchers.eq(lastCommentId)
            )
        ).thenReturn(
            Observable.just(
                QMessage().apply {
                    id = 102
                    chatRoomId = roomId
                    rawType = "text"
                    text = "text"
                    timestamp = Date()
                }
            )
        )

        val getCommentsFromNetwork = extractMethode(presenter!!, "getCommentsFromNetwork")
        val value = getCommentsFromNetwork.call(presenter!!, lastCommentId)
        (value as Observable<*>).toBlocking().single()
    }

    @Test
    fun handleIsResolvedMsgTest() {
        val message = QMessage().apply {
            id = 102
            chatRoomId = roomId
            rawType = "http://www.qiscus.com"
            text = "text"
            timestamp = Date()
            extras = JSONObject().put("survey_link", "http://www.qiscus.com")
        }
        val handleIsResolvedMsg = extractMethode(presenter!!, "handleIsResolvedMsg")
        handleIsResolvedMsg.call(presenter!!, message)
    }

    @Test
    fun handleIsResolvedMsgNotLinkTest() {
        val message = QMessage().apply {
            id = 102
            chatRoomId = roomId
            rawType = "text"
            text = "text"
            timestamp = Date()
        }
        val handleIsResolvedMsg = extractMethode(presenter!!, "handleIsResolvedMsg")
        handleIsResolvedMsg.call(presenter!!, message)
    }

    @Test
    fun deleteCommentTest() {
        val message = QMessage().apply {
            id = 102
            uniqueId = "unique_102"
            chatRoomId = roomId
            rawType = "text"
            text = "text"
            timestamp = Date()
        }

        whenever(core.api.deleteMessages(anyObject())).thenReturn(
            Observable.just(listOf(message))
        )
        presenter?.deleteComment(message)
    }

    @Test
    fun deleteCommentErrorTest() {
        val message = QMessage().apply {
            id = 102
            uniqueId = "unique_102"
            chatRoomId = roomId
            rawType = "text"
            text = "text"
            timestamp = Date()
        }

        whenever(core.api.deleteMessages(anyObject())).thenReturn(
            Observable.error(Throwable("msg"))
        )
        presenter?.deleteComment(message)
    }

    @Test
    fun checkRoomStatusTest() {
        val chattRoom = getRoom().apply {
            extras = JSONObject().put("is_resolved", true)
        }

        whenever(core.api.getChatRoomInfo(
            ArgumentMatchers.eq(roomId)
        )).thenReturn(
            Observable.just(chattRoom)
        )

        val checkRoomStatus = extractMethode(presenter!!, "checkRoomStatus")
        checkRoomStatus.call(presenter)
    }

    @Test
    fun chatSessionalTest() {
        chatSessional(true, isSessional = true)
    }

    @Test
    fun chatSessionalSessionalNullTest() {
        chatSessional(true, isSessional = null)
    }

    @Test
    fun chatSessionalErrorTest() {
        chatSessional(false, isSessional = false)
    }

    private fun chatSessional(isSuccess: Boolean, isSessional: Boolean?) {
        whenever(core.appId).thenReturn("appId")
        Mockito.clearInvocations(repository)

        val onSuccess = argumentCaptor<(ResponseInitiateChat) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        val extras = JSONObject().put("is_resolved", true)
        val checkRoomStatus = extractMethode(presenter!!, "checkIsSessional")
        checkRoomStatus.call(presenter!!, extras)

        verify(repository!!).checkSessional(
            anyObject(), onSuccess.capture(), onError.capture()
        )

        if (isSuccess) {
            val data = ResponseInitiateChat.Data(isSessional = isSessional)
            val result = ResponseInitiateChat(data, 200)
            onSuccess.lastValue.invoke(result)
        } else {
            onError.lastValue.invoke(Throwable("msg"))
        }
    }

    @Test
    fun cleanFailedCommentsTest() {
        val list = arrayListOf<QMessage>()
        list.add(
            QMessage().apply {
                id = 102
                uniqueId = "unique_102"
                chatRoomId = roomId
                rawType = "text"
                text = "text"
                timestamp = Date()
            }
        )
        list.add(
            QMessage().apply {
                id = -1L
                uniqueId = "unique_102"
                chatRoomId = roomId
                rawType = "text"
                text = "text"
                timestamp = Date()
            }
        )
        val cleanFailedComments = extractMethode(presenter!!, "cleanFailedComments")
        cleanFailedComments.call(presenter!!, list)
    }

    @Test
    fun updateRepliedSenderTest() {
        val list = arrayListOf<QMessage>()
        list.add(
            QMessage().apply {
                id = 102
                uniqueId = "unique_102"
                chatRoomId = roomId
                rawType = "reply"
                text = "text"
                timestamp = Date()
                replyTo =  createReplyTo(userSenderId)
                sender = QUser().apply {
                    id = userSenderId
                }
            }
        )
        list.add(
            QMessage().apply {
                id = 103
                uniqueId = "unique_102"
                chatRoomId = roomId
                rawType = "reply"
                text = "text"
                timestamp = Date()
                replyTo =  createReplyTo("user_reply@mail.com")
                sender = QUser().apply {
                    id = "${userSenderId}_Ok"
                }
            }
        )
        list.add(
            QMessage().apply {
                id = -1L
                uniqueId = "unique_1"
                chatRoomId = roomId
                rawType = "text"
                text = "text"
                timestamp = Date()
                sender = QUser().apply {
                    id = "${userSenderId}_Ok"
                }
            }
        )
        val updateRepliedSender = extractMethode(presenter!!, "updateRepliedSender")
        updateRepliedSender.call(presenter!!, list)
    }

    private fun createReplyTo(userReply: String): QMessage {
        val payload = JSONObject().apply {
            put("replied_comment_id", 1223)
            put("replied_comment_message", "msg")
            put("replied_comment_sender_username", "userReply")
            put("replied_comment_sender_email", userReply)
            put("replied_comment_type", "message_reply")
            put("replied_comment_payload", "{ }")
        }

        val replyTo = QMessage()
        replyTo.id = payload.getLong("replied_comment_id")
        replyTo.uniqueId = replyTo.id.toString() + ""
        replyTo.text = payload.getString("replied_comment_message")
        val qUser = QUser()
        qUser.name = payload.getString("replied_comment_sender_username")
        qUser.id = payload.getString("replied_comment_sender_email")
        replyTo.sender = qUser
        replyTo.sender.id = payload.getString("replied_comment_sender_email")
        replyTo.rawType = payload.optString("replied_comment_type")
        replyTo.payload = payload.optString("replied_comment_payload")
        return replyTo
    }

    @Test
    fun loadOlderCommentThanTest() {
        val list = cretateCommentList(10)
        loadOlderComment(list, false)
        presenter!!.loadOlderCommentThan(list[1])
    }

    @Test
    fun loadOlderCommentThan20Test() {
        val list = cretateCommentList(20)
        loadOlderComment(list, false)
        presenter!!.loadOlderCommentThan(list[1])
    }

    @Test
    fun loadOlderCommentThan30Test() {
        val list = cretateCommentList(30)
        loadOlderComment(list, true)
        presenter!!.loadOlderCommentThan(list[1])
    }

    @Test
    fun loadOlderCommentByReplyTargetExistTest() {
        val list = cretateCommentList(2)
        val target = QMessage().apply {
            id=102
            chatRoomId=100
            previousMessageId=0
            text = "text"
            timestamp= Date()
        }
        loadOlderComment(list, false)
        presenter!!.loadOlderCommentByReply(list[0], target)
    }

    @Test
    fun loadOlderCommentByReplyTest() {
        val list = cretateCommentList(2)
        val target = QMessage().apply {
            id=101
            chatRoomId=1045
            previousMessageId=10
            text = "texthgd"
            timestamp= Calendar.getInstance().apply {
                add(Calendar.DATE, -12)
            }.time
        }
        loadOlderComment(list, false)
        presenter!!.loadOlderCommentByReply(list[0], target)
    }

    @Test
    fun loadOlderCommentByReplyTargetNoteSameTest() {
        val message =  QMessage().apply {
            id = 103
            chatRoomId = roomId
            rawType = "text"
            text = "text"
            timestamp = Date()
        }
        val list = cretateCommentList(2)
        loadOlderComment(list, false)
        presenter!!.loadOlderCommentByReply(list[0], message)
    }

    @Test
    fun loadOlderCommentByReplyErrorTest() {
        val list = cretateCommentList(2)
        loadOlderComment(list, true)
        presenter!!.loadOlderCommentByReply(list[0], list[0])
    }

    private fun loadOlderComment(list: MutableList<QMessage>, isError: Boolean) {
        val message = QMessage().apply {
            id = 102
            chatRoomId = roomId
            rawType = "text"
            text = "text"
            timestamp = Date()
            status = if (list.size > 10) QMessage.STATE_SENT else QMessage.STATE_SENDING
        }
        whenever(
            core.api.getPreviousMessagesById(
                ArgumentMatchers.eq(roomId),
                ArgumentMatchers.eq(20),
                Mockito.anyLong()
            )
        ).thenReturn(
            Observable.just(message)
        )
        whenever(dataStore.getObservableOlderCommentsThan(
            anyObject(), ArgumentMatchers.eq(roomId), ArgumentMatchers.eq(40))
        ).thenReturn(
            if (isError) Observable.error(Throwable("msg")) else Observable.just(list)
        )

        val loadOlderComment = extractMethode(presenter!!, "loadOlderComment")
        val result = loadOlderComment.call(presenter!!, message)
        if (!isError) (result as Observable<*>).toBlocking().single()
    }

    private fun cretateCommentList(count: Int): MutableList<QMessage> {
        val list = arrayListOf<QMessage>()
        var idMsg = -1L
        var previousIdMsg = -1L

        for (i in 0 until count) {
            list.add(
                QMessage().apply {
                    id = idMsg
                    rawType = "text"
                    text = "text"
                    timestamp = Calendar.getInstance().apply {
                        add(Calendar.DATE, -i)
                    }.time
                    previousMessageId = previousIdMsg
                    status = if (count < 10) QMessage.STATE_SENT else QMessage.STATE_SENDING
                }
            )
            previousIdMsg = idMsg
            idMsg += (i + 1)
        }
        return list
    }

    @Test
    fun isValidOlderCommentsEmptyListTest() {
        val list = arrayListOf<QMessage>()
        val result = isValidOlderComments(list)
        assertFalse(result)
    }

    @Test
    fun isValidOlderCommentsEmptyListPreviousIdMinusTest() {
        val list = arrayListOf<QMessage>()
        val result = isValidOlderComments(list, -1L)
        assertFalse(result)
    }

    @Test
    fun isValidOlderCommentsTest() {
        val list = arrayListOf<QMessage>()
        list.add(
            QMessage().apply {
                id = 1L
                rawType = "text"
                text = "text"
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DATE, -10)
                }.time
                previousMessageId = 0L
            }
        )
        val result = isValidOlderComments(list, 1L, -1L)
        assertTrue(result)
    }

    @Test
    fun isValidOlderCommentsLastIdSameTest() {
        val list = arrayListOf<QMessage>()
        list.add(
            QMessage().apply {
                id = 100L
                rawType = "text"
                text = "text"
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DATE, -10)
                }.time
                previousMessageId = 0L
            }
        )
        val result = isValidOlderComments(list, 2L)
        assertFalse(result)
    }
    @Test
    fun isValidOlderCommentsLastPreviouseIdNotZeroTest() {
        val list = arrayListOf<QMessage>()
        list.add(
            QMessage().apply {
                id = 100L
                rawType = "text"
                text = "text"
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DATE, -10)
                }.time
                previousMessageId = 10L
            }
        )
        val result = isValidOlderComments(list, 2L)
        assertFalse(result)
    }

    @Test
    fun isValidOlderCommentsPreviousFalseTest() {
        val list = arrayListOf<QMessage>()
        list.add(
            QMessage().apply {
                id = 1L
                previousMessageId = 2L
            }
        )
        list.add(
            QMessage().apply {
                id = 2L
                previousMessageId = 1022L
            }
        )
        list.add(
            QMessage().apply {
                id = 6L
                previousMessageId = 1L
            }
        )
        val result = isValidOlderComments(list, 2L)
        assertFalse(result)
    }

    @Test
    fun isValidOlderCommentsPreviousTrueTest() {
        val list = arrayListOf<QMessage>()
        list.add(
            QMessage().apply {
                id = 1L
                previousMessageId = 2L
            }
        )
        list.add(
            QMessage().apply {
                id = 2L
                previousMessageId = 3L
            }
        )
        list.add(
            QMessage().apply {
                id = 3L
                previousMessageId = -1L
            }
        )
        val result = isValidOlderComments(list, 1L, -1L)
        assertTrue(result)
    }

    @Test
    fun isValidOlderCommentsLastMessageIdMinusTest() {
        val list = cretateCommentList(1)
        list.add(
            QMessage().apply {
                id = -1L
                rawType = "text"
                text = "text"
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DATE, -10)
                }.time
                previousMessageId = 2L
            }
        )
        val result = isValidOlderComments(list, 2L)
        assertTrue(result)
    }

    private fun isValidOlderComments(list: List<QMessage>, previousId: Long = -1L, lastId: Long = 100L): Boolean {
        val isValidOlderComments = extractMethode(presenter!!, "isValidOlderComments")
        return isValidOlderComments.call(
            presenter!!, list,
            if (previousId == -1L && list.isNotEmpty()) list[list.size-1] else QMessage().apply {
                id = lastId
                rawType = "text"
                text = "text"
                timestamp = Calendar.getInstance().apply {
                    add(Calendar.DATE, -1)
                }.time
                previousMessageId = previousId
            }
        ) as Boolean
    }

    @Test
    fun downloadFileTest() {
        val fileName = "fileName.pdf"
        val isShowLoading = true
        downloadFile(isShowLoading, fileName)
    }

    @Test
    fun downloadFileNotShowLoadingTest() {
        val fileName = "fileName.pdf"
        val isShowLoading = false
        downloadFile(isShowLoading, fileName)
    }

    @Test
    fun downloadFileSuccessFileTest() {
        val fileName = "fileName.pdf"
        val isShowLoading = false

        downloadFile(isShowLoading, fileName, fileNotNull = false)
    }

    @Test
    fun downloadFileSuccessVideoTest() {
        val fileName = "fileName.mp4"
        val isShowLoading = false
        downloadFile(isShowLoading, fileName, fileNotNull = false)
    }

    @Test
    fun downloadFileSuccessImgTest() {
        val fileName = "fileName.png"
        val isShowLoading = false
        downloadFile(isShowLoading, fileName, fileNotNull = false)
    }

    @Test
    fun downloadFileSuccessErrorTest() {
        val fileName = "fileName.pdf"
        val isShowLoading = false
        downloadFile(isShowLoading, fileName, fileNotNull = false, isError = true)
    }

    @Test
    fun downloadFileShowLoadingTest() {
        val fileName = "fileName.pdf"
        val isShowLoading = true
        downloadFile(isShowLoading, fileName, fileNotNull = false, isError = true)
    }

    private fun downloadFile(showLoading: Boolean, fileName: String, fileNotNull: Boolean = true, isError: Boolean = false) {
        val path =  "data/data/src.com.file.example/file/$fileName"

        val message =  QMessage().apply {
            id = 100L
            rawType = "file_attachment"
            text = "[file] $path [/file]"
            timestamp = Date()
            previousMessageId = 0L
            isDownloading = showLoading
            payload = "{ \"file_name\" : \"$fileName\" }"
        }


        val outputDir: File = context!!.cacheDir
        val result = File.createTempFile(fileName, null, outputDir)

        val process = argumentCaptor<QiscusApi.ProgressListener>()

        if (fileNotNull) {
            whenever(dataStore.getLocalPath(anyLong())).thenReturn(result)
        } else {
            whenever(core.api.downloadFile(
                anyString(), anyString(), process.capture()
            )).thenReturn(
                if (isError) Observable.error(Throwable("msg")) else Observable.just(result)
            )
        }

        presenter?.downloadFile(message, fileName, path)

        if (!fileNotNull && !isError) {
            verify(core.api).downloadFile(
                anyString(), anyString(), process.capture()
            )
            process.lastValue.onProgress(100)
        }
    }

    private fun createQMessageToUpload(caption: String, file: File, json: JSONObject): QMessage {
        val createQMessageToUpload = extractMethode(presenter!!, "createQMessageToUpload")
        return createQMessageToUpload.call(presenter!!, file, json, caption) as QMessage
    }

    @Test
    fun uploadFileTest() {
        val fileName = "fileName.pdf"
        val caption = "caption"
        val path =  "data/data/src.com.file.example/file/$fileName"

        val outputDir: File = context!!.cacheDir
        val file = File.createTempFile(fileName, null, outputDir)

        val json = JSONObject()
        try {
            json.put("url", path)
                .put("caption", caption)
                .put("file_name", fileName)
                .put("size", path.length)
        } catch (e: JSONException) {
            // ignored
        }

        val message = createQMessageToUpload(caption, file, json)
        assertNotNull(message)
        uploadFile(message, file, json)

        presenter?.sendFile(file, caption, JSONObject())
    }

    @Test
    fun sendFileIsImageTest() {
        val fileName = "fileName.jpg"
        val caption = "caption"
        val path =  "data/data/src.com.file.example/file/$fileName"

        val outputDir: File = context!!.cacheDir
        val file = File.createTempFile(fileName, null, outputDir)

        presenter?.sendFile(file, caption, JSONObject())
    }

    @Test
    fun sendFileNotExistTest() {
        val fileName = "fileName.pdf"
        val caption = "caption"
        val path =  "data/data/src.com.file.example/file/$fileName"

        val file = File(path, fileName)

        presenter?.sendFile(file, caption, JSONObject())
    }

    @Test
    fun sendFileJsonErrorTest() {
        val fileName = "fileName.pdf"
        val caption = "caption"
        val path =  "data/data/src.com.file.example/file/$fileName"

        val file = File(path, fileName)

        val json: JSONObject = mock()
        whenever(json.put(anyString(), anyString())).thenThrow(JSONException("err"))

        presenter?.sendFile(file, caption, json)
    }

    @Test
    fun uploadFileRoomIdNotSameTest() {
        val fileName = "fileName.pdf"
        val caption = "caption"
        val path =  "data/data/src.com.file.example/file/$fileName"

        val outputDir: File = context!!.cacheDir
        val file = File.createTempFile(fileName, null, outputDir)

        val json = JSONObject()
        try {
            json.put("url", path)
                .put("caption", caption)
                .put("file_name", fileName)
                .put("size", path.length)
        } catch (e: JSONException) {
            // ignored
        }

        val message = createQMessageToUpload(caption, file, json).apply {
            chatRoomId = 120002L
        }
        assertNotNull(message)
        Mockito.clearInvocations(core.api)
        uploadFile(message, file, json)
    }

    @Test
    fun uploadFileErrorTest() {
        val fileName = "fileName.pdf"
        val caption = "caption"
        val path =  "data/data/src.com.file.example/file/$fileName"

        val outputDir: File = context!!.cacheDir
        val file = File.createTempFile(fileName, null, outputDir)

        val json = JSONObject()
        try {
            json.put("url", path)
                .put("caption", caption)
                .put("file_name", fileName)
                .put("size", path.length)
        } catch (e: JSONException) {
            // ignored
        }

        val message = createQMessageToUpload(caption, file, json)
        assertNotNull(message)
        uploadFile(message, file, json, true)
    }

    @Test
    fun uploadFileRoomIdNotSameErrorTest() {
        val fileName = "fileName.pdf"
        val caption = "caption"
        val path =  "data/data/src.com.file.example/file/$fileName"

        val outputDir: File = context!!.cacheDir
        val file = File.createTempFile(fileName, null, outputDir)

        val json = JSONObject()
        try {
            json.put("url", path)
                .put("caption", caption)
                .put("file_name", fileName)
                .put("size", path.length)
        } catch (e: JSONException) {
            // ignored
        }

        val message = createQMessageToUpload(caption, file, json).apply {
            chatRoomId = 120002L
        }
        assertNotNull(message)
        uploadFile(message, file, json, isError = true, isProgress = false)
    }

    private fun uploadFile(message: QMessage, file: File, json: JSONObject, isError: Boolean = false, isProgress: Boolean = true) {
        val progress = argumentCaptor<QiscusApi.ProgressListener>()
        whenever(core.api.upload(
            anyObject(), progress.capture()
        )
        ).thenReturn(
            if (isError) Observable.error(Throwable("msg")) else Observable.just(Uri.fromFile(file))
        )

        whenever(core.api.sendMessage(anyObject()))
            .thenReturn(
                Observable.just(
                    message.apply {
                        extras = json
                    }
                )
            )
        val uploadFile = extractMethode(presenter!!, "uploadFile")
        uploadFile.call(presenter!!, message, file, json)

        if (!isError && isProgress) {
            verify(core.api).upload(anyObject(), progress.capture())
            progress.lastValue.onProgress(100L)
        }
    }

    @Test
    fun get20ListTest() {
        val list = cretateCommentList(20)

        val get20List = extractMethode(presenter!!, "get20List")
        get20List.call(presenter!!, list)
    }

    @Test
    fun get20ListMoreTest() {
        val list = cretateCommentList(30)

        val get20List = extractMethode(presenter!!, "get20List")
        get20List.call(presenter!!, list)
    }

    @Test
    fun get20ListLessTest() {
        val list = cretateCommentList(10)

        val get20List = extractMethode(presenter!!, "get20List")
        get20List.call(presenter!!, list)
    }

    @Test
    fun onGotNewCommentRoomIdNotSameTest() {
        val message = QMessage().apply {
            id = 100
            chatRoomId = 200010
            rawType = "text"
            text = "text"
            sender = QUser().apply {
                id = account.id
            }}
        val onGotNewComment = extractMethode(presenter!!, "onGotNewComment", 2)
        onGotNewComment.call(presenter!!, message)
    }

    @Test
    fun onGotNewCommentUserIdNotSameTest() {
        val message = QMessage().apply {
            id = 100
            chatRoomId = roomId
            sender = QUser().apply {
                id = "User_id@not_same.com"
            }
            rawType = "system_event"
            text = "as resolved"
            payload = "{ }"
        }

        whenever(core.api.getChatRoomInfo(
            ArgumentMatchers.eq(roomId)
        )).thenReturn(
            Observable.empty()
        )
        whenever(dataStore.getChatRoom(ArgumentMatchers.eq(roomId))).thenReturn(
            QChatRoom().apply {
                id = roomId
                lastMessage = message
            }
        )

        val onGotNewComment = extractMethode(presenter!!, "onGotNewComment", 2)
        onGotNewComment.call(presenter!!, message)
    }

    @Test
    fun onGotNewCommentUserIdNotSameAndLastChatActivityFalseTest() {
        val message = QMessage().apply {
            id = 100
            chatRoomId = roomId
            sender = QUser().apply {
                id = "User_id@not_same.com"
            }
            rawType = "system_event"
            text = "as resolved"
            payload = "{ }"
        }

        whenever(manager.lastChatActivity).thenReturn(Pair.create(false, roomId))
        whenever(core.api.getChatRoomInfo(
            ArgumentMatchers.eq(roomId)
        )).thenReturn(
            Observable.empty()
        )
        whenever(dataStore.getChatRoom(ArgumentMatchers.eq(roomId))).thenReturn(
            QChatRoom().apply {
                id = roomId
                lastMessage = message
            }
        )

        val onGotNewComment = extractMethode(presenter!!, "onGotNewComment", 2)
        onGotNewComment.call(presenter!!, message)
        whenever(manager.lastChatActivity).thenReturn(Pair.create(true, roomId))
    }

    @Test
    fun onGotNewCommentLastChatActivityFalseTest() {
        val message = QMessage().apply {
            id = 100
            chatRoomId = roomId
            sender = QUser().apply {
                id = account.id
            }
            rawType = "system_event"
            text = "not_exist"
            payload = "{ }"
        }

        whenever(manager.lastChatActivity).thenReturn(Pair.create(false, roomId))
        whenever(core.api.getChatRoomInfo(
            ArgumentMatchers.eq(roomId)
        )).thenReturn(
            Observable.empty()
        )
        whenever(dataStore.getChatRoom(ArgumentMatchers.eq(roomId))).thenReturn(
            QChatRoom().apply {
                id = roomId
                lastMessage = message
            }
        )

        val onGotNewComment = extractMethode(presenter!!, "onGotNewComment", 2)
        onGotNewComment.call(presenter!!, message)

        whenever(manager.lastChatActivity).thenReturn(Pair.create(true, roomId))

    }
}