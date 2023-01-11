package com.qiscus.multichannel.sample.widget

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.sample.R
import com.qiscus.multichannel.sample.widget.service.FirebaseServices
import com.qiscus.multichannel.util.QiscusChatRoomBuilder
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val qiscusMultichannelWidget = SampleApp.instance.qiscusMultichannelWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // always call when active app
        if (qiscusMultichannelWidget.hasSetupUser()) {
            FirebaseServices().getCurrentDeviceToken()
        }

        login.setOnClickListener {
            validateAndSetUser(object : OnValidSetUser {
                override fun call() {
                    initChat(0)
                }
            })
            setButton()
        }

        sendMessage.setOnClickListener {
            validateAndSetUser(object : OnValidSetUser {
                override fun call() {
                    seProgressSendMessage(true)
                    initChatWithSendMessage(0, "Sample Text Message!")
                }
            })
            setButton()
        }

        if (qiscusMultichannelWidget.isLoggedIn()) {
            qiscusMultichannelWidget.openChatRoom(this)
        }
    }

    private fun validateAndSetUser(onValid: OnValidSetUser) {
        if (qiscusMultichannelWidget.isLoggedIn()) {
            qiscusMultichannelWidget.clearUser()
            Toast.makeText(this, "Logout Success", Toast.LENGTH_LONG).show()

        } else {
            val username = etDisplayName.text.toString()
            val email = etUserEmail.text.toString()
            val avatarUrl =
                "https://vignette.wikia.nocookie.net/fatal-fiction-fanon/images/9/9f/Doraemon.png/revision/latest?cb=20170922055255"

            if (isValidEmail(email)) {
                val userProperties = mapOf(
                    "city" to "jogja",
                    "job" to "developer"
                ) // userProperties are additional details of the user(optional)
                qiscusMultichannelWidget.setUser(email, username, avatarUrl, userProperties)

                onValid.call()
            } else {
                Toast.makeText(this, "Please check email format", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initChat(channelId: Int) {
        configureInitiateChat(channelId)
            .showLoadingWhenInitiate(true)
            .startChat(this, object : QiscusChatRoomBuilder.InitiateCallback {
                override fun onProgress() {
                    Log.i("InitiateCallback", "onProgress: ")
                    login.isEnabled = false
                    login.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.bt_qiscus_radius_disable
                    )
                }

                override fun onSuccess(
                    qChatRoom: QChatRoom,
                    qMessage: QMessage?,
                    isAutomatic: Boolean
                ) {
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

    private fun initChatWithSendMessage(channelId: Int, textMessage: String) {
        configureInitiateChat(channelId)
            .automaticSendMessage(textMessage)
            .startChat(this)

        // only 1 after initiateChat
        if (qiscusMultichannelWidget.hasSetupUser()) {
            FirebaseServices().getCurrentDeviceToken()
        }
    }

    private fun configureInitiateChat(channelId: Int) = qiscusMultichannelWidget.initiateChat()
        .showLoadingWhenInitiate(false)
//            .setChannelId(channelId) // manual set channels id
        .setRoomTitle("Custom Title")
        .setAvatar(QiscusMultichannelWidgetConfig.Avatar.DISABLE)
        .setRoomSubtitle(
            QiscusMultichannelWidgetConfig.RoomSubtitle.EDITABLE,
            "Custom subtitle"
        )
        .setShowSystemMessage(true)
        .setSessional(false)

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setButton() {
        tv_start.text = if (qiscusMultichannelWidget.isLoggedIn()) "LOGOUT" else "START"
        tv_send_message.text = if (qiscusMultichannelWidget.isLoggedIn()) "LOGOUT" else "SEND MESSAGE"
    }

    private fun seProgressSendMessage(isActive: Boolean) {
        progress_bar_send_message.visibility = if (isActive) View.VISIBLE else View.GONE
        tv_send_message.visibility =if (isActive) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        setButton()
        seProgressSendMessage(false)
    }

    interface OnValidSetUser {
        fun call();
    }
}
