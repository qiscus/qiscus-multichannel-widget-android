package com.qiscus.multichannel.util

import com.qiscus.multichannel.QWidgetComponent
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.local.QiscusSessionLocal
import com.qiscus.multichannel.data.model.DataInitialChat
import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

internal class SecureSessionImpl(
    private val component: QWidgetComponent,
    private var config: QiscusMultichannelWidgetConfig
) : SecureSession {

    private var onCompleted: SessionCompleteListener? = null

    override fun setCompleteListener(onCompleted: SessionCompleteListener) {
        this.onCompleted = onCompleted
    }

    override fun initiateChat(
        name: String?,
        userId: String?,
        avatar: String?,
        sessionId: String?,
        extras: JSONObject?,
        userProperties: List<UserProperties>?,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        QiscusSessionLocal.removeInitiate()

        component.getChatroomRepository().getJwtNonce(
            name, userId, avatar, sessionId, extras?.toString() ?: "{}", userProperties,
            {
                runInitiateChat(
                    DataInitialChat(
                        MultichannelConst.qiscusCore()!!.appId,
                        userId,
                        getIfEmail(userId),
                        name,
                        avatar,
                        it.nonce,
                        MultichannelConst.ORIGIN,
                        extras?.toString() ?: "{}",
                        userProperties,
                        config.getChannelId(),
                        sessionId
                    ),
                    {
                        onCompleted?.onCompleted()
                        onSuccess.invoke(it)
                    },
                    onError
                )
            }) {
            onError(it)
        }
    }

    private fun runInitiateChat(
        dataInitialChat: DataInitialChat,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        component.getQiscusChatRepository()
            .initiateChat(dataInitialChat,
                { response ->
                    response.data.also { data ->
                        data.isSessional?.let { sessional ->
                            config.setSessional(sessional)
                        }

                        data.customerRoom?.let { room ->
                            QiscusChatLocal.setRoomId(room.roomId!!.toLong())
                            QiscusSessionLocal.save(
                                room.userId,
                                data.isSecure != null && data.isSecure,
                                room.sessionId
                            )
                        }

                        data.identityToken?.let { token ->
                            MultichannelConst.qiscusCore()?.setUserWithIdentityToken(token,
                                object : QiscusCore.SetUserListener {
                                    override fun onSuccess(qiscusAccount: QAccount) {
                                        onSuccess(qiscusAccount)
                                    }

                                    override fun onError(throwable: Throwable) {
                                        onError(throwable)
                                    }
                                })
                        }
                    }
                }, { throwable ->
                    onError(throwable)
                })
    }

    override fun goToChatroom(
        roomId: Long,
        onSuccess: (QChatRoom) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        MultichannelConst.qiscusCore()!!.api.getChatRoomInfo(roomId)
            .doOnNext {
                MultichannelConst.qiscusCore()!!.dataStore.addOrUpdate(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }

    private fun getIfEmail(userId: String?): String? {
        return if (userId != null && android.util.Patterns.EMAIL_ADDRESS.matcher(userId)
                .matches()
        ) {
            return userId
        } else {
            null
        }
    }

}

internal interface SecureSession {

    fun setCompleteListener(onCompleted: SessionCompleteListener)

    fun initiateChat(
        name: String?,
        userId: String?,
        avatar: String?,
        sessionId: String?,
        extras: JSONObject?,
        userProperties: List<UserProperties>?,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    )

    fun goToChatroom(
        roomId: Long,
        onSuccess: (QChatRoom) -> Unit,
        onError: (Throwable) -> Unit
    )
}