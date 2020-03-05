package com.qiscus.integrations.multichannel_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.qiscus.qiscusmultichannel.MultichannelWidget
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpen.setOnClickListener {
            MultichannelWidget.instance.initiateChat(this, "taufik dev", "taufik@qiscus.net","", null)
        }
    }
}
