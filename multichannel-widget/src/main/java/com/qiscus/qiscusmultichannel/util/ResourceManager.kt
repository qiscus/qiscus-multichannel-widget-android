package com.qiscus.qiscusmultichannel.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.R

/**
 * Created on : 16/08/21
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
object ResourceManager {

    // color
    var PENDING_STATE_COLOR: Int? = null
    var READ_STATE_COLOR: Int? = null
    var FAILED_STATE_COLOR: Int? = null

    // drawable
    var IC_SELECTED_BACKGROUND: Drawable? = null
    var IC_CHAT_FROM_ME: Drawable? = null
    var IC_CHAT_FROM: Drawable? = null

    // dimention
    var DIMEN_ROUNDED_IMAGE: Int = 0

    fun setUp(context: Context, color: QiscusMultichannelWidgetColor) {
        this.PENDING_STATE_COLOR = ContextCompat.getColor(context, R.color.pending_message_mc)
        this.READ_STATE_COLOR = color.getNavigationColor()
        this.FAILED_STATE_COLOR = ContextCompat.getColor(context, R.color.qiscus_red_mc)

        this.IC_SELECTED_BACKGROUND =
            ColorDrawable(color.getSelectedMessageColor())
        this.IC_CHAT_FROM = getTintDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_chat_from),
            color.getLeftBubbleColor()
        )
        this.IC_CHAT_FROM_ME = getTintDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_chat_from_me),
            color.getRightBubbleColor()
        )
    }

    fun getDimen(displayMetrics: DisplayMetrics, dimen: Int) =
        dimen * displayMetrics.density

    fun getColor(@ColorRes color: Int?, @ColorRes id: Int): Int =
        ContextCompat.getColor(MultichannelConst.qiscusCore()?.apps!!, color ?: id)

    fun getTintDrawable(drawable: Drawable?, @ColorInt color: Int): Drawable? {
        val wrappedDrawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }

}