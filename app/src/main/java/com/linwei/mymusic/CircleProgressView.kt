package com.linwei.mymusic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class CircleProgressView(context: Context, attrs: AttributeSet) :
    View(context, attrs) {
    private var InnerCircleColor = Color.WHITE
    private var OutCircleColor = Color.RED
    private var CenterShapeColor = Color.GRAY

    private var boundwidth = 0f
    private var progress = 0f
    private var max = 100f
    private var isPlaying = false

    //画笔定义
    private val InPaint: Paint = Paint()
    private val OutPaint = Paint()
    private val tangePaint = Paint()
    private val linePaint = Paint()

    var onStatePlayListener: OnStatePlayListener? = null

    init {
        //下层圆环画笔
        InPaint.isAntiAlias = true
        InPaint.color = InnerCircleColor
        InPaint.style = Paint.Style.STROKE

        //上层圆环画笔
        OutPaint.isAntiAlias = true
        OutPaint.color = OutCircleColor
        OutPaint.style = Paint.Style.STROKE
        //三角形画笔
        tangePaint.isAntiAlias = true
        tangePaint.color = CenterShapeColor

        linePaint.isAntiAlias = true
        linePaint.strokeWidth = 3f
        linePaint.color = Color.BLACK
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //给dp值
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(
            if (width > height) height else width,
            if (width > height) height else width
        )
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //下层圆环
        boundwidth = (width / 2f) * 0.05f
        InPaint.strokeWidth = boundwidth
//        val rectF=RectF(boundwidth/2,boundwidth/2,width-boundwidth/2,height-boundwidth/2)
//        canvas.drawArc(rectF,0f,360f,false,InPaint)
        canvas.drawCircle(width / 2f, height / 2f, width / 2f - boundwidth / 2, InPaint)

        //上层圆环
        OutPaint.strokeWidth = boundwidth
        if (max == 0f) return
        val sweepAngle = progress / max
        val rectF =
            RectF(boundwidth / 2, boundwidth / 2, width - boundwidth / 2, height - boundwidth / 2)
        canvas.drawArc(rectF, 270f, sweepAngle * 360f, false, OutPaint)

        //绘制三角形
        val bian = width / 3
        val path = Path()
        val ping = bian * bian - (bian / 2.0) * (bian / 2.0)
        val radiu = sqrt(ping).toFloat()
        if (!isPlaying) {
            path.moveTo(width / 2f + radiu * 2 / 3, height / 2f)
            path.lineTo(width / 2f - radiu / 3, height / 2f - radiu / 2)
            path.lineTo(width / 2f - radiu / 3, height / 2f + radiu / 2)
            path.close()
            canvas.drawPath(path, tangePaint)
        }


        //坐标线
//        canvas.drawLine(width/2f,0f,width/2f,height.toFloat(),linePaint)
//        canvas.drawLine(0f,height/2f,width.toFloat(),height/2f,linePaint)
        //canvas.drawLine(width/2f+radiu,height/2f,width/2f-radiu,3*height/4f,linePaint)
        //矩形
        else {
            canvas.drawRect(
                width / 2f - bian / 2f, height / 2f - bian / 2f, (width + bian) / 2f, (height
                        + bian) / 2f, tangePaint
            )
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (onStatePlayListener!=null){
                isPlaying = !isPlaying
                invalidate()
                if (isPlaying) {//true正在播放
                    onStatePlayListener?.toPlay()
                } else {
                    onStatePlayListener?.toPause()
                }
            }
        }
        return super.onTouchEvent(event)
    }


    interface OnStatePlayListener {
        fun toPlay() {}
        fun toPause() {}
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    fun setMaxProgress(max: Float) {
        this.max = max
        invalidate()
    }

    fun setPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        invalidate()
    }

}