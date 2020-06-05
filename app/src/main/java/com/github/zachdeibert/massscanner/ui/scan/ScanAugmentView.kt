package com.github.zachdeibert.massscanner.ui.scan

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.View
import com.github.zachdeibert.massscanner.R
import kotlin.math.max

class ScanAugmentView : View, ScanThread.AnalysisListener {
    private val paint = Paint()
    private lateinit var animation: AnimatedVectorDrawable
    private var points: Array<Point>? = null

    private var _imageSize: Size = Size(1080, 1920)
    var imageSize: Size
        get() = _imageSize
        set(value) {
            if (value != imageSize) {
                _imageSize = value
                postInvalidate()
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        points?.apply {
            val sw = width.toFloat() / imageSize.width.toFloat()
            val sh = height.toFloat() / imageSize.height.toFloat()
            val x0 = this[0].x.toFloat() * sw
            val y0 = this[0].y.toFloat() * sh
            val x1 = this[1].x.toFloat() * sw
            val y1 = this[1].y.toFloat() * sh
            val x2 = this[2].x.toFloat() * sw
            val y2 = this[2].y.toFloat() * sh
            val x3 = this[3].x.toFloat() * sw
            val y3 = this[3].y.toFloat() * sh
            canvas.drawLines(
                floatArrayOf(
                    x0, y0, x1, y1,
                    x1, y1, x2, y2,
                    x2, y2, x3, y3,
                    x3, y3, x0, y0
                ), paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            max(suggestedMinimumWidth, MeasureSpec.getSize(widthMeasureSpec)),
            max(suggestedMinimumHeight, MeasureSpec.getSize(heightMeasureSpec))
        )
    }

    override fun onAugmentData(sender: ScanThread, points: Array<Point>) {
        this.points = points
        postInvalidate()
    }

    override fun onAugmentLost(sender: ScanThread) {
        points = null
        postInvalidate()
    }

    override fun onBitmapSaved(bitmap: Bitmap) {
        Log.d("ScanAugmentView", "Image scanned.")
        animation.start()
    }

    private fun init(context: Context) {
        paint.color = context.getColor(R.color.scan_augment_box)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 20f
        paint.style = Paint.Style.STROKE
        animation = context.getDrawable(R.drawable.scan_glow) as AnimatedVectorDrawable;
        background = animation
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }
}
