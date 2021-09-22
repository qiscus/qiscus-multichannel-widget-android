package com.qiscus.multichannel.sample.widget

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.sample.R
import com.qiscus.multichannel.sample.widget.service.FirebaseServices
import com.qiscus.multichannel.util.QiscusChatRoomBuilder
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val qiscusMultichannelWidget = SampleApp.instance.qiscusMultichannelWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // always call when aktif app
        if (qiscusMultichannelWidget.hasSetupUser()) {
            FirebaseServices().getCurrentDeviceToken()
        }

        login.setOnClickListener {
            if (qiscusMultichannelWidget.isLoggedIn()) {
                qiscusMultichannelWidget.clearUser()
                Toast.makeText(this, "Logout Success", Toast.LENGTH_LONG).show()

            } else {
                val username = etDisplayName.text.toString()
                val email = etUserEmail.text.toString()
                val avatarUrl =
                    "https://vignette.wikia.nocookie.net/fatal-fiction-fanon/images/9/9f/Doraemon.png/revision/latest?cb=20170922055255"

                if (isValidEmail(email)) {
                    val userProperties = mapOf("city" to "jogja", "job" to "developer")
                    qiscusMultichannelWidget.setUser(email, username, avatarUrl, userProperties)

                    initChat()
                } else {
                    Toast.makeText(this, "Please check email format", Toast.LENGTH_LONG).show()
                }
            }
            setButton()
        }

        if (qiscusMultichannelWidget.isLoggedIn()) {
            qiscusMultichannelWidget.openChatRoom(this)
        }
    }

    private fun initChat() {
        qiscusMultichannelWidget.initiateChat()
            .showLoadingWhenInitiate(false)
            .setRoomTitle("Custom Title")
            .setAvatar(QiscusMultichannelWidgetConfig.Avatar.DISABLE)
            .setRoomSubtitle(
                QiscusMultichannelWidgetConfig.RoomSubtitle.EDITABLE,
                "Custom subtitle"
            )
            .setShowSystemMessage(true)
            .setSessional(false)
            .startChat(this, object : QiscusChatRoomBuilder.InitiateCallback {
                override fun onProgress() {
                    Log.i("InitiateCallback", "onProgress: ")
                    login.isEnabled = false
                    login.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.bt_qiscus_radius_disable
                    )
                }

                override fun onSuccess(qChatRoom: QChatRoom) {
                    Log.i("InitiateCallback", "onSuccess: ")
                    qiscusMultichannelWidget.openChatRoomById(
                        this@MainActivity,
                        qChatRoom.id,
                        true
                    ) { throwable ->
                        throwable.printStackTrace()
                    }
                    login.isEnabled = true
                    login.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.bt_qiscus_radius_sdk
                    )
                }

                override fun onError(throwable: Throwable) {
                    Log.e("InitiateCallback", "onError: ${throwable.message}")
                    login.isEnabled = true
                    login.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.bt_qiscus_radius_sdk
                    )
                }
            })

        // only 1 after initiateChat
        if (qiscusMultichannelWidget.hasSetupUser()) {
            FirebaseServices().getCurrentDeviceToken()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setButton() {
        tv_start.text = if (qiscusMultichannelWidget.isLoggedIn()) "LOGOUT" else "START"
    }

    override fun onResume() {
        super.onResume()
        setButton()
    }

}
