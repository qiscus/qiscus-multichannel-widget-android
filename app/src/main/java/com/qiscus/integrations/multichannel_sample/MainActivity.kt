package com.qiscus.integrations.multichannel_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qiscus.qiscusmultichannel.MultichannelWidget
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userProperties = mapOf("city" to "jogja", "job" to "developer")

        btnOpen.setOnClickListener {
            MultichannelWidget.instance.initiateChat(this, "taufik", "taufik@qiscus.net","https://vignette.wikia.nocookie.net/fatal-fiction-fanon/images/9/9f/Doraemon.png/revision/latest?cb=20170922055255", null, userProperties)

        }
    }
}
