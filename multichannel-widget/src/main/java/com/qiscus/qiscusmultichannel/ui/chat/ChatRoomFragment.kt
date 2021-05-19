package com.qiscus.qiscusmultichannel.ui.chat

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.qiscus.jupuk.JupukBuilder
import com.qiscus.jupuk.JupukConst
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.image.SendImageConfirmationActivity
import com.qiscus.qiscusmultichannel.ui.loading.LoadingActivity
import com.qiscus.qiscusmultichannel.ui.view.QiscusChatScrollListener
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.qiscusmultichannel.util.*
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_chat_room_mc.*
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Created on : 16/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatRoomFragment : Fragment(), QiscusChatScrollListener.Listener,
    ChatRoomPresenter.ChatRoomView, QiscusPermissionsUtil.PermissionCallbacks {

    private val CAMERA_PERMISSION = arrayOf(
        "android.permission.CAMERA"
    )
    private val CAMERA_PERMISSION_28 = arrayOf(
        "android.permission.CAMERA",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE"
    )
    private val FILE_PERMISSION = arrayOf(
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE"
    )
    protected val TAKE_PICTURE_REQUEST = 3
    protected val RC_CAMERA_PERMISSION = 128
    private val RC_FILE_PERMISSION = 130
    protected val SEND_PICTURE_CONFIRMATION_REQUEST = 4
    protected val GET_TEMPLATE = 5
    private val IMAGE_GALLERY_REQUEST = 7
    private lateinit var ctx: Context
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
            val account = Const.qiscusCore()?.getQiscusAccount()!!
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
            presenter = ChatRoomPresenter(it)
            presenter.attachView(this)
            presenter.loadComments(20)
            showNewChatButton(it.extras.getBoolean("is_resolved"))
        }
        btnAttachmentOptions.setOnClickListener { showAttachmentDialog() }
        btnAttachmentCamera.setOnClickListener { selectImage() }
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

    private fun initColor() {
        context?.let {
            btnAttachmentCamera.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.qiscus_pick_image_mc
                ),
                PorterDuff.Mode.SRC_IN
            )
            btnAttachmentDoc.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.qiscus_pick_doc_mc
                ),
                PorterDuff.Mode.SRC_IN
            )
            btnSend.setColorFilter(
                ContextCompat.getColor(
                    it,
                    R.color.qiscus_send_button_mc
                ),
                PorterDuff.Mode.SRC_IN
            )
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
        Const.qiscusCore()?.cacheManager?.setLastChatActivity(true, qiscusChatRoom!!.id)
        Const.qiscusCore()?.pusherApi?.subscribeChatRoom(qiscusChatRoom)
        notifyLatestRead()
    }

    override fun onPause() {
        super.onPause()
        Const.qiscusCore()?.pusherApi?.unsubsribeChatRoom(qiscusChatRoom)
        Const.qiscusCore()?.cacheManager?.setLastChatActivity(false, qiscusChatRoom!!.id)
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
        rvMessage.setHasFixedSize(true)
        rvMessage.addOnScrollListener(QiscusChatScrollListener(layoutManager, this))
        commentsAdapter = CommentsAdapter(ctx)
        rvMessage.adapter = commentsAdapter

        commentsAdapter.setOnItemClickListener(object :
            CommentsAdapter.RecyclerViewItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                handleItemClick(commentsAdapter.data[position])
            }

            override fun onItemLongClick(view: View, position: Int) {
                toggleSelectedComment(commentsAdapter.data[position])
            }

        })
    }

    private fun handleItemClick(comment: QMessage) {
        clearSelectedComment()
        when (comment.type) {
            QMessage.Type.FILE -> {
                val obj = JSONObject(comment.payload)
                val url = obj.getString("url")
                val fileName = obj.getString("file_name")
                presenter.downloadFile(comment, url, fileName)
            }
            else -> {
            }
        }
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
    }

    private fun openCamera() {
        val permission =
            if (Build.VERSION.SDK_INT <= 28) CAMERA_PERMISSION_28 else CAMERA_PERMISSION
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
                    startActivityForResult(intent, TAKE_PICTURE_REQUEST)
                }

            }
        } else {
            requestCameraPermission()
        }
    }

    private fun pickImageUsingIntentSystem() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST)
    }

    private fun pickImageUsingJupuk() {
        JupukBuilder().setMaxCount(1)
            .enableVideoPicker(true)
            .pickPhoto(this)
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (QiscusPermissionsUtil.hasPermissions(ctx, FILE_PERMISSION)) {
                pickImageUsingJupuk()
            } else {
                requestFilePermission()
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            pickImageUsingJupuk()
        } else {
            pickImageUsingIntentSystem()
        }
    }

    private fun openFile() {
        if ((Build.VERSION.SDK_INT >= 29) || (Build.VERSION.SDK_INT <= 28 && QiscusPermissionsUtil.hasPermissions(
                ctx,
                FILE_PERMISSION
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
            if (!QiscusPermissionsUtil.hasPermissions(ctx, CAMERA_PERMISSION_28)) {
                QiscusPermissionsUtil.requestPermissions(
                    this, getString(R.string.qiscus_permission_request_title_mc),
                    RC_CAMERA_PERMISSION, CAMERA_PERMISSION_28
                )
            }
        } else {
            if (!QiscusPermissionsUtil.hasPermissions(ctx, CAMERA_PERMISSION)) {
                QiscusPermissionsUtil.requestPermissions(
                    this, getString(R.string.qiscus_permission_request_title_mc),
                    RC_CAMERA_PERMISSION, CAMERA_PERMISSION
                )
            }
        }
    }

    private fun requestFilePermission() {
        if (!QiscusPermissionsUtil.hasPermissions(ctx, FILE_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(
                this, getString(R.string.qiscus_permission_request_title_mc),
                RC_FILE_PERMISSION, FILE_PERMISSION
            )
        }
    }


    private fun bindReplyView(origin: QMessage) {
        val obj = JSONObject(origin.payload)
        originSender.text = origin.sender.name
        when (origin.type) {
            QMessage.Type.IMAGE -> {
                originImage.visibility = View.VISIBLE
                Nirmana.getInstance().get()
                    .load(origin.attachmentUri)
                    .into(originImage)
                originContent.text = obj.getString("caption")
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
        if (comment.type != QMessage.Type.SYSTEM_EVENT || comment.type != QMessage.Type.CARD) {
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
        if (it && MultichannelWidgetConfig.isSessional()) {
            newChatPanel.visibility = View.VISIBLE
            messageInputPanel.visibility = View.GONE
        } else {
            newChatPanel.visibility = View.GONE
            messageInputPanel.visibility = View.VISIBLE
        }
    }

    override fun refreshComments() {
        dismissLoading()
        MultichannelWidget.instance.openChatRoomMultichannel(clearTaskActivity = false)
        activity?.finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    private fun notifyLatestRead() {
        val qiscusComment = commentsAdapter.getLatestSentComment()
        if (qiscusComment != null && qiscusChatRoom != null) {
            Const.qiscusCore()?.pusherApi
                ?.markAsRead(qiscusChatRoom!!.id, qiscusComment.id)
        }
    }

    private fun notifyServerTyping(typing: Boolean) {
        if (isTyping != typing) {
            Const.qiscusCore()?.pusherApi?.publishTyping(qiscusChatRoom!!.id, typing)
            isTyping = typing
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageFile =
                    QiscusFileUtil.from(Uri.parse(Const.qiscusCore()?.cacheManager?.lastImagePath))
                val qiscusPhoto = QiscusPhoto(imageFile)

                val intent =
                    SendImageConfirmationActivity.generateIntent(ctx, qiscusChatRoom!!, qiscusPhoto)
                startActivityForResult(intent, SEND_PICTURE_CONFIRMATION_REQUEST)

            } catch (e: Exception) {
                ctx.showToast(getString(R.string.qiscus_chat_error_failed_read_picture_mc))
                e.printStackTrace()
            }

        } else if (requestCode == SEND_PICTURE_CONFIRMATION_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_picture_mc))
                return
            }

            val caption = data.getStringExtra(SendImageConfirmationActivity.EXTRA_CAPTIONS)
            val qiscusPhoto =
                data.getParcelableExtra<QiscusPhoto>(SendImageConfirmationActivity.EXTRA_PHOTOS)
            if (qiscusPhoto != null) {
                presenter.sendFile(qiscusPhoto.photoFile, caption)
            } else {
                showError(getString(R.string.qiscus_chat_error_failed_read_picture_mc))
            }
        } else if (requestCode == GET_TEMPLATE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val template = it.getStringExtra("template")
                sendComment(template!!)
            }
        } else if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageFile = QiscusFileUtil.from(data?.data!!)
                val qiscusPhoto = QiscusPhoto(imageFile)
                startActivityForResult(
                    SendImageConfirmationActivity.generateIntent(
                        ctx,
                        qiscusChatRoom!!, qiscusPhoto
                    ),
                    SEND_PICTURE_CONFIRMATION_REQUEST
                )
            } catch (e: Exception) {
                showError("Failed to open image file!")
            }
        } else if (requestCode == JupukConst.REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            try {
                val paths = data!!.getStringArrayListExtra(JupukConst.KEY_SELECTED_MEDIA)
                val qiscusPhoto = QiscusPhoto(File(paths!![0]))
                startActivityForResult(
                    SendImageConfirmationActivity.generateIntent(
                        ctx,
                        qiscusChatRoom!!, qiscusPhoto!!
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

    private fun showAttachmentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_attachment_mc, null)
        val dialog = BottomSheetDialog(ctx, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(dialogView)
        dialog.show()

        val btnTakePhoto = dialogView.findViewById(R.id.linTakePhoto) as LinearLayout
        val btnImageGalery = dialogView.findViewById(R.id.linImageGallery) as LinearLayout
        val btnDocument = dialogView.findViewById(R.id.linDocument) as LinearLayout
        btnTakePhoto.visibility = View.GONE
        btnImageGalery.setOnClickListener {
            dialog.dismiss()
            selectImage()
        }

        btnTakePhoto.setOnClickListener {
            dialog.dismiss()
            openCamera()
        }

        btnDocument.setOnClickListener {
            dialog.dismiss()
            openFile()
        }
    }

    private fun selectImage() {

        val builder = AlertDialog.Builder(context)
        val items = arrayOf<CharSequence>(
            "Take Photo", "Choose from Library",
            "Cancel"
        )
        builder.setItems(items, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
        })
        builder.show()
    }

    override fun showError(message: String) {
        ctx.showToast(message)
    }

    override fun openWebview(url: String) {
        WebViewHelper.launchUrl(ctx, Uri.parse(url))
    }

    override fun onDestroy() {
        super.onDestroy()
        Const.qiscusCore()?.cacheManager?.setLastChatActivity(false, 0)
        presenter.detachView()
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