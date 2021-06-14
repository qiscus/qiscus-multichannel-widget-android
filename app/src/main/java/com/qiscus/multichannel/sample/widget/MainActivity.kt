package com.qiscus.multichannel.sample.widget

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qiscus.multichannel.sample.R
import com.qiscus.multichannel.sample.widget.service.FirebaseServices
import com.qiscus.qiscusmultichannel.MultichannelWidget
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //always call when aktif app
        if (MultichannelWidget.instance.hasSetupUser()) {
            FirebaseServices().getCurrentDeviceToken()
        }

        val userProperties = mapOf("city" to "jogja", "job" to "developer")

        login.setOnClickListener {
            val email = etUserEmail.text.toString()
            val username = etDisplayName.text.toString()

            if (isValidEmail(email)) {
                MultichannelWidget.InitiateChat()
                    .withName(username)
                    .withUserId(email)
                    .withAvatar("https://vignette.wikia.nocookie.net/fatal-fiction-fanon/images/9/9f/Doraemon.png/revision/latest?cb=20170922055255")
                    .withUserProperties(userProperties)
                    .showLoadingWhenInitiate(true)
                    .initiateAndOpenChatRoom(this)
//                    .initiateWithCallback(this, object : MultichannelWidget.InitiateCallback {
//                        override fun onProgress() {
//                            Log.e("InitiateCallback", "onProgress: ")
//                        }
//
//                        override fun onSuccess(qChatRoom: QChatRoom) {
//                            Log.e("InitiateCallback", "onSuccess: ")
//                        }
//
//                        override fun onError(throwable: Throwable) {
//                            Log.e("InitiateCallback", "onError: ${throwable.message}")
//                        }
//                    })

                // only 1 after initiateChat
                if (MultichannelWidget.instance.hasSetupUser()) {
                    FirebaseServices()
                        .getCurrentDeviceToken()
                }
            } else {
                Toast.makeText(this, "Please check email format", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
