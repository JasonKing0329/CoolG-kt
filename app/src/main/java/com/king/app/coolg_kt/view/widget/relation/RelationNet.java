package com.king.app.coolg_kt.view.widget.relation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.king.app.coolg_kt.utils.DebugLog;

import java.util.ArrayList;
import java.util.List;

public class RelationNet extends ViewGroup {

    private List<Point> pointList = new ArrayList<>();

    public RelationNet(Context context) {
        super(context);
    }

    public RelationNet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelationNet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void definePosition() {
        pointList.clear();
        if (getWidth() > getHeight()) {
            defineLandscape();
        }
        else {
            defineVertical();
        }
    }

    private void defineLandscape() {
        pointList.add(new Point(getWidth()/2, getHeight()/2));
    }

    private void defineVertical() {
        pointList.add(new Point(getWidth()/3, getHeight()/2));
        pointList.add(new Point(getWidth()/3 * 2, getHeight()/2));
        pointList.add(new Point(getWidth()/4 * 2, getHeight()/6 * 2));
        pointList.add(new Point(getWidth()/4 * 2, getHeight()/6 * 4));
        pointList.add(new Point(getWidth()/4, getHeight()/6 * 2));
        pointList.add(new Point(getWidth()/4 * 3, getHeight()/6 * 4));
        pointList.add(new Point(getWidth()/4 * 3, getHeight()/6 * 2));
        pointList.add(new Point(getWidth()/4, getHeight()/6 * 4));
        pointList.add(new Point(getWidth()/5 * 2, getHeight()/6));
        pointList.add(new Point(getWidth()/5 * 3, getHeight()/6));
        pointList.add(new Point(getWidth()/5 * 2, getHeight()/6 * 5));
        pointList.add(new Point(getWidth()/5 * 3, getHeight()/6 * 5));
        pointList.add(new Point(getWidth()/5, getHeight()/6));
        pointList.add(new Point(getWidth()/5 * 4, getHeight()/6));
        pointList.add(new Point(getWidth()/5, getHeight()/6 * 5));
        pointList.add(new Point(getWidth()/5 * 4, getHeight()/6 * 5));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DebugLog.e();
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 必须在onMeasure里对child进行测量，在onLayout中才能通过getMeasuredWidth，getMeasuredHeight获取到值
            child.measure(
                    getChildMeasureSpec(widthMeasureSpec, this.getPaddingLeft() + this.getPaddingRight(), lp.width),
                    getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), lp.height)
            );
        }
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        DebugLog.e("getWidth=" + getWidth() + ", getHeight=" + getHeight());
        definePosition();
        for (int i = 0; i < getChildCount(); i ++) {
            if (i < pointList.size()) {
                View view = getChildAt(i);
                Point point = pointList.get(i);
                // 用child的getWidth()和getHeight()，在初始化时始终为0
                // 只能用getMeasuredWidth/Height，并且提前在onMeasure里对child进行测量
                int xHalf = view.getMeasuredWidth() / 2;
                int yHalf = view.getMeasuredHeight() / 2;
                Rect rect = new Rect(point.x - xHalf, point.y - yHalf, point.x + xHalf, point.y + yHalf);
                DebugLog.e("layout i=" + i + ", rect=" + rect);
                view.layout(rect.left, rect.top, rect.right, rect.bottom);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
