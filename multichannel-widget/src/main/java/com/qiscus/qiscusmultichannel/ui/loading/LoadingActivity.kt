package com.qiscus.qiscusmultichannel.ui.loading

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.data.model.user.UserProperties
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.util.MultichanelChatWidget
import com.qiscus.qiscusmultichannel.util.showToast
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import kotlinx.android.synthetic.main.activity_loading.*
import org.json.JSONObject

/**
 * Created on : 04/03/20
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */

class LoadingActivity : AppCompatActivity(), LoadingPresenter.LoadingView {

    private lateinit var presenter: LoadingPresenter
    private var username: String? = null
    private var userId: String? = null
    private var extras: String? = null
    private var avatar: String? = null
    private var userProp: ArrayList<UserProperties>? = null
    private val qiscusMultichannelWidget: MultichanelChatWidget = QiscusMultichannelWidget.instance

    companion object {
        private val PARAM_USERNAME = "username"
        private val PARAM_USERID = "userid"
        private val PARAM_AVATAR = "avatar"
        private val PARAM_EXTRAS = "extras"
        private val PARAM_USER_PROPERTIES = "user_properties"

        fun generateIntent(context: Context, username: String?, userId: String?, avatar: String?, extras: JSONObject?, userProp: List<UserProperties>) {
            val intent = Intent(context, LoadingActivity::class.java)
            intent.putExtra(PARAM_USERNAME, username)
            intent.putExtra(PARAM_USERID, userId)
            intent.putExtra(PARAM_AVATAR, avatar)
            intent.putExtra(PARAM_EXTRAS, extras?.toString() ?: "{}")
            intent.putExtra(PARAM_USER_PROPERTIES, ArrayList(userProp))
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        initColor()
        presenter = LoadingPresenter(qiscusMultichannelWidget)

        intent?.let {
            username = it.getStringExtra(PARAM_USERNAME).toString()
            userId = it.getStringExtra(PARAM_USERID).toString()
            extras = it.getStringExtra(PARAM_EXTRAS).toString()
            avatar = it.getStringExtra(PARAM_AVATAR).toString()
            userProp = it.getSerializableExtra(PARAM_USER_PROPERTIES) as ArrayList<UserProperties>
        }
    }

    private fun initColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = qiscusMultichannelWidget.getColor().getStatusBarColor()
        }

        container.setBackgroundColor(qiscusMultichannelWidget.getColor().getNavigationColor())
        textLoading.setTextColor(qiscusMultichannelWidget.getColor().getNavigationTitleColor())
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)

        presenter.initiateChat(username, userId, avatar, extras, userProp?.toList())
    }

    override fun onStop() {
        super.onStop()
        presenter.detach()
    }

    override fun onError(message: String) {
        showToast(message)
        finish()
    }

    override fun onSuccess(room: QChatRoom) {
        val intent = ChatRoomActivity.generateIntent(this, room)
        startActivity(intent)
        finish()
    }
}
