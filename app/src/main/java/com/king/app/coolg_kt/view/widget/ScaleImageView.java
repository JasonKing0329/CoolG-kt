package com.king.app.coolg_kt.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.king.app.coolg_kt.R;

/**
 * Desc:固定比例的imageView，支持自定义比例、以高为基准或以宽为基准
 *
 * @author：Jing Yang
 * @date: 2018/9/5 17:26
 */
public class ScaleImageView extends AppCompatImageView {

    private int mRatioWidth = 4;

    private int mRatioHeight = 3;

    private int mWidth;
    private int mHeight;

    private boolean mBaseOnHeight;
    private boolean isVertical;

    public ScaleImageView(Context context) {
        super(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ScaleImageView);
        mRatioWidth = a.getInteger(R.styleable.ScaleImageView_ratioWidth, 4);
        mRatioHeight = a.getInteger(R.styleable.ScaleImageView_ratioHeight, 3);
        mBaseOnHeight = a.getBoolean(R.styleable.ScaleImageView_baseOnHeight, false);
    }

    public void setBaseOnHeight(boolean mBaseOnHeight) {
        this.mBaseOnHeight = mBaseOnHeight;
    }

    public void setVertical(boolean vertical) {
        isVertical = vertical;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int wSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int hMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int hSize = View.MeasureSpec.getSize(heightMeasureSpec);
        switch (wMode) {
            case View.MeasureSpec.EXACTLY:
                mWidth = wSize;
                break;
            case View.MeasureSpec.AT_MOST:
                mWidth = wSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
                break;
        }
        switch (hMode) {
            case View.MeasureSpec.EXACTLY:
                mHeight = hSize;
                break;
            case View.MeasureSpec.AT_MOST:
                mHeight = hSize;
                break;
            case View.MeasureSpec.UNSPECIFIED:
                break;
        }
        // 以高为基准，重新计算宽度
        if (mBaseOnHeight) {
            // 反向宽高比
            if (isVertical) {
                mWidth = mHeight * mRatioHeight / mRatioWidth;
            }
            // 宽高比
            else {
                mWidth = mHeight * mRatioWidth / mRatioHeight;
            }
        }
        // 以宽为基准，重新计算高度
        else {
            // 反向宽高比
            if (isVertical) {
                mHeight = mWidth * mRatioWidth / mRatioHeight;
            }
            // 宽高比
            else {
                mHeight = mWidth * mRatioHeight / mRatioWidth;
            }
        }
//        DebugLog.e("mBaseOnHeight " + mBaseOnHeight + ", mWidth=" + mWidth + ", mHeight=" + mHeight);
        setMeasuredDimension(mWidth, mHeight);
    }
}
