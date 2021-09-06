package com.qiscus.qiscusmultichannel

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.graphics.ColorUtils
import com.qiscus.qiscusmultichannel.util.ResourceManager.getColor

class QiscusMultichannelWidgetColor {

    private var statusBar: Int? = null
    private var navigation: Int? = null
    private var sendContainer: Int? = null
    private var fieldChatBorder: Int? = null
    private var sendContainerBackground: Int? = null
    private var navigationTitle: Int? = null
    private var systemEventText: Int? = null
    private var leftBubble: Int? = null
    private var rightBubble: Int? = null
    private var leftBubbleText: Int? = null
    private var rightBubbleText: Int? = null
    private var timeLabelText: Int? = null
    private var timeBackground: Int? = null
    private var base: Int? = null
    private var emptyText: Int? = null
    private var emptyBackground: Int? = null

    fun setStatusBarColor(@ColorRes id: Int) = apply {
        this.statusBar = id
    }

    fun setNavigationColor(@ColorRes id: Int) = apply {
        this.navigation = id
    }

    fun setSendContainerColor(@ColorRes id: Int) = apply {
        this.sendContainer = id
    }

    fun setFieldChatBorderColor(@ColorRes id: Int) = apply {
        this.fieldChatBorder = id
    }

    fun setSendContainerBackgroundColor(@ColorRes id: Int) = apply {
        this.sendContainerBackground = id
    }

    fun setNavigationTitleColor(@ColorRes id: Int) = apply {
        this.navigationTitle = id
    }

    fun setSystemEventTextColor(@ColorRes id: Int) = apply {
        this.systemEventText = id
    }

    fun setLeftBubbleColor(@ColorRes id: Int) = apply {
        this.leftBubble = id
    }

    fun setRightBubbleColor(@ColorRes id: Int) = apply {
        this.rightBubble = id
    }

    fun setLeftBubbleTextColor(@ColorRes id: Int) = apply {
        this.leftBubbleText = id
    }

    fun setRightBubbleTextColor(@ColorRes id: Int) = apply {
        this.rightBubbleText = id
    }

    fun setTimeLabelTextColor(@ColorRes id: Int) = apply {
        this.timeLabelText = id
    }

    fun setTimeBackgroundColor(@ColorRes id: Int) = apply {
        this.timeBackground = id
    }

    fun setBaseColor(@ColorRes id: Int) = apply {
        this.base = id
    }

    fun setEmptyTextColor(@ColorRes id: Int) = apply {
        this.emptyText = id
    }

    fun setEmptyBacgroundColor(@ColorRes id: Int) = apply {
        this.emptyBackground = id
    }

    @ColorInt
    fun getStatusBarColor() = getColor(this.statusBar, R.color.qiscus_statusbar_mc)

    @ColorInt
    fun getNavigationColor() = getColor(this.navigation, R.color.qiscus_appbar_mc)

    @ColorInt
    fun getSendContainerColor() = getColor(this.sendContainer, R.color.qiscus_appbar_mc)

    @ColorInt
    fun getFieldChatBorderColor() = getColor(this.fieldChatBorder, R.color.qiscus_light_grey_mc)

    @ColorInt
    fun getSendContainerBackgroundColor() = getColor(
        this.sendContainerBackground,
        R.color.qiscus_send_container_mc
    )

    @ColorInt
    fun getNavigationTitleColor() = getColor(this.navigationTitle, R.color.qiscus_title_mc)

    @ColorInt
    fun getSystemEventTextColor() = getColor(this.systemEventText, R.color.qiscus_white_mc)

    @ColorInt
    fun getLeftBubbleColor() = getColor(this.leftBubble, R.color.qiscus_left_bubble_mc)

    @ColorInt
    fun getRightBubbleColor() = getColor(this.rightBubble, R.color.qiscus_right_bubble_mc)

    @ColorInt
    fun getLeftBubbleTextColor() = getColor(this.leftBubbleText, R.color.qiscus_left_bubble_text_mc)

    @ColorInt
    fun getRightBubbleTextColor() = getColor(
        this.rightBubbleText,
        R.color.qiscus_right_bubble_text_mc
    )

    @ColorInt
    fun getTimeLabelTextColor() = getColor(this.timeLabelText, R.color.qiscus_white_mc)

    @ColorInt
    fun getTimeBackgroundColor() = getColor(this.timeBackground, R.color.dateBackground_mc)

    @ColorInt
    fun getBaseColor() = getColor(this.base, R.color.qiscus_white_mc)

    @ColorInt
    fun getEmptyTextColor() = getColor(this.emptyText, R.color.qiscus_darker_gray)

    @ColorInt
    fun getEmptyBacgroundColor() = getColor(this.emptyBackground, R.color.qiscus_white_mc)

    @ColorInt
    fun getSelectedMessageColor(): Int {
        return ColorUtils.setAlphaComponent(getNavigationColor(), 50)
    }

}
