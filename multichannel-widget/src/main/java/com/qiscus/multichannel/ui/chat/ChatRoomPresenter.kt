package com.qiscus.multichannel.ui.chat

import android.webkit.MimeTypeMap
import com.qiscus.multichannel.R
import com.qiscus.multichannel.data.repository.QiscusChatRepository
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.data.model.*
import com.qiscus.sdk.chat.core.event.QMessageDeletedEvent
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent
import com.qiscus.sdk.chat.core.presenter.QiscusChatRoomEventHandler
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import com.qiscus.sdk.chat.core.util.QiscusTextUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import androidx.core.util.Pair as APair

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatRoomPresenter(
    private val room: QChatRoom,
    private val qiscusChatRepository: QiscusChatRepository
) : QiscusChatRoomEventHandler.StateListener {

    private var view: ChatRoomView? = null
    private val qiscusAccount: QAccount = MultichannelConst.qiscusCore()!!.qiscusAccount
    private val roomEventHandler =
        QiscusChatRoomEventHandler(MultichannelConst.qiscusCore(), this.room, this)

    private val commentComparator = { lhs: QMessage, rhs: QMessage -> rhs.timestamp.compareTo(lhs.timestamp) }

    fun attachView(view: ChatRoomView) {
        this.view = view
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun detachView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    fun sendComment(message: QMessage) {
        if (message.type == QMessage.Type.TEXT && message.text.trim().isEmpty()) {
            return
        }

        val qAccount = MultichannelConst.qiscusCore()!!.qiscusAccount
        message.sender = QUser().apply {
            avatarUrl = qAccount.avatarUrl
            id = qAccount.id
            extras = qAccount.extras
            name = qAccount.name
        }

        view!!.onSendingComment(message)

        MultichannelConst.qiscusCore()!!.api.sendMessage(message)
            .doOnSubscribe { MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(message) }
            .doOnNext { this.commentSuccess(it) }
            .doOnError { commentFail(it, message) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
//            .compose(bindToLifecycle())
            .subscribe({ commentSend ->
                if (commentSend.chatRoomId == room.id) {
                    view!!.onSuccessSendComment(commentSend)
                }
            }, {
                if (message.chatRoomId == room.id) {
                    view!!.onFailedSendComment(message)
                }
            })

//        pendingTask.put(qiscusComment, subscription)
    }

    fun sendComment(content: String) {
        sendComment(QMessage.generateMessage(room.id, content))
    }

    fun sendLocation(location: QiscusLocation) {
        val qiscusComment = QMessage.generateLocationMessage(room.id, location)
        sendComment(qiscusComment)
    }

    fun sendReplyComment(content: String, origin: QMessage) {
        val comment = QMessage.generateReplyMessage(room.id, content, origin)
        sendComment(comment)
    }

    private fun commentSuccess(qiscusComment: QMessage) {
//        pendingTask.remove(qiscusComment)
        qiscusComment.status = QMessage.STATE_SENT
        val savedQMessage =
            MultichannelConst.qiscusCore()!!.dataStore.getComment(qiscusComment.uniqueId)
        if (savedQMessage != null && savedQMessage.status > qiscusComment.status) {
            qiscusComment.status = savedQMessage.status
        }
        MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(qiscusComment)
    }

    private fun commentFail(throwable: Throwable, qiscusComment: QMessage) {
//        pendingTask.remove(qiscusComment)

        //Have been deleted
        if (!MultichannelConst.qiscusCore()!!.dataStore.isContains(qiscusComment)) {
            return
        }

        var state = QMessage.STATE_PENDING
        if (mustFailed(throwable, qiscusComment)) {
            qiscusComment.isDownloading = false
            state = QMessage.STATE_FAILED
        }

        val savedQMessage =
            MultichannelConst.qiscusCore()!!.dataStore.getComment(qiscusComment.uniqueId)
        if (savedQMessage != null && savedQMessage.status > QMessage.STATE_SENDING) {
            return
        }

        qiscusComment.status = state
        MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(qiscusComment)
    }

    private fun mustFailed(throwable: Throwable, qiscusComment: QMessage): Boolean {
        //Error response from server
        //Means something wrong with server, e.g user is not member of these room anymore
        return throwable is HttpException && throwable.code() >= 400 ||
                //if throwable from JSONException, e.g response from server not json as expected
                throwable is JSONException ||
                // if attachment type
                qiscusComment.isAttachment
    }

    fun loadComments(count: Int) {
        Observable.merge(getInitRoomData(), getLocalComments(count)
            .map { comments -> APair.create(room, comments) })
            .filter { qiscusChatRoomListPair -> qiscusChatRoomListPair != null }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ roomData ->
                view!!.initRoomData(roomData.second!!, roomData.first!!)
                view!!.dismissLoading()
            }, {
                view!!.dismissLoading()
            })
    }

    private fun getLocalComments(
        count: Int
    ): Observable<List<QMessage>> {
        return MultichannelConst.qiscusCore()!!.dataStore.getObservableComments(room.id, 2 * count)
            .flatMap { Observable.from(it) }
            .toSortedList(commentComparator)
            .map {
                if (it.size > 0) {
                    it.subList(0,
                        if (it.size > count) count
                        else it.size
                    )
                } else it
            }
            .subscribeOn(Schedulers.io())!!
    }

    private fun getInitRoomData(): Observable<APair<QChatRoom, List<QMessage>>> {
        return MultichannelConst.qiscusCore()!!.api.getChatRoomWithMessages(room.id)
            .doOnNext { roomData ->
                val chatRoom = roomData.first
                roomEventHandler.setChatRoom(chatRoom)

                roomData.second.sortWith { lhs, rhs ->
                    rhs.timestamp.compareTo(lhs.timestamp)
                }

                MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(chatRoom)
            }
            .doOnNext { roomData ->
                for (qiscusComment in roomData.second!!) {
                    MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(qiscusComment)
                }
            }
            .subscribeOn(Schedulers.io())
            .onErrorReturn { null }
    }

    fun loadOlderCommentByReply(qiscusComment: QMessage, targetComment: QMessage) {
        loadOlderComment(qiscusComment)
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                view!!.onLoadMoreComments(list)
                if (!list.contains(targetComment)) {
                    loadOlderCommentByReply(list[list.size - 1], targetComment)
                } else {
                    view!!.onLoadReply(targetComment)
                    view!!.dismissLoading()
                }
            }, {
                view!!.showError("message not found")
                view!!.dismissLoading()
            })
    }

    fun loadOlderCommentThan(qiscusComment: QMessage) {
        view!!.showLoading()
        loadOlderComment(qiscusComment)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                view!!.onLoadMoreComments(list)
                view!!.dismissLoading()
            }, {
                view!!.dismissLoading()
            })
    }

    private fun loadOlderComment(qiscusComment: QMessage): Observable<List<QMessage>> =
        MultichannelConst.qiscusCore()!!.dataStore
            .getObservableOlderCommentsThan(qiscusComment, room.id, 40)
            .flatMap { Observable.from(it) }
            .filter { qiscusComment1 -> qiscusComment.id == -1L || qiscusComment1.id < qiscusComment.id }
            .toSortedList(commentComparator)
            .map {get20List(it)}
            .doOnNext { updateRepliedSender(it) }
            .flatMap { comments -> validateMessage(comments, qiscusComment)}

    private fun validateMessage(comments: List<QMessage>, qiscusComment: QMessage) =
        if (isValidOlderComments(comments, qiscusComment))
            Observable.from(comments).toSortedList()
        else
            getCommentsFromNetwork(qiscusComment.id)
                .map { comments1 ->
                    for (localComment in comments) {
                        if (localComment.status <= QMessage.STATE_SENDING) {
                            comments1.toMutableList().add(localComment)
                        }
                    }
                    comments1
                }

    private fun get20List(list: List<QMessage>): List<QMessage> {
        if (list.size >= 20) list.subList(0, 20)
        return list
    }

    fun sendFile(file: File, caption: String, json: JSONObject) {
        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            view!!.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file_mc))
            return
        }

        try {
            json.put("url", file.path)
                .put("caption", caption)
                .put("file_name", file.name)
                .put("size", file.length())
        } catch (e: JSONException) {
            // ignored
        }

        val qiscusComment = createQMessageToUpload(file, json, caption)
        view!!.onSendingComment(qiscusComment)

        uploadFile(qiscusComment, file, json)
    }

    private fun createQMessageToUpload(file: File, json: JSONObject, caption: String): QMessage {
        val qiscusComment = QMessage.generateFileAttachmentMessage(room.id, file.path, caption, file.name)
        qiscusComment.extras = json
        qiscusComment.isDownloading = true

        val qAccount: QAccount = MultichannelConst.qiscusCore()!!.qiscusAccount!!
        val qUser = QUser()
        qUser.avatarUrl = qAccount.avatarUrl
        qUser.id = qAccount.id
        qUser.extras = qAccount.extras
        qUser.name = qAccount.name
        qiscusComment.sender = qUser

        return qiscusComment
    }

    private fun uploadFile(qiscusComment: QMessage, compressedFile: File, json: JSONObject) {
        MultichannelConst.qiscusCore()!!.api
            .upload(compressedFile) { percentage ->
                qiscusComment.progress = percentage.toInt()
            }
            .doOnSubscribe { MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(qiscusComment) }
            .flatMap { uri ->
                json.put("url", uri)
                qiscusComment.extras = json
                qiscusComment.updateAttachmentUrl(uri.toString())
                MultichannelConst.qiscusCore()!!.api.sendMessage(qiscusComment)
            }
            .doOnNext { commentSend ->
                MultichannelConst.qiscusCore()!!.dataStore
                    .addOrUpdateLocalPath(
                        commentSend.chatRoomId,
                        commentSend.id,
                        compressedFile.absolutePath
                    )
                qiscusComment.isDownloading = false
                commentSuccess(commentSend)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ commentSend ->
                if (commentSend.chatRoomId == room.id) {
                    view!!.onSuccessSendComment(commentSend)
                }
            }, {
                commentFail(it, qiscusComment)
                if (qiscusComment.chatRoomId == room.id) {
                    view!!.onFailedSendComment(qiscusComment)
                }
            })
    }

    private fun isValidOlderComments(
        listMessage: List<QMessage>,
        lastQMessage: QMessage
    ): Boolean {
        var qiscusComments = listMessage
        if (qiscusComments.isEmpty()) return false

        qiscusComments = cleanFailedComments(qiscusComments)
        var containsLastValidComment = qiscusComments.isEmpty() || lastQMessage.id == -1L
        val size = qiscusComments.size

        if (size == 1) {
            return qiscusComments[0].previousMessageId == 0L && lastQMessage.previousMessageId == qiscusComments[0].id
        }

        for (i in 0 until size - 1) {
            if (!containsLastValidComment && qiscusComments[i].id == lastQMessage.previousMessageId) {
                containsLastValidComment = true
            }

            if (qiscusComments[i].previousMessageId != qiscusComments[i + 1].id) {
                return false
            }
        }
        return containsLastValidComment
    }

    private fun cleanFailedComments(qiscusComments: List<QMessage>): List<QMessage> {
        val comments = ArrayList<QMessage>()
        for (qiscusComment in qiscusComments) {
            if (qiscusComment.id != -1L) {
                comments.add(qiscusComment)
            }
        }
        return comments
    }

    private fun updateRepliedSender(comments: List<QMessage>) {
        var repliedComment: QMessage
        for (comment in comments) {
            if (comment.type == QMessage.Type.REPLY) {
                repliedComment = comment.replyTo
                for (qiscusRoomMember in room.participants) {
                    if (repliedComment.sender.id == qiscusRoomMember.id) {
                        repliedComment.sender.name = qiscusRoomMember.name
                        comment.replyTo = repliedComment
                        break
                    }
                }

            }
        }
    }

    fun downloadFile(qiscusComment: QMessage, uri: String, fileName: String) {
        if (qiscusComment.isDownloading) {
            return
        }

        val file = MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(qiscusComment.id)
        if (file == null) {
            qiscusComment.isDownloading = true
            MultichannelConst.qiscusCore()!!.api
                .downloadFile(
                    uri, fileName
                ) { percentage -> qiscusComment.progress = percentage.toInt() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { file1 ->
                    QiscusFileUtil.notifySystem(file1)
                    qiscusComment.isDownloading = false
                    MultichannelConst.qiscusCore()!!.dataStore.addOrUpdateLocalPath(
                        qiscusComment.chatRoomId, qiscusComment.id,
                        file1.absolutePath
                    )
                }
                .subscribe({ file1 ->
                    val type = qiscusComment.type
                    if (type == QMessage.Type.FILE || type == QMessage.Type.VIDEO) {
                        try {
                            view!!.onFileDownloaded(
                                file1,
                                MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(qiscusComment.extension)
                            )
                        } catch (e: IllegalArgumentException) {
                            qiscusComment.isDownloading = false
                            view!!.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file_mc))
                        }
                    }
                }, {
                    qiscusComment.isDownloading = false
                    view!!.showError(QiscusTextUtil.getString(R.string.qiscus_failed_download_file_mc))
                })
        } else {
//            if (qiscusComment.getType() == QMessage.Type.IMAGE) {
//                view.startPhotoViewer(qiscusComment)
//            else {
            try {
                view!!.onFileDownloaded(file,
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        QiscusFileUtil.getExtension(fileName)
                    )
                )
            } catch (e: IllegalArgumentException) {
                qiscusComment.isDownloading = false
                view!!.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file_mc))
            }
        }
    }

    private fun getCommentsFromNetwork(lastCommentId: Long): Observable<List<QMessage>> {
        return MultichannelConst.qiscusCore()!!.api.getPreviousMessagesById(
            room.id,
            20,
            lastCommentId
        )
            .doOnNext { qiscusComment ->
                MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(qiscusComment)
                qiscusComment.chatRoomId = room.id
            }
            .toSortedList()
            .subscribeOn(Schedulers.io())!!
    }

    @Subscribe
    fun onCommentReceivedEvent(event: QMessageReceivedEvent) {
        if (event.qiscusComment.chatRoomId == room.id) {
            onGotNewComment(event.qiscusComment)
        }
    }

    @Subscribe
    fun onRoomReceivedEvent(event: QiscusChatRoomEvent) {
        when (event.event) {
            QiscusChatRoomEvent.Event.READ -> {
                val qiscusComment =
                    MultichannelConst.qiscusCore()!!.dataStore.getComment(event.commentUniqueId)
                if (qiscusComment != null) {
                    qiscusComment.status = QMessage.STATE_READ
                    MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(qiscusComment)
                    QiscusAndroidUtil.runOnUIThread {
                        view!!.updateComment(qiscusComment)
                    }
                }
            }
            else -> {/*ignored*/}
        }
    }

    @Subscribe
    fun onMessageDeleted(event: QMessageDeletedEvent) {
        MultichannelConst.qiscusCore()!!.dataStore.delete(event.qiscusComment)
        QiscusAndroidUtil.runOnUIThread { view!!.onCommentDeleted(event.qiscusComment) }
    }

    private fun onGotNewComment(qiscusComment: QMessage) {
        if (qiscusComment.chatRoomId == room.id) {
            QiscusAndroidUtil.runOnBackgroundThread {
                if (!qiscusComment.sender.id.equals(
                        qiscusAccount.id,
                        ignoreCase = true
                    ) && MultichannelConst.qiscusCore()!!.cacheManager.lastChatActivity.first
                ) {
                    MultichannelConst.qiscusCore()!!.pusherApi.markAsRead(room.id, qiscusComment.id)
                }
            }
            view!!.onNewComment(qiscusComment)
            handleIsResolvedMsg(qiscusComment)

        }
        if (qiscusComment.type == QMessage.Type.SYSTEM_EVENT) {
            if (qiscusComment.text.contains("as resolved")) {
                view!!.showNewChatButton(true)
            }

            checkRoomStatus()
        }

        if (qiscusComment.sender.id.equals(qiscusAccount.id, ignoreCase = true)) {
            QiscusAndroidUtil.runOnBackgroundThread { commentSuccess(qiscusComment) }
        } else {
            roomEventHandler.onGotComment(qiscusComment)
        }
    }

    private fun checkRoomStatus() {
        qiscusChatRepository.getCustomerRoomById(room.id,
            { room ->
                room.data?.customerRoom?.let {
                    checkIsSessional(it.isResolved)
                }
            }) {
            // do nothing
        }
    }

    private fun checkIsSessional(isResolved: Boolean) {
        qiscusChatRepository.checkSessional(
            MultichannelConst.qiscusCore()!!.appId,
            {
                it.data.isSessional?.let { isSessional ->
                    view!!.onSessionalChange(isSessional)
                    view!!.showNewChatButton(isResolved)
                }
            }) {
                // do nothing
            }
    }

    private fun handleIsResolvedMsg(qiscusComment: QMessage) {
        if (qiscusComment.type == QMessage.Type.LINK) {
            val url = qiscusComment.extras.getString("survey_link")
            view!!.openWebview(url)
        }
    }

    fun deleteComment(comment: QMessage) {
        view!!.showLoading()
        QiscusAndroidUtil.runOnBackgroundThread {
            MultichannelConst.qiscusCore()!!.dataStore.delete(comment)
        }
        view!!.dismissLoading()
        view!!.onCommentDeleted(comment)

        MultichannelConst.qiscusCore()!!.api.deleteMessages(listOf(comment.uniqueId))
            .flatMap { Observable.from(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view!!.dismissLoading()
                view!!.onCommentDeleted(it)
            }, {
                view!!.dismissLoading()
            })

    }

    override fun onChatRoomNameChanged(name: String?) { }

    override fun onChangeLastDelivered(lastDeliveredCommentId: Long) {
        QiscusAndroidUtil.runOnUIThread {
            view!!.updateLastDeliveredComment(lastDeliveredCommentId)
        }
    }

    override fun onChatRoomMemberRemoved(member: QParticipant?) { }

    override fun onUserTypng(email: String?, typing: Boolean) {
        view!!.onUserTyping(email, typing)
    }

    override fun onChatRoomMemberAdded(member: QParticipant?) { }

    override fun onChangeLastRead(lastReadCommentId: Long) {
        QiscusAndroidUtil.runOnUIThread {
            view!!.updateLastReadComment(lastReadCommentId)
        }
    }

    interface ChatRoomView {
        fun showLoading()

        fun dismissLoading()

        fun showError(message: String)

        fun initRoomData(comments: List<QMessage>, qiscusChatRoom: QChatRoom)

        fun onSuccessSendComment(comment: QMessage)

        fun onFailedSendComment(comment: QMessage)

        fun onLoadMoreComments(comments: List<QMessage>)

        fun onNewComment(comment: QMessage)

        fun onCommentDeleted(comment: QMessage)

        fun onSendingComment(comment: QMessage)

        fun updateLastDeliveredComment(lastDeliveredCommentId: Long)

        fun updateLastReadComment(lastReadCommentId: Long)

        fun updateComment(comment: QMessage)

        fun onUserTyping(email: String?, isTyping: Boolean)

        fun onFileDownloaded(file: File, mimeType: String?)

        fun showNewChatButton(it: Boolean)

        fun refreshComments()

        fun openWebview(url: String)

        fun onSessionalChange(isSessional: Boolean)

        fun onLoadReply(targetComment: QMessage)
    }
}