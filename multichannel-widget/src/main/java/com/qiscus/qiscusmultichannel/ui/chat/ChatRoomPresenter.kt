package com.qiscus.qiscusmultichannel.ui.chat

import android.webkit.MimeTypeMap
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.custom.QiscusCore
import com.qiscus.sdk.chat.core.custom.data.local.QiscusCacheManager
import com.qiscus.sdk.chat.core.custom.data.model.*
import com.qiscus.sdk.chat.core.custom.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.custom.data.remote.QiscusPusherApi
import com.qiscus.sdk.chat.core.custom.event.QiscusChatRoomEvent
import com.qiscus.sdk.chat.core.custom.event.QiscusCommentDeletedEvent
import com.qiscus.sdk.chat.core.custom.event.QiscusCommentReceivedEvent
import com.qiscus.sdk.chat.core.custom.presenter.QiscusChatRoomEventHandler
import com.qiscus.sdk.chat.core.custom.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.custom.util.QiscusFileUtil
import com.qiscus.sdk.chat.core.custom.util.QiscusTextUtil
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
class ChatRoomPresenter(var room: QiscusChatRoom) : QiscusChatRoomEventHandler.StateListener {

    var view: ChatRoomView? = null
    private val qiscusAccount: QiscusAccount = QiscusCore.getQiscusAccount()
    private val roomEventHandler = QiscusChatRoomEventHandler(this.room, this)

    private val commentComparator =
        { lhs: QiscusComment, rhs: QiscusComment -> rhs.time.compareTo(lhs.time) }

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

    private fun sendComment(message: QiscusComment) {
//        view.onSendingComment(qiscusComment)
        val subscription = QiscusApi.getInstance().sendMessage(message)
            .doOnSubscribe { QiscusCore.getDataStore().addOrUpdate(message) }
            .doOnNext { this.commentSuccess(it) }
            .doOnError { commentFail(it, message) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
//            .compose(bindToLifecycle())
            .subscribe({ commentSend ->
                if (commentSend.roomId == room.id) {
                    view?.onSuccessSendComment(commentSend)
                }
            }, { throwable ->
                throwable.printStackTrace()
                if (message.roomId == room.id) {
                    view?.onFailedSendComment(message)
                }
            })

//        pendingTask.put(qiscusComment, subscription)
    }

    fun sendComment(content: String) {
        val qiscusComment = QiscusComment.generateMessage(room.id, content)
        sendComment(qiscusComment)
    }

    fun sendLocation(location: QiscusLocation) {
        val qiscusComment = QiscusComment.generateLocationMessage(room.id, location)
        sendComment(qiscusComment)
    }

    fun sendReplyComment(content: String, origin: QiscusComment) {
        val comment = QiscusComment.generateReplyMessage(room.id, content, origin)
        sendComment(comment)
    }

    private fun commentSuccess(qiscusComment: QiscusComment) {
//        pendingTask.remove(qiscusComment)
        qiscusComment.state = QiscusComment.STATE_ON_QISCUS
        val savedQiscusComment = QiscusCore.getDataStore().getComment(qiscusComment.uniqueId)
        if (savedQiscusComment != null && savedQiscusComment.state > qiscusComment.state) {
            qiscusComment.state = savedQiscusComment.state
        }
        QiscusCore.getDataStore().addOrUpdate(qiscusComment)
    }

    private fun commentFail(throwable: Throwable, qiscusComment: QiscusComment) {
//        pendingTask.remove(qiscusComment)
        if (!QiscusCore.getDataStore().isContains(qiscusComment)) { //Have been deleted
            return
        }

        var state = QiscusComment.STATE_PENDING
        if (mustFailed(throwable, qiscusComment)) {
            qiscusComment.isDownloading = false
            state = QiscusComment.STATE_FAILED
        }

        val savedQiscusComment = QiscusCore.getDataStore().getComment(qiscusComment.uniqueId)
        if (savedQiscusComment != null && savedQiscusComment.state > QiscusComment.STATE_SENDING) {
            return
        }

        qiscusComment.state = state
        QiscusCore.getDataStore().addOrUpdate(qiscusComment)
    }

    private fun mustFailed(throwable: Throwable, qiscusComment: QiscusComment): Boolean {
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
//            .compose(bindToLifecycle())
            .subscribe({ roomData ->
                view?.initRoomData(roomData.second!!, roomData.first!!)
                view?.dismissLoading()
            }, { throwable ->
                throwable.printStackTrace()
                view?.dismissLoading()
            })
    }

    private fun getLocalComments(
        count: Int
    ): Observable<List<QiscusComment>> {
        return QiscusCore.getDataStore().getObservableComments(room.id, 2 * count)
            .flatMap { Observable.from(it) }
            .toSortedList(commentComparator)
            .map {
                if (it.size > 0) {
                    it.subList(0, if (it.size > count) count else it.size)
                } else it
            }
            .subscribeOn(Schedulers.io())
    }

    private fun getInitRoomData(): Observable<APair<QiscusChatRoom, List<QiscusComment>>> {
        return QiscusApi.getInstance().getChatRoomWithMessages(room.id)
            .doOnError { it.printStackTrace() }
            .doOnNext { roomData ->
                roomEventHandler.setChatRoom(roomData.first)

                roomData.second?.sortWith(Comparator { lhs, rhs -> rhs.time.compareTo(lhs.time) })

                QiscusCore.getDataStore().addOrUpdate(roomData.first)
            }
            .doOnNext { roomData ->
                for (qiscusComment in roomData.second!!) {
                    QiscusCore.getDataStore().addOrUpdate(qiscusComment)
                }
            }
            .subscribeOn(Schedulers.io())
            .onErrorReturn { null }
    }

    fun loadOlderCommentThan(qiscusComment: QiscusComment) {
        view?.showLoading()
        QiscusCore.getDataStore().getObservableOlderCommentsThan(qiscusComment, room.id, 40)
            .flatMap { Observable.from(it) }
            .filter { qiscusComment1 -> qiscusComment.id == -1L || qiscusComment1.id < qiscusComment.id }
            .toSortedList(commentComparator)
            .map {
                if (it.size >= 20) {
                    it.subList(0, 20)
                }
                it
            }
            .doOnNext { updateRepliedSender(it) }
            .flatMap { comments ->
                if (isValidOlderComments(comments, qiscusComment))
                    Observable.from<QiscusComment>(comments).toSortedList(commentComparator)
                else
                    getCommentsFromNetwork(qiscusComment.id)
                        .map { comments1 ->
                            for (localComment in comments) {
                                if (localComment.state <= QiscusComment.STATE_SENDING) {
                                    comments1.toMutableList().add(localComment)
                                }
                            }
                            comments1
                        }
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
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
                compressedFile = Compressor(QiscusCore.getApps()).compressToFile(file)
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
            QiscusComment.generateFileAttachmentMessage(room.id, file.path, caption, file.name)
        qiscusComment.extras = json
        qiscusComment.isDownloading = true

        view?.onSendingComment(qiscusComment)
        val finalCompressedFile = compressedFile
        val subscription = QiscusApi.getInstance()
            .upload(compressedFile) { percentage ->
                qiscusComment.progress = percentage.toInt()
            }
            .doOnSubscribe { QiscusCore.getDataStore().addOrUpdate(qiscusComment) }
            .flatMap { uri ->
                json.put("url", uri)
                qiscusComment.extras = json
                qiscusComment.updateAttachmentUrl(uri.toString())
                QiscusApi.getInstance().sendMessage(qiscusComment)
            }
            .doOnNext { commentSend ->
                QiscusCore.getDataStore()
                    .addOrUpdateLocalPath(
                        commentSend.roomId,
                        commentSend.id,
                        finalCompressedFile.absolutePath
                    )
                qiscusComment.isDownloading = false
                commentSuccess(commentSend)
            }
            .doOnError { throwable -> commentFail(throwable, qiscusComment) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
//            .compose(bindToLifecycle())
            .subscribe({ commentSend ->
                if (commentSend.roomId == room.id) {
                    view?.onSuccessSendComment(commentSend)
                }
            }, { throwable ->
                throwable.printStackTrace()
                if (qiscusComment.roomId == room.id) {
                    view?.onFailedSendComment(qiscusComment)
                }
            })
    }

    private fun isValidOlderComments(
        qiscusComments: List<QiscusComment>,
        lastQiscusComment: QiscusComment
    ): Boolean {
        var qiscusComments = qiscusComments
        if (qiscusComments.isEmpty()) return false

        qiscusComments = cleanFailedComments(qiscusComments)
        var containsLastValidComment = qiscusComments.isEmpty() || lastQiscusComment.id == -1L
        val size = qiscusComments.size

        if (size == 1) {
            return qiscusComments[0].commentBeforeId == 0L && lastQiscusComment.commentBeforeId == qiscusComments[0].id
        }

        for (i in 0 until size - 1) {
            if (!containsLastValidComment && qiscusComments[i].id == lastQiscusComment.commentBeforeId) {
                containsLastValidComment = true
            }

            if (qiscusComments[i].commentBeforeId != qiscusComments[i + 1].id) {
                return false
            }
        }
        return containsLastValidComment
    }

    private fun cleanFailedComments(qiscusComments: List<QiscusComment>): List<QiscusComment> {
        val comments = ArrayList<QiscusComment>()
        for (qiscusComment in qiscusComments) {
            if (qiscusComment.id != -1L) {
                comments.add(qiscusComment)
            }
        }
        return comments
    }

    private fun updateRepliedSender(comments: List<QiscusComment>) {
        for (comment in comments) {
            if (comment.type == QiscusComment.Type.REPLY) {
                val repliedComment = comment.replyTo
                if (repliedComment != null) {
                    for (qiscusRoomMember in room.member) {
                        if (repliedComment.senderEmail == qiscusRoomMember.email) {
                            repliedComment.sender = qiscusRoomMember.username
                            comment.replyTo = repliedComment
                            break
                        }
                    }
                }
            }
        }
    }

    fun downloadFile(qiscusComment: QiscusComment, uri: String, fileName: String) {
        if (qiscusComment.isDownloading) {
            return
        }

        val file = QiscusCore.getDataStore().getLocalPath(qiscusComment.id)
        if (file == null) {
            qiscusComment.isDownloading = true
            QiscusApi.getInstance()
                .downloadFile(
                    uri, fileName
                ) { percentage -> qiscusComment.progress = percentage.toInt() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .compose(bindToLifecycle())
                .doOnNext { file1 ->
                    QiscusFileUtil.notifySystem(file1)
                    qiscusComment.isDownloading = false
                    QiscusCore.getDataStore().addOrUpdateLocalPath(
                        qiscusComment.roomId, qiscusComment.id,
                        file1.absolutePath
                    )
                }
                .subscribe({ file1 ->
                    //                    view.notifyDataChanged()
                    if (qiscusComment.type == QiscusComment.Type.AUDIO) {
                        qiscusComment.playAudio()
                    } else if (qiscusComment.type == QiscusComment.Type.FILE || qiscusComment.type == QiscusComment.Type.VIDEO) {
                        view?.onFileDownloaded(
                            file1,
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.extension)
                        )
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                    qiscusComment.isDownloading = false
                    view?.showError(QiscusTextUtil.getString(R.string.qiscus_failed_download_file_mc))
                })
        } else {
//            if (qiscusComment.getType() == QiscusComment.Type.IMAGE) {
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

    private fun getCommentsFromNetwork(lastCommentId: Long): Observable<List<QiscusComment>> {
        return QiscusApi.getInstance().getPreviousMessagesById(room.id, 20, lastCommentId)
            .doOnNext { qiscusComment ->
                QiscusCore.getDataStore().addOrUpdate(qiscusComment)
                qiscusComment.roomId = room.id
            }
            .toSortedList(commentComparator)
            .subscribeOn(Schedulers.io())
    }

    @Subscribe
    fun onCommentReceivedEvent(event: QiscusCommentReceivedEvent) {
        if (event.qiscusComment.roomId == room.id) {
            onGotNewComment(event.qiscusComment)
        }
    }

    @Subscribe
    fun onRoomReceivedEvent(event: QiscusChatRoomEvent) {
        when (event.event) {
            QiscusChatRoomEvent.Event.READ -> {
                val qiscusComment = QiscusCore.getDataStore().getComment(event.commentUniqueId)
                if (qiscusComment != null) {
                    qiscusComment.state = QiscusComment.STATE_READ
                    QiscusCore.getDataStore().addOrUpdate(qiscusComment)
                    QiscusAndroidUtil.runOnUIThread {
                        view?.updateComment(qiscusComment)
                    }
                }
            }
        }
    }

    @Subscribe
    public fun onMessageDeleted(event: QiscusCommentDeletedEvent) {
        QiscusCore.getDataStore().delete(event.qiscusComment)
        QiscusAndroidUtil.runOnUIThread { view?.onCommentDeleted(event.qiscusComment) }
    }

    private fun onGotNewComment(qiscusComment: QiscusComment) {
        if (qiscusComment.senderEmail.equals(qiscusAccount.email, ignoreCase = true)) {
            QiscusAndroidUtil.runOnBackgroundThread { commentSuccess(qiscusComment) }
        } else {
            roomEventHandler.onGotComment(qiscusComment)
        }

        if (qiscusComment.roomId == room.id) {
            QiscusAndroidUtil.runOnBackgroundThread {
                if (!qiscusComment.senderEmail.equals(
                        qiscusAccount.email,
                        ignoreCase = true
                    ) && QiscusCacheManager.getInstance().lastChatActivity.first!!
                ) {
                    QiscusPusherApi.getInstance().markAsRead(room.id, qiscusComment.id)
                }
            }
            view?.onNewComment(qiscusComment)
        }
        if (qiscusComment.type == QiscusComment.Type.SYSTEM_EVENT) {
            QiscusApi.getInstance().getChatRoomInfo(room.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { chatRoom ->
                    chatRoom.options.getBoolean("is_resolved").let {
                        view?.showNewChatButton(it)
                    }
                }
        }
    }

    fun deleteComment(comment: QiscusComment) {
        view?.showLoading()
        QiscusAndroidUtil.runOnBackgroundThread { QiscusCore.getDataStore().delete(comment) }
        view?.dismissLoading()
        view?.onCommentDeleted(comment)

        QiscusApi.getInstance().deleteMessages(listOf(comment.uniqueId))
            .flatMap { Observable.from(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
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

    override fun onChatRoomMemberRemoved(member: QiscusRoomMember?) {

    }

    override fun onUserTypng(email: String?, typing: Boolean) {
        view?.onUserTyping(email, typing)
    }

    override fun onChatRoomMemberAdded(member: QiscusRoomMember?) {

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

        fun initRoomData(comments: List<QiscusComment>, qiscusChatRoom: QiscusChatRoom)

        fun onSuccessSendComment(comment: QiscusComment)

        fun onFailedSendComment(comment: QiscusComment)

        fun onLoadMoreComments(comments: List<QiscusComment>)

        fun onNewComment(comment: QiscusComment)

        fun onCommentDeleted(comment: QiscusComment)

        fun onSendingComment(comment: QiscusComment)

        fun updateLastDeliveredComment(lastDeliveredCommentId: Long)

        fun updateLastReadComment(lastReadCommentId: Long)

        fun updateComment(comment: QiscusComment)

        fun onUserTyping(email: String?, isTyping: Boolean)

        fun onFileDownloaded(file: File, mimeType: String?)
        fun showNewChatButton(it: Boolean)
        fun refreshComments()
    }
}