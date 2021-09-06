package com.qiscus.qiscusmultichannel.ui.chat

import android.webkit.MimeTypeMap
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.sdk.chat.core.data.model.*
import com.qiscus.sdk.chat.core.event.QMessageDeletedEvent
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent
import com.qiscus.sdk.chat.core.presenter.QiscusChatRoomEventHandler
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import com.qiscus.sdk.chat.core.util.QiscusTextUtil
import id.zelory.compressor.Compressor
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.util.Pair as APair

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatRoomPresenter(var room: QChatRoom) : QiscusChatRoomEventHandler.StateListener {

    var view: ChatRoomView? = null
    private val qiscusAccount: QAccount = Const.qiscusCore()?.qiscusAccount!!
    private val roomEventHandler = QiscusChatRoomEventHandler(Const.qiscusCore(), this.room, this)

    private val commentComparator =
        { lhs: QMessage, rhs: QMessage -> rhs.timestamp.compareTo(lhs.timestamp) }

    fun attachView(view: ChatRoomView) {
        this.view = view
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun detachView() {
        this.view = null
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun sendComment(message: QMessage) {
//        view.onSendingComment(qiscusComment)

        val qAccount: QAccount = Const.qiscusCore()?.qiscusAccount!!
        val qUser = QUser()
        qUser.avatarUrl = qAccount.avatarUrl
        qUser.id = qAccount.id
        qUser.extras = qAccount.extras
        qUser.name = qAccount.name
        message.sender = qUser

        if (message.type == QMessage.Type.TEXT && message.text.trim().isEmpty()) {
            return
        }

        val subscription = Const.qiscusCore()?.api?.sendMessage(message)
            ?.doOnSubscribe { Const.qiscusCore()?.dataStore?.addOrUpdate(message) }
            ?.doOnNext { this.commentSuccess(it) }
            ?.doOnError { commentFail(it, message) }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
//            .compose(bindToLifecycle())
            ?.subscribe({ commentSend ->
                if (commentSend.chatRoomId == room.id) {
                    view?.onSuccessSendComment(commentSend)
                }
            }, { throwable ->
                throwable.printStackTrace()
                if (message.chatRoomId == room.id) {
                    view?.onFailedSendComment(message)
                }
            })

//        pendingTask.put(qiscusComment, subscription)
    }

    fun sendComment(content: String) {
        val qiscusComment = QMessage.generateMessage(room.id, content)
        sendComment(qiscusComment)
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
        val savedQMessage = Const.qiscusCore()?.dataStore?.getComment(qiscusComment.uniqueId)!!
        if (savedQMessage != null && savedQMessage.status > qiscusComment.status) {
            qiscusComment.status = savedQMessage.status
        }
        Const.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment)
    }

    private fun commentFail(throwable: Throwable, qiscusComment: QMessage) {
//        pendingTask.remove(qiscusComment)
        if (!Const.qiscusCore()?.dataStore?.isContains(qiscusComment)!!) { //Have been deleted
            return
        }

        var state = QMessage.STATE_PENDING
        if (mustFailed(throwable, qiscusComment)) {
            qiscusComment.isDownloading = false
            state = QMessage.STATE_FAILED
        }

        val savedQMessage = Const.qiscusCore()?.dataStore?.getComment(qiscusComment.uniqueId)
        if (savedQMessage != null && savedQMessage.status > QMessage.STATE_SENDING) {
            return
        }

        qiscusComment.status = state
        Const.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment)
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
            ?.filter { qiscusChatRoomListPair -> qiscusChatRoomListPair != null }
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())
//            .compose(bindToLifecycle())
            ?.subscribe({ roomData ->
                view?.initRoomData(roomData.second!!, roomData.first!!)
                view?.dismissLoading()
            }, { throwable ->
                throwable.printStackTrace()
                view?.dismissLoading()
            })
    }

    private fun getLocalComments(
        count: Int
    ): Observable<List<QMessage>> {
        return Const.qiscusCore()?.dataStore?.getObservableComments(room.id, 2 * count)
            ?.flatMap { Observable.from(it) }
            ?.toSortedList(commentComparator)
            ?.map {
                if (it.size > 0) {
                    it.subList(0, if (it.size > count) count else it.size)
                } else it
            }
            ?.subscribeOn(Schedulers.io())!!
    }

    private fun getInitRoomData(): Observable<APair<QChatRoom, List<QMessage>>> {
        return Const.qiscusCore()?.api?.getChatRoomWithMessages(room.id)
            ?.doOnError { it.printStackTrace() }
            ?.doOnNext { roomData ->
                roomEventHandler.setChatRoom(roomData.first)

                roomData.second?.sortWith(Comparator { lhs, rhs -> rhs.timestamp.compareTo(lhs.timestamp) })

                Const.qiscusCore()?.dataStore?.addOrUpdate(roomData.first)
            }
            ?.doOnNext { roomData ->
                for (qiscusComment in roomData.second!!) {
                    Const.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment)
                }
            }
            ?.subscribeOn(Schedulers.io())
            ?.onErrorReturn { null }!!
    }

    fun loadOlderCommentThan(qiscusComment: QMessage) {
        view?.showLoading()
        Const.qiscusCore()?.dataStore
            ?.getObservableOlderCommentsThan(qiscusComment, room.id, 40)
            ?.flatMap { Observable.from(it) }
            ?.filter { qiscusComment1 -> qiscusComment.id == -1L || qiscusComment1.id < qiscusComment.id }
            ?.toSortedList(commentComparator)
            ?.map {
                if (it.size >= 20) {
                    it.subList(0, 20)
                }
                it
            }
            ?.doOnNext { updateRepliedSender(it) }
            ?.flatMap { comments ->
                if (isValidOlderComments(comments, qiscusComment))
                    Observable.from<QMessage>(comments).toSortedList(commentComparator)
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
            }
            ?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view?.onLoadMoreComments(it)
                view?.dismissLoading()
            }, { throwable ->
                throwable.printStackTrace()
                view?.dismissLoading()
            })
    }

    fun sendFile(file: File) {
        sendFile(file, null)
    }

    fun sendFile(file: File, caption: String?) {
        var compressedFile = file
        if (QiscusFileUtil.isImage(file.path) && !file.name.endsWith(".gif")) {
            try {
                compressedFile = Compressor(Const.qiscusCore()?.apps).compressToFile(file)
            } catch (e: NullPointerException) {
                view?.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file_mc))
                return
            } catch (e: IOException) {
                view?.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file_mc))
                return
            }

        } else {
            compressedFile = QiscusFileUtil.saveFile(compressedFile)
        }

        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            view?.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file_mc))
            return
        }

        val json = JSONObject()
        try {
            json.put("url", compressedFile.path)
                .put("caption", caption)
                .put("file_name", file.name)
                .put("size", file.length())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val qiscusComment =
            QMessage.generateFileAttachmentMessage(room.id, file.path, caption, file.name)
        qiscusComment.extras = json
        qiscusComment.isDownloading = true


        val qAccount: QAccount = Const.qiscusCore()?.qiscusAccount!!
        val qUser = QUser()
        qUser.avatarUrl = qAccount.avatarUrl
        qUser.id = qAccount.id
        qUser.extras = qAccount.extras
        qUser.name = qAccount.name
        qiscusComment.sender = qUser

        view?.onSendingComment(qiscusComment)
        val finalCompressedFile = compressedFile
        val subscription = Const.qiscusCore()?.api
            ?.upload(compressedFile) { percentage ->
                qiscusComment.progress = percentage.toInt()
            }
            ?.doOnSubscribe { Const.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment) }
            ?.flatMap { uri ->
                json.put("url", uri)
                qiscusComment.extras = json
                qiscusComment.updateAttachmentUrl(uri.toString())
                Const.qiscusCore()?.api?.sendMessage(qiscusComment)
            }
            ?.doOnNext { commentSend ->
                Const.qiscusCore()?.dataStore
                    ?.addOrUpdateLocalPath(
                        commentSend.chatRoomId,
                        commentSend.id,
                        finalCompressedFile.absolutePath
                    )
                qiscusComment.isDownloading = false
                commentSuccess(commentSend)
            }
            ?.doOnError { throwable -> commentFail(throwable, qiscusComment) }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
//            .compose(bindToLifecycle())
            ?.subscribe({ commentSend ->
                if (commentSend.chatRoomId == room.id) {
                    view?.onSuccessSendComment(commentSend)
                }
            }, { throwable ->
                throwable.printStackTrace()
                if (qiscusComment.chatRoomId == room.id) {
                    view?.onFailedSendComment(qiscusComment)
                }
            })
    }

    private fun isValidOlderComments(
        qiscusComments: List<QMessage>,
        lastQMessage: QMessage
    ): Boolean {
        var qiscusComments = qiscusComments
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
        for (comment in comments) {
            if (comment.type == QMessage.Type.REPLY) {
                val repliedComment = comment.replyTo
                if (repliedComment != null) {
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
    }

    fun downloadFile(qiscusComment: QMessage, uri: String, fileName: String) {
        if (qiscusComment.isDownloading) {
            return
        }

        val file = Const.qiscusCore()?.dataStore?.getLocalPath(qiscusComment.id)
        if (file == null) {
            qiscusComment.isDownloading = true
            Const.qiscusCore()?.api
                ?.downloadFile(
                    uri, fileName
                ) { percentage -> qiscusComment.progress = percentage.toInt() }
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnNext { file1 ->
                    QiscusFileUtil.notifySystem(file1)
                    qiscusComment.isDownloading = false
                    Const.qiscusCore()?.dataStore?.addOrUpdateLocalPath(
                        qiscusComment.chatRoomId, qiscusComment.id,
                        file1.absolutePath
                    )
                }
                ?.subscribe({ file1 ->
                    if (qiscusComment.type == QMessage.Type.FILE || qiscusComment.type == QMessage.Type.VIDEO) {
                        view?.onFileDownloaded(
                            file1,
                            MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(qiscusComment.extension)
                        )
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                    qiscusComment.isDownloading = false
                    view?.showError(QiscusTextUtil.getString(R.string.qiscus_failed_download_file_mc))
                })
        } else {
//            if (qiscusComment.getType() == QMessage.Type.IMAGE) {
//                view.startPhotoViewer(qiscusComment)
//            else {
            view?.onFileDownloaded(
                file,
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    QiscusFileUtil.getExtension(
                        fileName
                    )
                )
            )

        }
    }

    private fun getCommentsFromNetwork(lastCommentId: Long): Observable<List<QMessage>> {
        return Const.qiscusCore()?.api?.getPreviousMessagesById(room.id, 20, lastCommentId)
            ?.doOnNext { qiscusComment ->
                Const.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment)
                qiscusComment.chatRoomId = room.id
            }
            ?.toSortedList(commentComparator)
            ?.subscribeOn(Schedulers.io())!!
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
                    Const.qiscusCore()?.dataStore?.getComment(event.commentUniqueId)
                if (qiscusComment != null) {
                    qiscusComment.status = QMessage.STATE_READ
                    Const.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment)
                    QiscusAndroidUtil.runOnUIThread {
                        view?.updateComment(qiscusComment)
                    }
                }
            }
        }
    }

    @Subscribe
    fun onMessageDeleted(event: QMessageDeletedEvent) {
        Const.qiscusCore()?.dataStore?.delete(event.qiscusComment)
        QiscusAndroidUtil.runOnUIThread { view?.onCommentDeleted(event.qiscusComment) }
    }

    private fun onGotNewComment(qiscusComment: QMessage) {
        if (qiscusComment.chatRoomId == room.id) {
            QiscusAndroidUtil.runOnBackgroundThread {
                if (!qiscusComment.sender.id.equals(
                        qiscusAccount.id,
                        ignoreCase = true
                    ) && Const.qiscusCore()?.cacheManager?.lastChatActivity?.first!!
                ) {
                    Const.qiscusCore()?.pusherApi?.markAsRead(room.id, qiscusComment.id)
                }
            }
            view?.onNewComment(qiscusComment)
            handleIsResolvedMsg(qiscusComment)

        }
        if (qiscusComment.type == QMessage.Type.SYSTEM_EVENT) {
            if (qiscusComment.text.contains("as resolved")) {
                view?.showNewChatButton(true)
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
        Const.qiscusCore()?.api?.getChatRoomInfo(room.id)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { chatRoom ->
                chatRoom.extras.getBoolean("is_resolved").let { resolved ->
                    QiscusMultichannelWidget.instance.component.qiscusChatRepository.checkSessional(
                        Const.qiscusCore()!!.appId,
                        {
                            it.data.isSessional?.let {
                                view?.onSessionalChange(it)
                                view?.showNewChatButton(resolved)
                            }
                        },
                        {
                            // do nothing
                        })
                }
            }
    }

    private fun handleIsResolvedMsg(qiscusComment: QMessage) {
        if (qiscusComment.type == QMessage.Type.LINK) {
            val url = qiscusComment.extras.getString("survey_link")
            view?.openWebview(url)
        }
    }

    fun deleteComment(comment: QMessage) {
        view?.showLoading()
        QiscusAndroidUtil.runOnBackgroundThread {
            Const.qiscusCore()?.dataStore?.delete(comment)
        }
        view?.dismissLoading()
        view?.onCommentDeleted(comment)

        Const.qiscusCore()?.api?.deleteMessages(listOf(comment.uniqueId))
            ?.flatMap { Observable.from(it) }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                view?.dismissLoading()
                view?.onCommentDeleted(it)
            }, {
                view?.dismissLoading()
            })

    }

    override fun onChatRoomNameChanged(name: String?) {

    }

    override fun onChangeLastDelivered(lastDeliveredCommentId: Long) {
        QiscusAndroidUtil.runOnUIThread {
            view?.updateLastDeliveredComment(lastDeliveredCommentId)
        }
    }

    override fun onChatRoomMemberRemoved(member: QParticipant?) {

    }

    override fun onUserTypng(email: String?, typing: Boolean) {
        view?.onUserTyping(email, typing)
    }

    override fun onChatRoomMemberAdded(member: QParticipant?) {

    }

    override fun onChangeLastRead(lastReadCommentId: Long) {
        QiscusAndroidUtil.runOnUIThread {
            view?.updateLastReadComment(lastReadCommentId)
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
    }
}