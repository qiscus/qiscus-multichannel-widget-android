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
import com.qiscus.multichannel.sample.databinding.ActivityMainBinding
import com.qiscus.multichannel.sample.widget.service.FirebaseServices
import com.qiscus.multichannel.util.QiscusChatRoomBuilder
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val qiscusMultichannelWidget = SampleApp.instance.qiscusMultichannelWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // always call when active app
        if (qiscusMultichannelWidget.hasSetupUser()) {
            FirebaseServices().getCurrentDeviceToken()
        }

        binding.login.setOnClickListener {
            validateAndSetUser(object : OnValidSetUser {
                override fun call() {
                    initChat(0)
                }
            })
            setButton()
        }

        binding.sendMessage.setOnClickListener {
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
            val username = binding.etDisplayName.text.toString()
            val email = binding.etUserEmail.text.toString()
            val avatarUrl =
                "https://vignette.wikia.nocookie.net/fatal-fiction-fanon/images/9/9f/Doraemon.png/revision/latest?cb=20170922055255"

            if (isValidEmail(email)) {
                qiscusMultichannelWidget.setUser(
                    userId = email,
                    name = username,
                    avatar = avatarUrl,
                    // userProperties are additional details of the user(optional)
                    userProperties = mapOf(
                        "city" to "jogja",
                        "job" to "developer"
                    ),
                    // extras custom data(optional)
                    extras = JSONObject("{\"user_lastname\": \"red\" }")
                )

                onValid.call()
            } else {
                Toast.makeText(this, "Please check email format", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initChat(channelId: Int) {
        configureInitiateChat(channelId)
            .showLoadingWhenInitiate(true) // if showLoadingWhenInitiate is true it doesn't trigger the callback
            .startChat(this, object : QiscusChatRoomBuilder.InitiateCallback {
                override fun onProgress() {
                    Log.i("InitiateCallback", "onProgress: ")
                    binding.login.isEnabled = false
                    binding.login.background = ContextCompat.getDrawable(
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

                     /**
                      * replace openChatRoomById to openChatRoom
                      * only call openChatRoom after initiateChat
                     * */

                    qiscusMultichannelWidget.openChatRoom(
                        this@MainActivity,
                        qChatRoom,
                        qMessage,
                        isAutomatic,
                        true
                    ) {
                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
                    }

                    binding.login.isEnabled = true
                    binding.login.background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.bt_qiscus_radius_sdk
                    )
                }

                override fun onError(throwable: Throwable) {
                    Log.e("InitiateCallback", "onError: ${throwable.message}")
                    binding.login.isEnabled = true
                    binding.login.background = ContextCompat.getDrawable(
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
        binding.tvStart.text = if (qiscusMultichannelWidget.isLoggedIn()) "LOGOUT" else "START"
        binding.sendMessage.visibility = if (qiscusMultichannelWidget.isLoggedIn()) View.GONE else View.VISIBLE
    }

    private fun seProgressSendMessage(isActive: Boolean) {
        binding.progressBarSendMessage.visibility = if (isActive) View.VISIBLE else View.GONE
        binding.tvSendMessage.visibility = if (isActive) View.GONE else View.VISIBLE
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
