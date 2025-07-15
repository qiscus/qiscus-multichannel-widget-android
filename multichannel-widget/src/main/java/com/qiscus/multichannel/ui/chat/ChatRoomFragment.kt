package com.qiscus.multichannel.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.qiscus.jupuk.JupukBuilder
import com.qiscus.jupuk.JupukConst
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.R
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.local.QiscusSessionLocal
import com.qiscus.multichannel.databinding.FragmentChatRoomMcBinding
import com.qiscus.multichannel.ui.chat.ChatRoomActivity.Companion.AUTO_MESSAGE_KEY
import com.qiscus.multichannel.ui.chat.ChatRoomActivity.Companion.CHATROOM_KEY
import com.qiscus.multichannel.ui.chat.ChatRoomActivity.Companion.MESSAGE_KEY
import com.qiscus.multichannel.ui.chat.image.ImageMessageActivity
import com.qiscus.multichannel.ui.chat.image.ImageMessageActivity.Companion.CAPTION_COMMENT_IMAGE
import com.qiscus.multichannel.ui.chat.image.ImageMessageActivity.Companion.DATA
import com.qiscus.multichannel.ui.loading.LoadingActivity
import com.qiscus.multichannel.ui.view.QiscusChatScrollListener
import com.qiscus.multichannel.ui.webview.WebViewHelper
import com.qiscus.multichannel.util.*
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import org.json.JSONObject
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

    private lateinit var binding: FragmentChatRoomMcBinding
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
    private var isTyping = false
    private val maxMediaPickerCount = 30
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxMediaPickerCount)
    ) { uris ->
        if (uris == null) showError("Failed to open image file!")
        if (uris.isEmpty()) return@registerForActivityResult
        uris?.let {
            val list: Array<String?> = arrayOfNulls(it.size)
            for (i in 0 until it.size) {
                list[i] = QiscusFileUtil.from(it[i]).absolutePath
            }
            openImagePreview(list)
        }
    }

    companion object {

        fun newInstance(
            qiscusChatRoom: QChatRoom, autoQiscusMessage: QMessage?,
            isAutoSendMessage: Boolean
        ): ChatRoomFragment {
            val chatRoomFragment = ChatRoomFragment()
            val bundle = Bundle()
            bundle.putParcelable(CHATROOM_KEY, qiscusChatRoom)
            bundle.putParcelable(MESSAGE_KEY, autoQiscusMessage)
            bundle.putBoolean(AUTO_MESSAGE_KEY, isAutoSendMessage)
            chatRoomFragment.arguments = bundle
            return chatRoomFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatRoomMcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initColor()
        initRecyclerMessage()

        var autoQiscusMessage: QMessage? = null
        var isAutoSendMessage = false
        arguments?.let {
            qiscusChatRoom = it.getParcelable(CHATROOM_KEY)
            autoQiscusMessage = it.getParcelable(MESSAGE_KEY)
            isAutoSendMessage = it.getBoolean(AUTO_MESSAGE_KEY)
        }

        if (qiscusChatRoom == null) {
            throw RuntimeException("please provide qiscus chat room")
        }

        binding.btnSend.setOnClickListener { sendingComment() }
        binding.btnNewRoom.setOnClickListener {
            val account = MultichannelConst.qiscusCore()?.qiscusAccount!!
            QiscusChatLocal.setRoomId(0)
            LoadingActivity.generateIntent(
                ctx,
                account.name,
                QiscusChatLocal.getUserId(),
                QiscusChatLocal.getAvatar(),
                QiscusSessionLocal.getSessionId(QiscusChatLocal.getUserId()),
                QiscusChatLocal.getExtras(),
                QiscusChatLocal.getUserProps()
            )
            activity?.finish()
        }
        binding.rootViewSender.btnCancelReply.setOnClickListener {
            binding.rootViewSender.root.visibility = View.GONE
        }
        qiscusChatRoom?.let {
            presenter =
                ChatRoomPresenter(
                    it,
                    qiscusMultichannelWidget.getComponent().getQiscusChatRepository()
                )
            presenter.attachView(this)
            presenter.loadComments(20)
            showNewChatButton(it.extras.getBoolean("is_resolved"))
        }

        binding.btnAttachmentCamera.setOnClickListener { showImageDialog() }
        binding.btnAttachmentDoc.setOnClickListener { openFile() }

        binding.etMessage.afterTextChangedDelayed({
            notifyServerTyping(true)
        }, {
            notifyServerTyping(false)
        })

        if (Build.VERSION.SDK_INT <= 28) {
            requestFilePermission()
        }

        autoQiscusMessage?.let {
            if (isAutoSendMessage) {
                presenter.sendComment(it)
            } else {
                binding.etMessage.setText(it.text)
            }
        }
    }

    private fun initColor() = with(qiscusMultichannelWidget.getColor()) {
        context?.let {
            binding.etMessage.background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(it, R.color.qiscus_white_mc))
                shape = GradientDrawable.RECTANGLE
                cornerRadius = ResourceManager.getDimen((it as ChatRoomActivity).displayMetrics, 8)
                setStroke(
                    ResourceManager.getDimen(it.displayMetrics, 1).toInt(),
                    getFieldChatBorderColor()
                )
            }
            binding.btnNewRoom.background = ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(it, R.drawable.qiscus_button_bg),
                getNavigationColor()
            )

            binding.messageInputPanel.setBackgroundColor(getSendContainerBackgroundColor())
            binding.btnNewRoom.setTextColor(getNavigationTitleColor())
            binding.btnAttachmentCamera.setColorFilter(getSendContainerColor())
            binding.btnAttachmentDoc.setColorFilter(getSendContainerColor())
            binding.btnSend.setColorFilter(getSendContainerColor())
            binding.rootViewSender.root.setBackgroundColor(getBaseColor())
            binding.rootViewSender.originSender.setTextColor(getNavigationColor())
            binding.rootViewSender.btnCancelReply.setColorFilter(getSendContainerColor())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.progressBar.indeterminateDrawable.colorFilter =
                    BlendModeColorFilter(
                        getNavigationColor(), BlendMode.SRC_IN
                    )
            } else {
                binding.progressBar.indeterminateDrawable.setColorFilter(
                    getNavigationColor(), PorterDuff.Mode.SRC_IN
                )
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
        binding.rvMessage.layoutManager = layoutManager
        binding.rvMessage.itemAnimator = null
        binding.rvMessage.setHasFixedSize(true)
        binding.rvMessage.addOnScrollListener(QiscusChatScrollListener(layoutManager, this))

        audioHandler = AudioHandler(ctx)
        commentsAdapter = CommentsAdapter(
            ctx,
            qiscusMultichannelWidget.getConfig(),
            qiscusMultichannelWidget.getColor(),
            audioHandler
        )
        binding.rvMessage.adapter = commentsAdapter

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
            { _, position ->
                binding.rvMessage.scrollToPosition(position)
                binding.rvMessage.postDelayed({
                    commentsAdapter.clearSelected(position)
                }, 2000)
            })
    }

    private fun setChatNoEmpty(isNoEmpty: Boolean) {
        if (isChatNoEmpty) return

        if (isNoEmpty) {
            binding.containerBackground.setBackgroundColor(
                qiscusMultichannelWidget.getColor().getBaseColor()
            )
            binding.tvEmpty.visibility = View.GONE
        } else if (context != null) {
            binding.containerBackground.setBackgroundColor(
                qiscusMultichannelWidget.getColor().getEmptyBacgroundColor()
            )
            binding.tvEmpty.setTextColor(qiscusMultichannelWidget.getColor().getEmptyTextColor())
            binding.tvEmpty.text = HtmlCompat.fromHtml(
                context!!.getString(R.string.qiscus_empyText),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding.tvEmpty.visibility = View.VISIBLE
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
            else -> { /*ignored*/
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
        if (!TextUtils.isEmpty(binding.etMessage.text)) {
            if (binding.rootViewSender.root.isVisible) {
                selectedComment?.let {
                    presenter.sendReplyComment(binding.etMessage.text.toString(), it)
                }
                binding.rootViewSender.root.visibility = View.GONE
                selectedComment = null
            } else {
                sendComment(binding.etMessage.text.toString())
            }

            binding.etMessage.setText("")
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
        pickMedia.launch(
            PickVisualMediaRequest(PickVisualMedia.ImageAndVideo)
        )
    }

    /**
     * open images using jupuk gallery for android under 11 version
     * */
    private fun pickImageUsingJupuk() {
        JupukBuilder().setMaxCount(maxMediaPickerCount)
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

        binding.rootViewSender.originSender.text =
            if (origin.isMyComment(me)) ctx.getText(R.string.qiscus_you_mc)
            else origin.sender.name

        when (origin.type) {
            QMessage.Type.IMAGE, QMessage.Type.VIDEO -> {
                binding.rootViewSender.originImage.visibility = View.VISIBLE

                Nirmana.getInstance().get()
                    .load(origin.attachmentUri)
                    .into(binding.rootViewSender.originImage)

                val caption: String? = obj.getString("caption")
                binding.rootViewSender.originContent.text = MultichannelQMessageUtils.getFileName(
                    if (caption != null && caption == "") origin.text
                    else caption
                )
            }
            QMessage.Type.FILE -> {
                binding.rootViewSender.originContent.text = origin.attachmentName
                binding.rootViewSender.originImage.visibility = View.GONE
            }
            else -> {
                binding.rootViewSender.originImage.visibility = View.GONE
                binding.rootViewSender.originContent.text = origin.text
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
        binding.rootViewSender.root.visibility =
            if (selectedComment == null) View.GONE else View.VISIBLE
        selectedComment?.let { bindReplyView(it) }
    }

    fun copyComment() {
        clearSelectedComment()
        commentsAdapter.getSelectedComment()?.let {
            val payload = if (it.payload.isNullOrEmpty()) "{}" else it.payload!!
            val obj = JSONObject(payload)

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

    override fun onMiddleOffListMessage() { /*ignored*/
    }

    override fun onBottomOffListMessage() {/*ignored*/
    }

    private fun loadMoreComments() {
        if (binding.progressBar.visibility == View.GONE && commentsAdapter.itemCount > 0) {
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
        if ((binding.rvMessage.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() <= 2) {
            binding.rvMessage.smoothScrollToPosition(0)
        }
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun dismissLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onCommentDeleted(comment: QMessage) {
        commentsAdapter.remove(comment)
    }

    override fun onSendingComment(comment: QMessage) {
        commentsAdapter.addOrUpdate(comment)
        binding.rvMessage.scrollToPosition(0)
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

    @Throws(java.lang.IllegalArgumentException::class)
    override fun onFileDownloaded(file: File, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent.setDataAndType(Uri.fromFile(file), mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else {
            intent.setDataAndType(
                FileProvider.getUriForFile(ctx, getAuthority(), file), mimeType
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
        val isSessional = qiscusMultichannelWidget.getConfig().isSessional()
        if (it && isSessional) {
            binding.newChatPanel.visibility = View.VISIBLE
            binding.messageInputPanel.visibility = View.GONE
        } else {
            binding.newChatPanel.visibility = View.GONE
            binding.messageInputPanel.visibility = View.VISIBLE
        }
    }

    override fun refreshComments() {
        dismissLoading()
        qiscusMultichannelWidget.openChatRoom(ctx, false)
        activity?.finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {/*ignored*/
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
                    sendFile(
                        File(Uri.parse(paths[i]).path.toString()),
                        captions!![i]
                    )
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
            if (data == null || (data.clipData != null && data.clipData!!.itemCount == 0)) return

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

                    openImagePreview(list)
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
                sendFile(File(paths[0]), "")
            }
            setChatNoEmpty(true)
        }

    }

    private fun openImagePreview(list: Array<String?>) {
        startActivityForResult(
            ImageMessageActivity.generateIntent(ctx, qiscusChatRoom!!, list),
            SEND_PICTURE_CONFIRMATION_REQUEST
        )
    }

    private fun sendFile(rawFile: File, caption: String) {
        val file = QiscusFileUtil.saveFile(rawFile)
        presenter.sendFile(file, caption, JSONObject())
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        QiscusPermissionsUtil.checkDeniedPermissionsNeverAskAgain(
            this, getString(R.string.qiscus_permission_message_mc),
            R.string.qiscus_grant_mc, R.string.qiscus_denny_mc, perms
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        QiscusPermissionsUtil.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    @SuppressLint("InflateParams")
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

            override fun onSlide(bottomSheet: View, slideOffset: Float) {/*ignored*/
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
        binding.rvMessage.adapter = null
//        clearFindViewByIdCache()
    }


    interface CommentSelectedListener {
        fun onCommentSelected(selectedComment: QMessage)
        fun onClearSelectedComment(status: Boolean)
    }

    interface OnUserTypingListener {
        fun onUserTyping(email: String?, isTyping: Boolean)
    }
}