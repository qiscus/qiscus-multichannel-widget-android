package com.qiscus.multichannel.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.qiscus.multichannel.util.ResourceManager

class DotIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {

    private var currentDotView: View? = null
    private var selectedColor: Drawable? = null
    private var unselectedColor: Drawable? = null
    private var dotSpacing: Int = -1
    private var dotSize: Int = -1

    fun setSpacing(spaceDp: Int) {
        this.dotSpacing = ResourceManager.getDimen(
            context.resources.displayMetrics, spaceDp
        ).toInt()
    }

    fun setSize(sizeDp: Int) {
        this.dotSize = ResourceManager.getDimen(
            context.resources.displayMetrics, sizeDp
        ).toInt()
    }

    fun setSelectedColor(color: Int) {
        this.selectedColor = drawCircle(color)
    }

    fun setUnselectedColor(color: Int) {
        this.unselectedColor = drawCircle(color)
    }

    fun setPosition(position: Int) {
        currentDotView?.background = unselectedColor
        currentDotView = getChildAt(position)
        currentDotView?.background = selectedColor
    }

    fun createDotIndicator(count: Int) {
        removeAllViews()

        for (i in 0 until count) {
            val dotView = View(context).apply {
                background = if (i == 0) selectedColor else unselectedColor
            }

            val params = LayoutParams(dotSize, dotSize).apply {
                setMargins(dotSpacing, 0, dotSpacing, 0)
            }

            addView(dotView, params)
        }

    }

    private fun drawCircle(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        setColor(color)
    }

}