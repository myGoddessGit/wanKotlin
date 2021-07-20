package com.xll.wanstudy.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.IntentSender
import android.graphics.*
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.just.agentweb.AgentWebUtils
import com.just.agentweb.BaseIndicatorSpec
import com.just.agentweb.BaseIndicatorView
import kotlin.math.min

/**
 * @author cyl
 * @date 2021/7/12
 */
class GradientWebIndicator @JvmOverloads constructor(
    context: Context,attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
) : BaseIndicatorView(context, attrs, defStyleAttr), BaseIndicatorSpec {

    /**
     * 进度条的画笔
     */
    private var mPaint : Paint? = null

    /**
     * 进度条动画
     */
    private var mAnimator : Animator? = null

    /**
     * 控件的宽度
     */
    private var mTargetWidth  = 0

    /**
     * 当前匀速动画最大的时长
     */
    private var mCurrentMaxUniformSpeedDuration = MAX_UNIFORM_SPEED_DURATION

    /**
     * 当前加速后减速动画的最大时长
     */
    private var mCurrentMaxDecelerateSpeedDuration = MAX_DECELERATE_SPEED_DURATION

    /**
     * 结束动画时长
     */
    private var mCurrentDoEndAnimationDuration = DO_END_ANIMATION_DURATION

    /**
     * 当前进度条的状态
     */
    private var indicatorStatus =0
    private var mCurrentProgress = 0f

    private var mWebIndicatorDefaultHeight = 3

    private fun init(context: Context){
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        mPaint!!.strokeCap = Paint.Cap.SQUARE
        mTargetWidth = context.resources.displayMetrics.widthPixels
        mWebIndicatorDefaultHeight = AgentWebUtils.dp2px(context, 1f)

        /**
         * 渐变色画笔
         */
        val linearGradient = LinearGradient(0f, 0f, mTargetWidth.toFloat()
        , 0f, intArrayOf(Color.RED, Color.YELLOW, Color.BLUE), null, Shader.TileMode.CLAMP)

        mPaint!!.shader = linearGradient
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        var w = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        var h = MeasureSpec.getSize(heightMeasureSpec)

        if (wMode == MeasureSpec.AT_MOST){
            w = min(w, context.resources.displayMetrics.widthPixels)
        }

        if (hMode == MeasureSpec.AT_MOST){
            h = mWebIndicatorDefaultHeight
        }

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {

    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mCurrentProgress / 100 * this.width.toFloat()
        , this.height.toFloat(), mPaint!!)
    }

    override fun show() {
        if (visibility == View.GONE){
            this.visibility = View.VISIBLE
            mCurrentProgress = 0f

        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mTargetWidth = measuredWidth
        val screenWidth = context.resources.displayMetrics.widthPixels
        if (mTargetWidth >= screenWidth){
            mCurrentMaxDecelerateSpeedDuration = MAX_DECELERATE_SPEED_DURATION
            mCurrentMaxUniformSpeedDuration = MAX_UNIFORM_SPEED_DURATION
            mCurrentDoEndAnimationDuration = MAX_DECELERATE_SPEED_DURATION
        } else {
            // 取比值
            val rate = mTargetWidth / screenWidth.toFloat()
            mCurrentMaxUniformSpeedDuration = (MAX_UNIFORM_SPEED_DURATION * rate).toInt()
            mCurrentMaxDecelerateSpeedDuration = (MAX_DECELERATE_SPEED_DURATION * rate).toInt()
            mCurrentDoEndAnimationDuration = (DO_END_ANIMATION_DURATION * rate).toInt()
        }
    }

    private fun startAnim(isFinished: Boolean){
        val v: Float = if (isFinished) 100f else 95F
        if (mAnimator != null && mAnimator!!.isStarted){
            mAnimator!!.cancel()
        }
        mCurrentProgress = if (mCurrentProgress == 0f) 0.00000001f else mCurrentProgress

        if (!isFinished){
            val animatorSet = AnimatorSet()
            val p1 = v*0.6F
            val animator = ValueAnimator.ofFloat(mCurrentProgress, p1)
            val animator0 = ValueAnimator.ofFloat(p1, v)
            val residue = 1f - mCurrentProgress / 100 - 0.05f
            val duration = (residue * mCurrentMaxUniformSpeedDuration).toLong()
            val duration6 = (duration * 0.6f).toLong()
            val duration4 = (duration * 0.4f).toLong()
            animator.interpolator = LinearInterpolator()
            animator.duration = duration4
            animator.addUpdateListener(mAnimatorUpdateListener)
            animator0.interpolator = LinearInterpolator()
            animator0.duration = duration6
            animator0.addUpdateListener(mAnimatorUpdateListener)
            animatorSet.play(animator0).after(animator)
            animatorSet.start()
            mAnimator = animatorSet
        } else {
            var segment95Animator : ValueAnimator? = null
            if (mCurrentProgress < 95f){
                segment95Animator = ValueAnimator.ofFloat(mCurrentProgress, 95f)
                val residue = 1f - mCurrentProgress / 100f - 0.05f
                segment95Animator.duration = (residue * mCurrentMaxDecelerateSpeedDuration).toLong()
                segment95Animator.interpolator = DecelerateInterpolator()
                segment95Animator.addUpdateListener(mAnimatorUpdateListener)
            }
        }
    }

    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
        animation -> mCurrentProgress =
            animation.animatedValue as Float
        this@GradientWebIndicator.invalidate()
    }

    private val mAnimatorListenerAdapter : AnimatorListenerAdapter = object : AnimatorListenerAdapter(){
        override fun onAnimationEnd(animation: Animator?) {
            doEnd()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAnimator != null && mAnimator!!.isStarted){
            mAnimator!!.cancel()
            mAnimator = null
        }
    }

    private fun doEnd(){
        if (indicatorStatus == FINISH && mCurrentProgress == 100f){
            visibility = View.GONE
            mCurrentProgress = 0f
            this.alpha = 1f
        }
        indicatorStatus = UN_START
    }

    override fun reset() {
        super.reset()
    }

    override fun setProgress(newProgress: Int) {
        super.setProgress(newProgress)
    }

    override fun offerLayoutParams(): LayoutParams {
        TODO("Not yet implemented")
    }

    companion object {
        /**
         * 默认匀速动画最大的时长
         */
        const val MAX_UNIFORM_SPEED_DURATION = 8 * 1000

        /**
         * 默认加速后减速动画最大时长
         */
        const val MAX_DECELERATE_SPEED_DURATION = 450

        /**
         * 结束动画时长 ， Fade out 。
         */
        const val DO_END_ANIMATION_DURATION = 600
        const val UN_START = 0
        const val STARTED = 1
        const val FINISH = 2
    }

}