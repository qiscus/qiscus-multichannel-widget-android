package com.qiscus.qiscusmultichannel.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.QiscusConverterUtil

/**
 * Created on : 2019-09-20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class QiscusCircleProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr), QiscusProgressView {
    private val defaultFinishedColor = Color.rgb(66, 145, 241)
    private val defaultUnfinishedColor = Color.rgb(204, 204, 204)
    private val defaultTextColor = Color.WHITE
    private val defaultMax = 100
    private val defaultTextSize: Float
    private val minSize: Int
    private var textPaint: Paint? = null
    private val rectF = RectF()
    private var textSize: Float = 0.toFloat()
    private var textColor: Int = 0
    var prgress = 0
    var max: Int = 0
        set(max) {
            if (max > 0) {
                field = max
                invalidate()
            }
        }
    private var finishedColor: Int = 0
    private var unfinishedColor: Int = 0
    private var prefixText: String? = ""
    private var suffixText: String? = "%"
    private val paint = Paint()

    val drawText: String
        get() = getPrefixText() + prgress + getSuffixText()

    val progressPercentage: Float
        get() = prgress / max.toFloat()

    init {

        defaultTextSize = QiscusConverterUtil.sp2px(resources, 18f)
        minSize = QiscusConverterUtil.dp2px(resources, 100f).toInt()

        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.QiscusCircleProgress,
            defStyleAttr,
            0
        )
        initByAttributes(attributes)
        attributes.recycle()

        initPainters()
    }

    protected fun initByAttributes(attributes: TypedArray) {
        finishedColor = attributes.getColor(
            R.styleable.QiscusCircleProgress_qcircle_finished_color,
            defaultFinishedColor
        )
        unfinishedColor = attributes.getColor(
            R.styleable.QiscusCircleProgress_qcircle_unfinished_color,
            defaultUnfinishedColor
        )
        textColor = attributes.getColor(
            R.styleable.QiscusCircleProgress_qcircle_text_color,
            defaultTextColor
        )
        textSize = attributes.getDimension(
            R.styleable.QiscusCircleProgress_qcircle_text_size,
            defaultTextSize
        )

        max = attributes.getInt(R.styleable.QiscusCircleProgress_qcircle_max, defaultMax)
        prgress = attributes.getInt(R.styleable.QiscusCircleProgress_qcircle_progress, 0)

        if (attributes.getString(R.styleable.QiscusCircleProgress_qcircle_prefix_text) != null) {
            setPrefixText(attributes.getString(R.styleable.QiscusCircleProgress_qcircle_prefix_text))
        }
        if (attributes.getString(R.styleable.QiscusCircleProgress_qcircle_suffix_text) != null) {
            setSuffixText(attributes.getString(R.styleable.QiscusCircleProgress_qcircle_suffix_text))
        }
    }

    protected fun initPainters() {
        textPaint = TextPaint()
        textPaint!!.color = textColor
        textPaint!!.textSize = textSize
        textPaint!!.isAntiAlias = true

        paint.isAntiAlias = true
    }

    override fun invalidate() {
        initPainters()
        super.invalidate()
    }

    fun getTextSize(): Float {
        return textSize
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        this.invalidate()
    }

    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        this.invalidate()
    }

    override fun getProgress(): Int {
        return prgress
    }

    override fun setProgress(progress: Int) {
        this.prgress = progress
        if (this.prgress > max) {
            this.prgress %= max
        }
        invalidate()
    }

    override fun getFinishedColor(): Int {
        return finishedColor
    }

    override fun setFinishedColor(finishedColor: Int) {
        this.finishedColor = finishedColor
        this.invalidate()
    }

    override fun getUnfinishedColor(): Int {
        return unfinishedColor
    }

    override fun setUnfinishedColor(unfinishedColor: Int) {
        this.unfinishedColor = unfinishedColor
        this.invalidate()
    }

    fun getPrefixText(): String? {
        return prefixText
    }

    fun setPrefixText(prefixText: String?) {
        this.prefixText = prefixText
        this.invalidate()
    }

    fun getSuffixText(): String? {
        return suffixText
    }

    fun setSuffixText(suffixText: String?) {
        this.suffixText = suffixText
        this.invalidate()
    }

    override fun getSuggestedMinimumHeight(): Int {
        return minSize
    }

    override fun getSuggestedMinimumWidth(): Int {
        return minSize
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        rectF.set(
            0f,
            0f,
            View.MeasureSpec.getSize(widthMeasureSpec).toFloat(),
            View.MeasureSpec.getSize(heightMeasureSpec).toFloat()
        )
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        val yHeight = prgress / max.toFloat() * height
        val radius = width / 2f
        val angle = (Math.acos(((radius - yHeight) / radius).toDouble()) * 180 / Math.PI).toFloat()
        val startAngle = 90 + angle
        val sweepAngle = 360 - angle * 2
        paint.color = getUnfinishedColor()
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)

        canvas.save()
        canvas.rotate(180f, (width / 2).toFloat(), (height / 2).toFloat())
        paint.color = getFinishedColor()
        canvas.drawArc(rectF, 270 - angle, angle * 2, false, paint)
        canvas.restore()

        // Also works.
        //        paint.setColor(getFinishedColor());
        //        canvas.drawArc(rectF, 90 - angle, angle * 2, false, paint);

        val text = drawText
        if (!TextUtils.isEmpty(text)) {
            val textHeight = textPaint!!.descent() + textPaint!!.ascent()
            canvas.drawText(
                text,
                (width - textPaint!!.measureText(text)) / 2.0f,
                (width - textHeight) / 2.0f,
                textPaint!!
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor())
        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize())
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedColor())
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedColor())
        bundle.putInt(INSTANCE_MAX, max)
        bundle.putInt(INSTANCE_PROGRESS, prgress)
        bundle.putString(INSTANCE_SUFFIX, getSuffixText())
        bundle.putString(INSTANCE_PREFIX, getPrefixText())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            textColor = state.getInt(INSTANCE_TEXT_COLOR)
            textSize = state.getFloat(INSTANCE_TEXT_SIZE)
            finishedColor = state.getInt(INSTANCE_FINISHED_STROKE_COLOR)
            unfinishedColor = state.getInt(INSTANCE_UNFINISHED_STROKE_COLOR)
            initPainters()
            max = state.getInt(INSTANCE_MAX)
            prgress = state.getInt(INSTANCE_PROGRESS)
            prefixText = state.getString(INSTANCE_PREFIX)
            suffixText = state.getString(INSTANCE_SUFFIX)
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
    }

    companion object {
        private val INSTANCE_STATE = "saved_instance"
        private val INSTANCE_TEXT_COLOR = "text_color"
        private val INSTANCE_TEXT_SIZE = "text_size"
        private val INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color"
        private val INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color"
        private val INSTANCE_MAX = "max"
        private val INSTANCE_PROGRESS = "progress"
        private val INSTANCE_SUFFIX = "suffix"
        private val INSTANCE_PREFIX = "prefix"
    }
}
