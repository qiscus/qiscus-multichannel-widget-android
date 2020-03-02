package com.qiscus.qiscusmultichannel.ui.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusChatScrollListener(
    private val linearLayoutManager: LinearLayoutManager,
    private val listener: Listener
) : RecyclerView.OnScrollListener() {
    private var onTop: Boolean = false
    private var onBottom = true
    private var onMiddle: Boolean = false


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (linearLayoutManager.findFirstVisibleItemPosition() <= 0 && !onTop) {
            listener.onBottomOffListMessage()
            onBottom = true
            onTop = false
            onMiddle = false
        } else if (linearLayoutManager.findLastVisibleItemPosition() >= linearLayoutManager.itemCount - 1 && !onBottom) {
            listener.onTopOffListMessage()
            onTop = true
            onBottom = false
            onMiddle = false
        } else if (!onMiddle) {
            listener.onMiddleOffListMessage()
            onMiddle = true
            onTop = false
            onBottom = false
        }
    }

    interface Listener {
        fun onTopOffListMessage()

        fun onMiddleOffListMessage()

        fun onBottomOffListMessage()
    }
}