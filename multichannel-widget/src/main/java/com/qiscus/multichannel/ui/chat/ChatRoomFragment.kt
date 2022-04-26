package com.qiscus.multichannel.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.qiscus.jupuk.JupukBuilder
import com.qiscus.jupuk.JupukConst
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.R
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.ui.chat.image.ImageMessageActivity
import com.qiscus.multichannel.ui.chat.image.ImageMessageActivity.Companion.CAPTION_COMMENT_IMAGE
import com.qiscus.multichannel.ui.chat.image.ImageMessageActivity.Companion.DATA
import com.qiscus.multichannel.ui.loading.LoadingActivity
import com.qiscus.multichannel.ui.view.QiscusChatScrollListener
import com.qiscus.multichannel.ui.webView.WebViewHelper
import com.qiscus.multichannel.util.*
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_chat_room_mc.*
import kotlinx.android.synthetic.main.message_layout_reply.*
import org.json.JSONObject
import rx.functions.Action1
import rx.functions.Action2
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.*


/**
 * Created on : 16/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatRoomFragment : Fragment(), QiscusChatScrollListener.Listener,
    ChatRoomPresenter.ChatRoomView, QiscusPermissionsUtil.PermissionCallbacks {

    private val qiscusMultichannelWidget: MultichanelChatWidget = QiscusMultichannelWidget.instance
    private var isChatNoEmpty: Boolean = false
    protected val SEND_PICTURE_CONFIRMATION_REQUEST = 4
    protected val GET_TEMPLATE = 5
    private lateinit var ctx: Context
    private lateinit var audioHandler: AudioHandler
    private lateinit var commentsAdapter: CommentsAdapter
    private var qiscusChatRoom: QChatRoom? = null
    private lateinit var presenter: ChatRoomPresenter
    private var commentSelectedListener: CommentSelectedListener? = null
    private var userTypingListener: OnUserTypingListener? = null
    private var selectedComment: QMessage? = null
    private lateinit var rvMessage: RecyclerView
    private var isTyping = false

    companion object {
        const val CHATROOM_KEY = "chatroom_key"

        fun newInstance(qiscusChatRoom: QChatRoom): ChatRoomFragment {
            val chatRoomFragment = ChatRoomFragment()
            val bundle = Bundle()
            bundle.putParcelable(CHATROOM_KEY, qiscusChatRoom)
            chatRoomFragment.arguments = bundle
            return chatRoomFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_room_mc, container, false)
        rvMessage = view.findViewById(R.id.rvMessage) as RecyclerView
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initColor()
        initRecyclerMessage()

        arguments?.let {
            qiscusChatRoom = it.getParcelable(CHATROOM_KEY)
        }

        if (qiscusChatRoom == null) {
            throw RuntimeException("please provide qiscus chat room")
        }

        btnSend.setOnClickListener { sendingComment() }
        btn_new_room.setOnClickListener {
            val account = MultichannelConst.qiscusCore()?.qiscusAccount!!
            QiscusChatLocal.setRoomId(0)
            LoadingActivity.generateIntent(
                ctx,
                account.name,
                QiscusChatLocal.getUserId(),
                QiscusChatLocal.getAvatar(),
                QiscusChatLocal.getExtras(),
                QiscusChatLocal.getUserProps()
            )
            activity?.finish()
        }
        btnCancelReply.setOnClickListener { rootViewSender.visibility = View.GONE }
        qiscusChatRoom?.let {
            presenter =
                ChatRoomPresenter(it, qiscusMultichannelWidget.getComponent().qiscusChatRepository)
            presenter.attachView(this)
            presenter.loadComments(20)
            showNewChatButton(it.extras.getBoolean("is_resolved"))
        }

        btnAttachmentCamera.setOnClickListener { showImageDialog() }
        btnAttachmentDoc.setOnClickListener { openFile() }

        etMessage.afterTextChangedDelayed({
            notifyServerTyping(true)
        }, {
            notifyServerTyping(false)
        })

        if (Build.VERSION.SDK_INT <= 28) {
            requestFilePermission()
        }
    }

    private fun initColor() = with(qiscusMultichannelWidget.getColor()) {
        context?.let {
            etMessage.background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(it, R.color.qiscus_white_mc))
                shape = GradientDrawable.RECTANGLE
                cornerRadius = ResourceManager.getDimen((it as ChatRoomActivity).displayMetrics, 8)
                setStroke(
                    ResourceManager.getDimen(it.displayMetrics, 1).toInt(),
                    getFieldChatBorderColor()
                )
            }
            btn_new_room.background = ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(it, R.drawable.qiscus_button_bg),
                getNavigationColor()
            )

            messageInputPanel.setBackgroundColor(getSendContainerBackgroundColor())
            btn_new_room.setTextColor(getNavigationTitleColor())
            btnAttachmentCamera.setColorFilter(getSendContainerColor())
            btnAttachmentDoc.setColorFilter(getSendContainerColor())
            btnSend.setColorFilter(getSendContainerColor())
            rootViewSender.setBackgroundColor(getBaseColor())
            originSender.setTextColor(getNavigationColor())
            btnCancelReply.setColorFilter(getSendContainerColor())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                progressBar.indeterminateDrawable.colorFilter =
                    BlendModeColorFilter(
                        getNavigationColor(), BlendMode.SRC_IN
                    )
            } else {
                progressBar.indeterminateDrawable.setColorFilter(
                    getNavigationColor(), PorterDuff.Mode.SRC_IN);
            }

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is ChatRoomActivity) {
            ctx = context
        }

        if (activity is CommentSelectedListener) {
            commentSelectedListener = activity as CommentSelectedListener
            userTypingListener = activity as OnUserTypingListener
        }
    }

    override fun onResume() {
        super.onResume()
        MultichannelConst.qiscusCore()?.cacheManager?.setLastChatActivity(true, qiscusChatRoom!!.id)
        MultichannelConst.qiscusCore()?.pusherApi?.subscribeChatRoom(qiscusChatRoom)
        notifyLatestRead()
    }

    override fun onPause() {
        super.onPause()
        MultichannelConst.qiscusCore()?.pusherApi?.unsubsribeChatRoom(qiscusChatRoom)
        MultichannelConst.qiscusCore()?.cacheManager?.setLastChatActivity(
            false,
            qiscusChatRoom!!.id
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notifyLatestRead()
        presenter.detachView()
    }

    private fun initRecyclerMessage() {
        val layoutManager = LinearLayoutManager(ctx)
        layoutManager.reverseLayout = true
        rvMessage.layoutManager = layoutManager
        rvMessage.itemAnimator = null
        rvMessage.setHasFixedSize(true)
        rvMessage.addOnScrollListener(QiscusChatScrollListener(layoutManager, this))

        audioHandler = AudioHandler(ctx)
        commentsAdapter = CommentsAdapter(
            ctx,
            qiscusMultichannelWidget.getConfig(),
            qiscusMultichannelWidget.getColor(),
            audioHandler
        )
        rvMessage.adapter = commentsAdapter

        commentsAdapter.setOnItemClickListener(object : CommentsAdapter.ItemViewListener {

            override fun onSendComment(comment: QMessage) {
                presenter.sendComment(comment)
            }

            override fun onItemClick(view: View, position: Int) {
                handleItemClick(commentsAdapter.data[position])
            }

            override fun onItemLongClick(view: View, position: Int) {
                toggleSelectedComment(commentsAdapter.data[position])
            }

            override fun onItemReplyClick(view: View, comment: QMessage) {
                if (commentsAdapter.findPosition(comment) > -1) {
                    onLoadReply(comment)
                } else {
                    showLoading()
                    presenter.loadOlderCommentByReply(commentsAdapter.getLatestComment(), comment)
                }

            }

            override fun stopAnotherAudio(comment: QMessage) {
                commentsAdapter.stopAnotherAudio(comment.id)
            }
        })
    }

    override fun onLoadReply(comment: QMessage) {
        commentsAdapter.goToComment(comment.id,
            Action2 { _, position ->
                rvMessage.scrollToPosition(position)
                rvMessage.postDelayed( {
                    commentsAdapter.clearSelected(position)
                }, 2000)
            })
    }

    private fun setChatNoEmpty(isNoEmpty: Boolean) {
        if (isChatNoEmpty) return

        if (isNoEmpty) {
            containerBackground.setBackgroundColor(
                qiscusMultichannelWidget.getColor().getBaseColor()
            )
            tvEmpty.visibility = View.GONE
        } else if (context != null) {
            containerBackground.setBackgroundColor(
                qiscusMultichannelWidget.getColor().getEmptyBacgroundColor()
            )
            tvEmpty.setTextColor(qiscusMultichannelWidget.getColor().getEmptyTextColor())
            tvEmpty.text = HtmlCompat.fromHtml(
                context!!.getString(R.string.qiscus_empyText),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            tvEmpty.visibility = View.VISIBLE
        }

        this.isChatNoEmpty = isNoEmpty
    }

    private fun handleItemClick(comment: QMessage) {
        clearSelectedComment()
        when (comment.type) {
            QMessage.Type.AUDIO -> {
                if (MultichannelConst.qiscusCore()?.dataStore?.getLocalPath(comment.id) == null)
                    downloadFile(comment)
            }
            QMessage.Type.FILE -> {
                downloadFile(comment)
            }
            else -> {
            }
        }
    }

    private fun downloadFile(comment: QMessage) {
        val obj = JSONObject(comment.payload)
        val url = obj.getString("url")
        val fileName = obj.getString("file_name")
        presenter.downloadFile(comment, url, fileName)
    }

    private fun sendingComment() {
        if (!TextUtils.isEmpty(etMessage.text)) {
            if (rootViewSender.isVisible) {
                selectedComment?.let {
                    presenter.sendReplyComment(etMessage.text.toString(), it)
                }
                rootViewSender.visibility = View.GONE
                selectedComment = null
            } else {
                sendComment(etMessage.text.toString())
            }

            etMessage.setText("")
        }
        setChatNoEmpty(true)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        val permission =
            if (Build.VERSION.SDK_INT <= 28) MultichannelConst.CAMERA_PERMISSION_28 else MultichannelConst.CAMERA_PERMISSION
        if (QiscusPermissionsUtil.hasPermissions(ctx, permission)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(ctx.packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = QiscusImageUtil.createImageFile()
                } catch (ex: IOException) {
                    if (ex.message != null && !ex.message.isNullOrEmpty()) {
                        ctx.showToast(ex.message!!)
                    } else {
                        ctx.showToast(getString(R.string.qiscus_chat_error_failed_write_mc))
                    }

                }

                if (photoFile != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        intent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(
                                ctx,
                                getAuthority(),
                                photoFile
                            )
                        )
                    }
                    startActivityForResult(intent, MultichannelConst.TAKE_PICTURE_REQUEST)
                }

            }
        } else {
            requestCameraPermission()
        }
    }

    /**
     * open images using default gallery for android 11 or higher
     * */
    private fun pickImageUsingIntentSystem() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
        }

        startActivityForResult(intent, MultichannelConst.IMAGE_GALLERY_REQUEST)
    }

    /**
     * open images using jupuk gallery for android under 11 version
     * */
    private fun pickImageUsingJupuk() {
        JupukBuilder().setMaxCount(30)
                .enableVideoPicker(true)
                .pickPhoto(this)
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (QiscusPermissionsUtil.hasPermissions(ctx, MultichannelConst.FILE_PERMISSION)) {
                pickImageUsingJupuk()
            } else {
                requestFilePermission()
            }
        } else {
            pickImageUsingIntentSystem()
        }
    }

    private fun openFile() {
        if ((Build.VERSION.SDK_INT >= 29) || (Build.VERSION.SDK_INT <= 28 && QiscusPermissionsUtil.hasPermissions(
                ctx,
                MultichannelConst.FILE_PERMISSION
            ))
        ) {
            JupukBuilder().setMaxCount(1)
                .pickDoc(this)
        } else {
            requestFilePermission()
        }
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT <= 28) {
            if (!QiscusPermissionsUtil.hasPermissions(
                    ctx,
                    MultichannelConst.CAMERA_PERMISSION_28
                )
            ) {
                QiscusPermissionsUtil.requestPermissions(
                    this, getString(R.string.qiscus_permission_request_title_mc),
                    MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28
                )
            }
        } else {
            if (!QiscusPermissionsUtil.hasPermissions(ctx, MultichannelConst.CAMERA_PERMISSION)) {
                QiscusPermissionsUtil.requestPermissions(
                    this, getString(R.string.qiscus_permission_request_title_mc),
                    MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION
                )
            }
        }
    }

    private fun requestFilePermission() {
        if (!QiscusPermissionsUtil.hasPermissions(ctx, MultichannelConst.FILE_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(
                this, getString(R.string.qiscus_permission_request_title_mc),
                MultichannelConst.RC_FILE_PERMISSION, MultichannelConst.FILE_PERMISSION
            )
        }
    }

    private fun bindReplyView(origin: QMessage) {
        val obj = JSONObject(origin.payload)
        val me = MultichannelConst.qiscusCore()?.qiscusAccount?.id

        originSender.text =
            if (origin.isMyComment(me)) ctx.getText(R.string.qiscus_you_mc)
            else origin.sender.name

        when (origin.type) {
            QMessage.Type.IMAGE,  QMessage.Type.VIDEO -> {
                originImage.visibility = View.VISIBLE

                Nirmana.getInstance().get()
                    .load(origin.attachmentUri)
                    .into(originImage)

                val caption: String? = obj.getString("caption")
                originContent.text = MultichannelQMessageUtils.getFileName(
                    if (caption != null && caption == "") origin.text
                    else caption
                )
            }
            QMessage.Type.FILE -> {
                originContent.text = origin.attachmentName
                originImage.visibility = View.GONE
            }
            else -> {
                originImage.visibility = View.GONE
                originContent.text = origin.text
            }
        }
    }

    fun toggleSelectedComment(comment: QMessage) {
        if (comment.type != QMessage.Type.SYSTEM_EVENT
            && comment.type != QMessage.Type.BUTTONS
            && comment.type != QMessage.Type.CAROUSEL
            && comment.type != QMessage.Type.CARD
        ) {
            comment.isSelected = true
            commentsAdapter.addOrUpdate(comment)
            commentsAdapter.setSelectedComment(comment)
            commentSelectedListener?.onCommentSelected(comment)
        }
    }

    fun clearSelectedComment() {
        commentSelectedListener?.onClearSelectedComment(true)
        commentsAdapter.clearSelected()
    }

    fun sendComment(message: String?) {
        message?.let {
            clearSelectedComment()
            presenter.sendComment(message)
        }
    }

    fun deleteComment() {
        clearSelectedComment()
        commentsAdapter.getSelectedComment()?.let {
            showDialogDeleteComment(it)
        }
    }

    fun replyComment() {
        clearSelectedComment()
        selectedComment = commentsAdapter.getSelectedComment()
        rootViewSender.visibility = if (selectedComment == null) View.GONE else View.VISIBLE
        selectedComment?.let { bindReplyView(it) }
    }

    fun copyComment() {
        clearSelectedComment()
        commentsAdapter.getSelectedComment()?.let {
            val obj = JSONObject(it.payload)
            val textCopied = when (it.type) {
                QMessage.Type.FILE -> it.attachmentName
                QMessage.Type.IMAGE -> obj.getString("caption")
                QMessage.Type.CARD -> {
                    val title = obj.getString("title")
                    val description = obj.getString("description")
                    title + "\n" + description
                }
                else -> it.text
            }
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                getString(R.string.qiscus_chat_activity_label_clipboard_mc),
                textCopied
            )
            clipboard.setPrimaryClip(clip)

            ctx.showToast(getString(R.string.qiscus_copied_message_mc))
        }

    }

    private fun showDialogDeleteComment(qiscusComment: QMessage) {
        val alertDialogBuilder = AlertDialog.Builder(ctx)
        alertDialogBuilder.setTitle(R.string.qiscus_delete_message_title)

        alertDialogBuilder
            .setMessage(R.string.qiscus_delete_message)
            .setCancelable(false)
            .setPositiveButton(R.string.qiscus_delete) { dialog, p ->
                presenter.deleteComment(qiscusComment)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.qiscus_cancel) { dialog, p ->
                dialog.dismiss()
            }

        alertDialogBuilder.create().show()
    }

    override fun onTopOffListMessage() {
        loadMoreComments()
    }

    override fun onMiddleOffListMessage() {

    }

    override fun onBottomOffListMessage() {

    }

    private fun loadMoreComments() {
        if (progressBar.visibility == View.GONE && commentsAdapter.itemCount > 0) {
            val comment = commentsAdapter.data.get(commentsAdapter.itemCount - 1)
            if (comment.id == -1L || comment.previousMessageId > 0) {
                presenter.loadOlderCommentThan(comment)
            }
        }
    }

    override fun initRoomData(comments: List<QMessage>, qiscusChatRoom: QChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom
        commentsAdapter.addOrUpdate(comments)
        setChatNoEmpty(commentsAdapter.itemCount > 0)
    }

    override fun onLoadMoreComments(comments: List<QMessage>) {
        commentsAdapter.addOrUpdate(comments)
    }

    override fun onSuccessSendComment(comment: QMessage) {
        commentsAdapter.addOrUpdate(comment)
    }

    override fun onFailedSendComment(comment: QMessage) {
        commentsAdapter.addOrUpdate(comment)
    }

    override fun onNewComment(comment: QMessage) {
        commentsAdapter.addOrUpdate(comment)
        if ((rvMessage.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() <= 2) {
            rvMessage.smoothScrollToPosition(0)
        }
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun dismissLoading() {
        progressBar.visibility = View.GONE
    }

    override fun onCommentDeleted(comment: QMessage) {
        commentsAdapter.remove(comment)
    }

    override fun onSendingComment(comment: QMessage) {
        commentsAdapter.addOrUpdate(comment)
        rvMessage.smoothScrollToPosition(0)
    }

    override fun updateLastDeliveredComment(lastDeliveredCommentId: Long) {
        commentsAdapter.updateLastDeliveredComment(lastDeliveredCommentId)
    }

    override fun updateLastReadComment(lastReadCommentId: Long) {
        commentsAdapter.updateLastReadComment(lastReadCommentId)
    }

    override fun updateComment(comment: QMessage) {
        commentsAdapter.addOrUpdate(comment)
    }

    override fun onUserTyping(email: String?, isTyping: Boolean) {
        userTypingListener?.onUserTyping(email, isTyping)
    }

    override fun onFileDownloaded(file: File, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent.setDataAndType(Uri.fromFile(file), mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            intent.setDataAndType(
                FileProvider.getUriForFile(
                    ctx,
                    getAuthority(),
                    file
                ), mimeType
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showError(getString(R.string.qiscus_chat_error_failed_open_file))
        }

    }

    override fun showNewChatButton(it: Boolean) {
        if (it && qiscusMultichannelWidget.getConfig().isSessional()) {
            newChatPanel.visibility = View.VISIBLE
            messageInputPanel.visibility = View.GONE
        } else {
            newChatPanel.visibility = View.GONE
            messageInputPanel.visibility = View.VISIBLE
        }
    }

    override fun refreshComments() {
        dismissLoading()
        qiscusMultichannelWidget.openChatRoom(ctx, false)
        activity?.finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    private fun notifyLatestRead() {
        val qiscusComment = commentsAdapter.getLatestSentComment()
        if (qiscusComment != null && qiscusChatRoom != null) {
            MultichannelConst.qiscusCore()?.pusherApi
                ?.markAsRead(qiscusChatRoom!!.id, qiscusComment.id)
        }
    }

    private fun notifyServerTyping(typing: Boolean) {
        if (isTyping != typing) {
            MultichannelConst.qiscusCore()?.pusherApi?.publishTyping(qiscusChatRoom!!.id, typing)
            isTyping = typing
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == MultichannelConst.TAKE_PICTURE_REQUEST) {
            try {
                val imageFile =
                    QiscusFileUtil.from(Uri.parse(MultichannelConst.qiscusCore()?.cacheManager?.lastImagePath))

                val list: Array<String?> = arrayOf(imageFile.absolutePath)
                val intent =
                    ImageMessageActivity.generateIntent(ctx, qiscusChatRoom!!, list)
                startActivityForResult(intent, SEND_PICTURE_CONFIRMATION_REQUEST)

            } catch (e: Exception) {
                ctx.showToast(getString(R.string.qiscus_chat_error_failed_read_picture_mc))
                e.printStackTrace()
            }

        } else if (requestCode == SEND_PICTURE_CONFIRMATION_REQUEST) {
            data?.let {
                val paths = it.getStringArrayListExtra(DATA)
                val captions = it.getStringArrayListExtra(CAPTION_COMMENT_IMAGE)
                for (i in paths!!.indices) {
                    presenter.sendFile(File(Uri.parse(paths[i]).path), captions!![i])
                }
            }

            setChatNoEmpty(true)
        } else if (requestCode == GET_TEMPLATE) {
            data?.let {
                val template = it.getStringExtra("template")
                sendComment(template!!)
            }
            setChatNoEmpty(true)
        } else if (requestCode == MultichannelConst.IMAGE_GALLERY_REQUEST) {
            try {
                data?.let {
                    val list: Array<String?>
                    if (it.clipData != null) {
                        list = arrayOfNulls(it.clipData!!.itemCount)

                        for (i in 0 until it.clipData!!.itemCount) {
                            list[i] =
                                QiscusFileUtil.from(it.clipData!!.getItemAt(i).uri).absolutePath
                        }

                    } else {
                        list = arrayOf(QiscusFileUtil.from(it.data!!).absolutePath)
                    }

                    startActivityForResult(
                        ImageMessageActivity.generateIntent(
                            ctx,
                            qiscusChatRoom!!, list
                        ),
                        SEND_PICTURE_CONFIRMATION_REQUEST
                    )
                }

//                val imageFile = QiscusFileUtil.from(data?.data!!)
//                val qiscusPhoto = QiscusPhoto(imageFile)

            } catch (e: Exception) {
                showError("Failed to open image file!")
            }
        } else if (requestCode == JupukConst.REQUEST_CODE_PHOTO) {
            try {
                // will be deleted
                /*val paths = data!!.getStringArrayListExtra(JupukConst.KEY_SELECTED_MEDIA)
                val qiscusPhoto = QiscusPhoto(File(paths!![0]))*/

                val dataListExtras: ArrayList<String> =
                    data?.getStringArrayListExtra(JupukConst.KEY_SELECTED_MEDIA)!!
                val dataImages = arrayOfNulls<String>(
                    dataListExtras.size
                )
                for (i in dataListExtras.indices) {
                    dataImages[i] = dataListExtras[i]
                }

                startActivityForResult(
                    ImageMessageActivity.generateIntent(
                        ctx,
                        qiscusChatRoom!!, dataImages
                    ),
                    SEND_PICTURE_CONFIRMATION_REQUEST
                )
            } catch (e: Exception) {
                showError("Failed to open image file!")
            }
        } else if (requestCode == JupukConst.REQUEST_CODE_DOC) {
            val paths = data?.getStringArrayListExtra(JupukConst.KEY_SELECTED_DOCS)
            if (paths != null && paths.isNotEmpty()) {
                presenter.sendFile(File(paths[0]))
            }
            setChatNoEmpty(true)
        }

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        QiscusPermissionsUtil.checkDeniedPermissionsNeverAskAgain(
            this, getString(R.string.qiscus_permission_message_mc),
            R.string.qiscus_grant_mc, R.string.qiscus_denny_mc, perms
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        QiscusPermissionsUtil.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    private fun showImageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_attachment_mc, null)
        val dialog = BottomSheetDialog(ctx, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(dialogView)

        val behaviorField: Field = dialog.javaClass.getDeclaredField("behavior")
        behaviorField.isAccessible = true
        val behavior = behaviorField.get(dialog) as BottomSheetBehavior<*>

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        dialogView.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(ctx, R.drawable.bottom_sheet_style),
            qiscusMultichannelWidget.getColor().getSendContainerBackgroundColor()
        )

        dialogView.findViewById<LinearLayout>(R.id.linTakePhoto).also {
            it.findViewById<ImageView>(R.id.imgCamera).setColorFilter(
                qiscusMultichannelWidget.getColor().getNavigationColor()
            )
            it.findViewById<TextView>(R.id.textCamera).setTextColor(
                qiscusMultichannelWidget.getColor().getNavigationColor()
            )
            it.setOnClickListener {
                dialog.dismiss()
                openCamera()
            }
        }
        dialogView.findViewById<LinearLayout>(R.id.linImageGallery).also {
            it.findViewById<ImageView>(R.id.imgGallery).setColorFilter(
                qiscusMultichannelWidget.getColor().getNavigationColor()
            )
            it.findViewById<TextView>(R.id.textGallery).setTextColor(
                qiscusMultichannelWidget.getColor().getNavigationColor()
            )
            it.setOnClickListener {
                dialog.dismiss()
                openGallery()
            }
        }
        dialog.show()
    }

    override fun showError(message: String) {
        ctx.showToast(message)
    }

    override fun openWebview(url: String) {
        WebViewHelper.launchUrl(ctx, Uri.parse(url))
    }

    override fun onSessionalChange(isSessional: Boolean) {
        qiscusMultichannelWidget.getConfig().setSessional(isSessional)
    }

    override fun onDestroy() {
        audioHandler.destroyMedia()
        audioHandler.detach()
        super.onDestroy()
        MultichannelConst.qiscusCore()?.cacheManager?.setLastChatActivity(false, 0)
        presenter.detachView()
        rvMessage.adapter = null
        clearFindViewByIdCache()
    }


    interface CommentSelectedListener {
        fun onCommentSelected(selectedComment: QMessage)
        fun onClearSelectedComment(status: Boolean)
    }

    interface OnUserTypingListener {
        fun onUserTyping(email: String?, isTyping: Boolean)
    }
}