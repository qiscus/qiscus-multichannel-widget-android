package com.qiscus.qiscusmultichannel.ui.loading

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.data.model.UserProperties
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.util.showToast
import com.qiscus.sdk.chat.core.data.model.QChatRoom
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

    companion object {
        private val PARAM_USERNAME = "username"
        private val PARAM_USERID = "userid"
        private val PARAM_AVATAR = "avatar"
        private val PARAM_EXTRAS = "extras"
        private val PARAM_USER_PROPERTIES = "user_properties"

        fun generateIntent(context: Context, username: String, userId: String, avatar: String?, extras: JSONObject?, userProp: List<UserProperties>) {
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
        presenter = LoadingPresenter()

        intent?.let {
            username = it.getStringExtra(PARAM_USERNAME).toString()
            userId = it.getStringExtra(PARAM_USERID).toString()
            extras = it.getStringExtra(PARAM_EXTRAS).toString()
            avatar = it.getStringExtra(PARAM_AVATAR).toString()
            userProp = it.getSerializableExtra(PARAM_USER_PROPERTIES) as ArrayList<UserProperties>
        }
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
