package com.qiscus.qiscusmultichannel

import android.content.Context
import com.qiscus.qiscusmultichannel.data.model.user.UserProperties
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.ui.loading.LoadingActivity
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.data.model.QChatRoom

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusChatRoomBuilder {

    private var showLoading: Boolean = false

    fun showLoadingWhenInitiate(showLoading: Boolean) = apply { this.showLoading = showLoading }

    fun startChat(context: Context) {
        startChatWithCallback(context, null)
    }

    fun startChatWithCallback(context: Context, initiateCallback: InitiateCallback?) {
        QiscusMultichannelWidget.instance.getUser().let {
            val userProp: MutableList<UserProperties> = ArrayList()
            it.userProperties?.let { properties ->
                for ((k, v) in properties) {
                    val obj = UserProperties(k, v)
                    userProp.add(obj)
                }
            }

            if (isRequiredDataValid(it.name, it.userId)) {
                if (this.showLoading) {
                    LoadingActivity.generateIntent(
                        context,
                        it.name,
                        it.userId,
                        it.avatar,
                        null,
                        userProp
                    )
                } else {
                    initiateCallback?.onProgress()
                    QiscusMultichannelWidget.instance.loginMultiChannel(
                        it.name,
                        it.userId,
                        it.avatar,
                        "{}",
                        userProp,
                        {
                            loadChatRoom(context, initiateCallback)
                        },
                        { throwable ->
                            initiateCallback?.onError(throwable)
                        })
                }
            } else {
                showRuntimeErrorWhenDataInvalid("Make sure name and userId data is filled")
            }
        }

    }

    private fun loadChatRoom(context: Context, initiateCallback: InitiateCallback?) {
        QiscusMultichannelWidget.instance.openChatRoomById(QiscusChatLocal.getRoomId(), {
            if (initiateCallback != null) {
                initiateCallback.onSuccess(it)
            } else {
                openChatRoomWithIntent(context, it)
            }
        }, {
            initiateCallback?.onError(it)
        })
    }

    private fun openChatRoomWithIntent(
        context: Context,
        it: QChatRoom
    ) {
        context.startActivity(ChatRoomActivity.generateIntent(context, it))
    }

    private fun isRequiredDataValid(name: String, userId: String): Boolean {
        return name != null && userId != null
    }

    private fun showRuntimeErrorWhenDataInvalid(message: String) {
        throw RuntimeException(message)
    }

    interface InitiateCallback {
        fun onProgress()
        fun onSuccess(qChatRoom: QChatRoom)
        fun onError(throwable: Throwable)
    }

}