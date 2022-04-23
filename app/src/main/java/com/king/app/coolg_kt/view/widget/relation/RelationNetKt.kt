package com.king.app.coolg_kt.view.widget.relation

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.utils.DebugLog
import java.util.ArrayList

class RelationNetKt : ViewGroup {
    private val pointList: MutableList<Point> = ArrayList()

    var adapter: NetAdapter? = null
    set(value) {
        field = value
        removeAllViews()
        value?.apply {
            for (i in 0 until getCount()) {
                addView(getView(this@RelationNetKt, i))
            }
        }
    }

    var paint = Paint()

    val colors = arrayOf(
        Color.RED, Color.CYAN, Color.DKGRAY, Color.GREEN,
        Color.BLUE, Color.YELLOW
    )

    constructor(context: Context?) : super(context) { initParams() }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { initParams() }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) { initParams() }

    private fun initParams() {
        setWillNotDraw(false)
    }

    override fun addView(child: View?) {
        super.addView(child)
    }

    private fun definePosition() {
        pointList.clear()
        if (width > height) {
            defineLandscape()
        } else {
            defineVertical()
        }
    }

    private fun defineLandscape() {
        pointList.add(Point(width / 2, height / 2))
    }

    private fun defineVertical() {
        pointList.add(Point(width / 3, height / 2))
        pointList.add(Point(width / 3 * 2, height / 2))
        pointList.add(Point(width / 4 * 2, height / 6 * 2))
        pointList.add(Point(width / 4 * 2, height / 6 * 4))
        pointList.add(Point(width / 4, height / 6 * 2))
        pointList.add(Point(width / 4 * 3, height / 6 * 4))
        pointList.add(Point(width / 4 * 3, height / 6 * 2))
        pointList.add(Point(width / 4, height / 6 * 4))
        pointList.add(Point(width / 5 * 2, height / 6))
        pointList.add(Point(width / 5 * 3, height / 6))
        pointList.add(Point(width / 5 * 2, height / 6 * 5))
        pointList.add(Point(width / 5 * 3, height / 6 * 5))
        pointList.add(Point(width / 5, height / 6))
        pointList.add(Point(width / 5 * 4, height / 6))
        pointList.add(Point(width / 5, height / 6 * 5))
        pointList.add(Point(width / 5 * 4, height / 6 * 5))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        DebugLog.e()
        //        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        val count = this.childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            val lp = child.layoutParams as MarginLayoutParams
            // 必须在onMeasure里对child进行测量，在onLayout中才能通过getMeasuredWidth，getMeasuredHeight获取到值
            child.measure(
                getChildMeasureSpec(
                    widthMeasureSpec,
                    this.paddingLeft + this.paddingRight,
                    lp.width
                ),
                getChildMeasureSpec(
                    heightMeasureSpec,
                    this.paddingTop + this.paddingBottom,
                    lp.height
                )
            )
        }
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        DebugLog.e("getWidth=$width, getHeight=$height")
        definePosition()
        for (i in 0 until childCount) {
            if (i < pointList.size) {
                val view = getChildAt(i)
                val point = pointList[i]
                // 用child的getWidth()和getHeight()，在初始化时始终为0
                // 只能用getMeasuredWidth/Height，并且提前在onMeasure里对child进行测量
                val xHalf = view.measuredWidth / 2
                val yHalf = view.measuredHeight / 2
                val rect = Rect(point.x - xHalf, point.y - yHalf, point.x + xHalf, point.y + yHalf)
                DebugLog.e("layout i=$i, rect=$rect")
                view.layout(rect.left, rect.top, rect.right, rect.bottom)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        adapter?.apply {
            for (i in 0 until getCount()) {
                if (i < pointList.size) {
                    val point = pointList[i]
                    getRelatedPositions(i).forEach {
                        val targetPoint = if (it < pointList.size) {
                            pointList[it]
                        }
                        else {
                            null
                        }
                        targetPoint?.apply {
                            linePoints(canvas, i, point, it, this)
                        }
                    }
                }
            }
        }
        super.onDraw(canvas)
    }

    /**
     * 每个item取8个锚点，目前只支持childView限定的矩形作为边界，8个点为4个顶点与每条边的4个中点
     */
    private fun linePoints(canvas: Canvas, startPosition: Int, start: Point, endPosition: Int, end: Point) {
        if (startPosition >= childCount || endPosition >= childCount) {
            return
        }
        val startView = getChildAt(startPosition)
        val startRect = Rect(start.x - startView.width / 2, start.y - startView.height / 2,
            start.x + startView.width / 2, start.y + startView.height / 2)
        val endView = getChildAt(endPosition)
        val endRect = Rect(end.x - endView.width / 2, end.y - endView.height / 2,
            end.x + endView.width / 2, end.y + endView.height / 2)
        val anchors = defineAnchor(startRect, endRect)
        val anchorStart = getAnchorPoint(startRect, anchors[0])
        val anchorEnd = getAnchorPoint(endRect, anchors[1])
        paint.color = colors[startPosition % colors.size]
        paint.strokeWidth = 10F
        canvas.drawPoint(anchorStart.x.toFloat(), anchorStart.y.toFloat(), paint)
        canvas.drawPoint(anchorEnd.x.toFloat(), anchorEnd.y.toFloat(), paint)
        paint.strokeWidth = 3F
        canvas.drawLine(anchorStart.x.toFloat(), anchorStart.y.toFloat(), anchorEnd.x.toFloat(), anchorEnd.y.toFloat(), paint)
    }

    private fun getAnchorPoint(rect: Rect, anchor: Anchor): Point {
        return when(anchor) {
            Anchor.LEFT_TOP -> Point(rect.left, rect.top)
            Anchor.TOP_CENTER -> Point(rect.left + rect.width() / 2, rect.top)
            Anchor.RIGHT_TOP -> Point(rect.right, rect.top)
            Anchor.LEFT_CENTER -> Point(rect.left, rect.top + rect.height() / 2)
            Anchor.RIGHT_CENTER -> Point(rect.right, rect.top + rect.height() / 2)
            Anchor.LEFT_BOTTOM -> Point(rect.left, rect.bottom)
            Anchor.BOTTOM_CENTER -> Point(rect.left + rect.width() / 2, rect.bottom)
            Anchor.RIGHT_BOTTOM -> Point(rect.right, rect.bottom)
        }
    }

    private fun defineAnchor(start: Rect, end: Rect): Array<Anchor> {
        var startAnchor = Anchor.LEFT_TOP
        var endAnchor = Anchor.LEFT_TOP
        // target在start右边
        if (end.left > start.right) {
            // 完全右上方
            if (end.bottom < start.top) {
                startAnchor = Anchor.RIGHT_TOP
                // 一个身位以内，取底边中心；超出才取左下角
                endAnchor = if (end.left > start.right + start.width()) {
                    Anchor.LEFT_BOTTOM
                }
                else {
                    Anchor.BOTTOM_CENTER
                }
            }
            // start水平延伸后与target有重叠
            else if (end.bottom >= start.top && end.top <= start.bottom) {
                startAnchor = Anchor.RIGHT_CENTER
                endAnchor = Anchor.LEFT_CENTER
            }
            // 完全右下方
            else {
                startAnchor = Anchor.RIGHT_BOTTOM
                // 一个身位以内，取顶边中心；超出才取左上角
                endAnchor = if (end.left > start.right + start.width()) {
                    Anchor.LEFT_TOP
                }
                else {
                    Anchor.TOP_CENTER
                }
            }
        }
        // target在start垂直方向延伸后有重叠
        else if (end.left <= start.right && end.right >= start.left) {
            // 完全上方
            if (end.bottom < start.top) {
                startAnchor = Anchor.TOP_CENTER
                endAnchor = Anchor.BOTTOM_CENTER
            }
            // 不存在重叠的情况
            // 完全下方
            else {
                startAnchor = Anchor.BOTTOM_CENTER
                endAnchor = Anchor.TOP_CENTER
            }
        }
        // target在start左边
        else {
            // 完全左上方
            if (end.bottom < start.top) {
                startAnchor = Anchor.LEFT_TOP
                endAnchor = Anchor.RIGHT_BOTTOM
                // 一个身位以内，取底边中心；超出才取右下角
                endAnchor = if (end.left > start.right + start.width()) {
                    Anchor.RIGHT_BOTTOM
                }
                else {
                    Anchor.BOTTOM_CENTER
                }
            }
            // start水平延伸后与target有重叠
            else if (end.bottom >= start.top && end.top <= start.bottom) {
                startAnchor = Anchor.LEFT_CENTER
                endAnchor = Anchor.RIGHT_CENTER
            }
            // 完全左下方
            else {
                startAnchor = Anchor.LEFT_BOTTOM
                // 一个身位以内，取顶边中心；超出才取右上角
                endAnchor = if (end.left > start.right + start.width()) {
                    Anchor.RIGHT_TOP
                }
                else {
                    Anchor.TOP_CENTER
                }
            }
        }
        return arrayOf(startAnchor, endAnchor)
    }
}

enum class Anchor {
    LEFT_TOP, TOP_CENTER, RIGHT_TOP, LEFT_CENTER, RIGHT_CENTER, LEFT_BOTTOM, BOTTOM_CENTER, RIGHT_BOTTOM
}

abstract class NetAdapter {
    abstract fun getRelatedPositions(position: Int): List<Int>
    abstract fun getCount(): Int
    abstract fun getView(parent: View, position: Int): View
}