package com.qiscus.multichannel.ui.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qiscus.multichannel.R
import com.qiscus.multichannel.util.QiscusPermissionsUtil

class BlankForTestActivity : AppCompatActivity(), QiscusPermissionsUtil.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank_for_test_mc)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // ignored
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // ignored
    }
}