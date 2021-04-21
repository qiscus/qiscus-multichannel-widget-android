package com.qiscus.qiscusmultichannel.ui.view

import android.view.View.*
import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created on : 2019-09-20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
interface QiscusProgressView {

    fun getProgress(): Int

    fun setProgress(progress: Int)

    fun getFinishedColor(): Int

    fun setFinishedColor(finishedColor: Int)

    fun getUnfinishedColor(): Int

    fun setUnfinishedColor(unfinishedColor: Int)

    fun setVisibility(@Visibility visibility: Int)

    @IntDef(VISIBLE, INVISIBLE, GONE)
    @Retention(RetentionPolicy.SOURCE)
    annotation class Visibility
}