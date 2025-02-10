package com.qiscus.multichannel.ui.chat.image

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.qiscus.jupuk.JupukBuilder
import com.qiscus.jupuk.JupukConst
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.R
import com.qiscus.multichannel.data.model.ImageToSend
import com.qiscus.multichannel.databinding.ActivityImageMessageBinding
import com.qiscus.multichannel.ui.chat.image.ImagePagerAdapter
import com.qiscus.multichannel.util.*
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import rx.Subscription
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.util.*
import kotlin.math.roundToInt

/**
 * Created on : 10/08/21
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
class ImageMessageActivity : AppCompatActivity(),
    ImagePagerAdapter.ImagePagerListener, ImagePreviewAdapter.ImagePreviewListener,
    QiscusPermissionsUtil.PermissionCallbacks {

    private lateinit var binding: ActivityImageMessageBinding
    private val dataList: MutableList<ImageToSend> = ArrayList()
    private var chatRoom: QChatRoom? = null
    private var pagerAdapter: ImagePagerAdapter? = null
    private var adapter: ImagePreviewAdapter? = null
    private var displayMetrics: DisplayMetrics? = null
    private var avatarTarget: CustomTarget<Drawable>? = null
    private var ubcritionResultFiles: Subscription? = null
    private var currentPosition = 0
    private val color = QiscusMultichannelWidget.instance.getColor()
    private val maxMediaPickerCount = 30
    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxMediaPickerCount)
    ) { uris ->
        if (uris == null) showToast("Failed to open image file!")
        uris?.let {
            val list: Array<String?> = arrayOfNulls(it.size)
            for (i in 0 until it.size) {
                list[i] = QiscusFileUtil.from(it[i]).absolutePath
            }
            toList(list)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setBackgroundDrawable(null)
        initComponent()
        initColor()
        initView()
        listener()
    }

    private fun initColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color.getStatusBarColor()
        }
        binding.toolbar.setBackgroundColor(color.getNavigationColor())
        binding.toolbar.setTitleTextColor(color.getNavigationTitleColor())
        binding.toolbar.setSubtitleTextColor(color.getNavigationTitleColor())
        binding.toolbar.navigationIcon = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_back_white),
            color.getNavigationTitleColor()
        )

        displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        binding.fieldMessage.background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(this@ImageMessageActivity, R.color.qiscus_white_mc))
            shape = GradientDrawable.RECTANGLE
            cornerRadius = ResourceManager.getDimen(displayMetrics!!, 8)
            setStroke(
                ResourceManager.getDimen(displayMetrics!!, 1).toInt(),
                this@ImageMessageActivity.color.getFieldChatBorderColor()
            )
        }

        binding.messageBox.setBackgroundColor(color.getSendContainerBackgroundColor())
        binding.btnSend.setColorFilter(color.getSendContainerColor())
        binding.rvImagePrev.setBackgroundColor(color.getSendContainerBackgroundColor())
    }

    private fun initView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_avatar))
        avatarTarget = object : CustomTarget<Drawable>() {

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                supportActionBar!!.setIcon(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        }

    }

    private fun initComponent() {
        val i = intent
        chatRoom = i.getParcelableExtra(ROOM_DATA)
        for (path in i.getStringArrayExtra(DATA)!!) {
            dataList.add(ImageToSend(path = path))
        }
        if (dataList.size < maxMediaPickerCount) {
            setAddImageButton()
        }
    }

    private fun setRemoveImageButton() {
        val pathAddImage = getString(R.string.add_image)
        for (i in 0 until dataList.size) {
           if (dataList[i].path == pathAddImage) {
               dataList.removeAt(i)
               break
           }
        }
    }

    private fun setAddImageButton() {
        synchronized(this) {
            val lastPosition = dataList.size
            dataList.add(ImageToSend(path = getString(R.string.add_image)))
            adapter?.notifyItemInserted(lastPosition)
        }
    }

    private fun setRoomData() {
        supportActionBar?.title = chatRoom!!.name
        val avatarSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 40f,
            resources.displayMetrics
        ).roundToInt()

        Nirmana.getInstance().get()
            .load(chatRoom!!.avatarUrl)
            .apply(
                RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .skipMemoryCache(false)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .override(avatarSize, avatarSize)
                    .circleCrop()
                    .placeholder(R.drawable.ic_toolbar_avatar)
                    .error(R.drawable.ic_toolbar_avatar)
            )
            .into(avatarTarget!!)
    }

    private fun listener() {
        binding.fieldMessage.addTextChangedListener(ImageTextWatcher())
        binding.btnSend.setOnClickListener {
            val intent = Intent()
            val imagePaths = ArrayList<String>()
            val captions = ArrayList<String>()
            for (item in dataList) {
                if (item.path != getString(R.string.add_image)) {
                    imagePaths.add(item.path)
                    captions.add(item.caption)
                }
            }
            intent.putStringArrayListExtra(DATA, imagePaths)
            intent.putStringArrayListExtra(CAPTION_COMMENT_IMAGE, captions)
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.imageContainer.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                binding.fieldMessage.setText(dataList[position].caption)
                adapter?.clearSelected(position)
                binding.rvImagePrev.smoothScrollToPosition(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setRoomData()
        if (pagerAdapter == null) {
            pagerAdapter = ImagePagerAdapter(this, this, dataList, displayMetrics!!.widthPixels)
            binding.imageContainer.adapter = pagerAdapter
        }
        var type: String? = ""
        try {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                dataList[0].path.substring(dataList[0].path.lastIndexOf('.') + 1)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (type != null && type.contains("image") && adapter == null) {
            adapter = ImagePreviewAdapter(this, this, dataList, displayMetrics!!.widthPixels, color)
            binding.rvImagePrev.also {
                it.setHasFixedSize(true)
                it.itemAnimator = null
                it.animation = null
                it.adapter = adapter
            }
        } else if (adapter == null) {
            binding.rvImagePrev.visibility = View.GONE
        }
    }

    override fun onItemClick(position: Int) {
        if (adapter == null) {
            adapter = ImagePreviewAdapter(this, this, dataList, displayMetrics!!.widthPixels, color)
            binding.rvImagePrev.also {
                it.setHasFixedSize(true)
                it.itemAnimator = null
                it.animation = null
                it.adapter = adapter
            }
        }
        adapter!!.clearOtherSelected(position)
        binding.rvImagePrev.scrollToPosition(position)
    }

    override fun onImagePreviewClick(position: Int) {
        binding.imageContainer.currentItem = position
    }

    @SuppressLint("InflateParams")
    override fun onAddImage() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_attachment_mc, null)
        val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
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

            override fun onSlide(bottomSheet: View, slideOffset: Float) {/*ignored*/}
        })

        dialogView.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(this, R.drawable.bottom_sheet_style),
            color.getSendContainerBackgroundColor()
        )

        dialogView.findViewById<LinearLayout>(R.id.linTakePhoto).also {
            it.findViewById<ImageView>(R.id.imgCamera).setColorFilter(
                color.getNavigationColor()
            )
            it.findViewById<TextView>(R.id.textCamera).setTextColor(
                color.getNavigationColor()
            )
            it.setOnClickListener {
                dialog.dismiss()
                openCamera()
            }
        }
        dialogView.findViewById<LinearLayout>(R.id.linImageGallery).also {
            it.findViewById<ImageView>(R.id.imgGallery).setColorFilter(
                color.getNavigationColor()
            )
            it.findViewById<TextView>(R.id.textGallery).setTextColor(
                color.getNavigationColor()
            )
            it.setOnClickListener {
                dialog.dismiss()
                openGallery()
            }
        }
        dialog.show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        val permission =
            if (Build.VERSION.SDK_INT <= 28) MultichannelConst.CAMERA_PERMISSION_28 else MultichannelConst.CAMERA_PERMISSION
        if (QiscusPermissionsUtil.hasPermissions(this, permission)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = QiscusImageUtil.createImageFile()
                } catch (ex: IOException) {
                    if (ex.message != null && !ex.message.isNullOrEmpty()) {
                        showToast(ex.message!!)
                    } else {
                        showToast(getString(R.string.qiscus_chat_error_failed_write_mc))
                    }

                }

                if (photoFile != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        intent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(
                                this,
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

    private fun openGallery() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (QiscusPermissionsUtil.hasPermissions(this, MultichannelConst.FILE_PERMISSION)) {
                pickImageUsingJupuk()
            } else {
                requestFilePermission()
            }
        } else {
            pickImageUsingIntentSystem()
        }
    }

    private fun pickImageUsingJupuk() {
        JupukBuilder().setMaxCount(maxMediaPickerCount)
            .enableVideoPicker(true)
            .pickPhoto(this)
    }

    /**
     * open images using default gallery for android 11 or higher
     * */
    private fun pickImageUsingIntentSystem() {
        pickMedia.launch(
            PickVisualMediaRequest(PickVisualMedia.ImageAndVideo)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDelete(position: Int) {
        dataList.removeAt(position)
        if (dataList[dataList.size - 1].path != getString(R.string.add_image)) {
            setAddImageButton()
        }
        if (dataList.size > 1) {
            adapter?.let {
                it.notifyDataSetChanged()
                binding.imageContainer.adapter = pagerAdapter
                if (it.selectedPosition == position && position > 0) {
                    scrollToPositon(position - 1)
                } else if (it.selectedPosition == position && position == 0) {
                    scrollToPositon(position)
                } else {
                    scrollToPositon(currentPosition)
                }

            }
        } else {
            onBackPressed()
        }
    }

    private fun scrollToPositon(position: Int) {
        adapter!!.clearSelected(position)
        binding.rvImagePrev.scrollToPosition(position)
        binding.imageContainer.currentItem = position
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT <= 28) {
            if (!QiscusPermissionsUtil.hasPermissions(
                    this,
                    MultichannelConst.CAMERA_PERMISSION_28
                )
            ) {
                QiscusPermissionsUtil.requestPermissions(
                    this, getString(R.string.qiscus_permission_request_title_mc),
                    MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION_28
                )
            }
        } else {
            if (!QiscusPermissionsUtil.hasPermissions(this, MultichannelConst.CAMERA_PERMISSION)) {
                QiscusPermissionsUtil.requestPermissions(
                    this, getString(R.string.qiscus_permission_request_title_mc),
                    MultichannelConst.RC_CAMERA_PERMISSION, MultichannelConst.CAMERA_PERMISSION
                )
            }
        }
    }

    private fun requestFilePermission() {
        if (!QiscusPermissionsUtil.hasPermissions(this, MultichannelConst.FILE_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(
                this, getString(R.string.qiscus_permission_request_title_mc),
                MultichannelConst.RC_FILE_PERMISSION, MultichannelConst.FILE_PERMISSION
            )
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("RestrictedApi")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (dataList.size < 31 && dataList[dataList.size - 1].path == getString(R.string.add_image)) {
            dataList.removeAt(dataList.size - 1)
        }

        if (requestCode == MultichannelConst.TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageFile =
                    QiscusFileUtil.from(Uri.parse(MultichannelConst.qiscusCore()?.cacheManager?.lastImagePath))
                toList(arrayOf(imageFile.absolutePath))
            } catch (e: Exception) {
                showToast(getString(R.string.qiscus_chat_error_failed_read_picture_mc))
                e.printStackTrace()
            }

        } else if (requestCode == MultichannelConst.IMAGE_GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
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
                    toList(list)
                }

            } catch (e: Exception) {
                showToast("Failed to open image file!")
            }
        } else if (requestCode == JupukConst.REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            try {
                val dataListExtras: ArrayList<String> =
                    data?.getStringArrayListExtra(JupukConst.KEY_SELECTED_MEDIA)!!
                val list = arrayOfNulls<String>(dataListExtras.size)

                for (i in dataListExtras.indices) {
                    list[i] = dataListExtras[i]
                }
                toList(list)
            } catch (e: Exception) {
                showToast("Failed to open image file!")
            }
        }
    }

    private fun toList(imagePaths: Array<String?>) {
        val dataImages: MutableList<ImageToSend> = ArrayList()
        for (imagePath in imagePaths) {
            if (dataList.size < maxMediaPickerCount && imagePath != null) {
                dataImages.add(ImageToSend(path = imagePath))
            } else {
                break
            }
        }
        dataList.addAll(dataImages)
        setToList()
    }

    private fun setToList() {
        setRemoveImageButton()
        pagerAdapter?.notifyDataSetChanged()
        adapter?.notifyDataSetChanged()
        binding.rvImagePrev.scrollToPosition(dataList.size - 1)
        binding.imageContainer.currentItem = dataList.size - 1


        binding.rvImagePrev.postDelayed({
            if (dataList.size < maxMediaPickerCount + 1) {
                setAddImageButton()
            } else showToast(
                getString(R.string.max_image_selected)
            )
        }, 200)
    }

    override fun onDestroy() {
        super.onDestroy()
        Nirmana.getInstance().get().clear(avatarTarget)
        if (ubcritionResultFiles != null) {
            ubcritionResultFiles!!.unsubscribe()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        /* when (requestCode) {
             QiscusPermissionsUtils.RC_CAMERA_PERMISSION -> MultichannelPickFile.openCamera(this,
                 { imageFilePath -> this.imageFilePath = imageFilePath },
                 { message: String -> showToast(message) })
             QiscusPermissionsUtils.RC_GALLERY_PERMISSION -> MultichannelPickFile.pickGallery(
                 this,
                 true
             )
         }*/
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        /* do nothing */
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /*QiscusPermissionsUtils.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )*/
    }

    private inner class ImageTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            dataList[binding.imageContainer.currentItem].caption = s.toString()
        }
    }

    companion object {

        val ROOM_DATA: String = "room_data"
        val DATA: String = "data"
        val CAPTION_COMMENT_IMAGE: String = "caption_image"

        fun generateIntent(
            context: Context?,
            qiscusChatRoom: QChatRoom?,
            dataImage: Array<String?>?
        ): Intent {
            return Intent(context, ImageMessageActivity::class.java)
                .putExtra(ROOM_DATA, qiscusChatRoom)
                .putExtra(DATA, dataImage)
        }
    }

}