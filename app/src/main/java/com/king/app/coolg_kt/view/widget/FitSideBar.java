package com.king.app.coolg_kt.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/5/22 9:30
 */
public class FitSideBar extends View {

    private int mTextSize;
    private int mTextColor;
    private int mTextSizeFocus;
    private int mTextColorFocus;
    private int mMaxWidth;
    private int mMinWidth;
    private int mMinMarginHor;
    private int mGravity;

    private List<String> indexList;

    private Paint mPaint;

    private int mSelection;

    private OnSideIndexChangedListener onSideIndexChangedListener;

    private OnSidebarStatusListener onSidebarStatusListener;

    public FitSideBar(Context context) {
        super(context);
        init(null);
    }

    public FitSideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        indexList = new ArrayList<>();
        mPaint = new Paint();
        mSelection = -1;

        if (attrs == null) {
            mTextSize = ScreenUtils.dp2px(14);
            mTextSizeFocus = ScreenUtils.dp2px(16);
            mTextColor = Color.parseColor("#3F51B5");
            mTextColorFocus = Color.parseColor("#FF4081");
            mMinWidth = ScreenUtils.dp2px(22);
            mMaxWidth = -1;// 不限
            mMinMarginHor = ScreenUtils.dp2px(1);
            mGravity = Gravity.CENTER;
        }
        else {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs
                    , R.styleable.FitSideBar, 0, 0);
            mTextSize = a.getDimensionPixelSize(R.styleable.FitSideBar_textSize, ScreenUtils.dp2px(14));
            mTextColor = a.getColor(R.styleable.FitSideBar_textColor, Color.parseColor("#3F51B5"));
            mTextSizeFocus = a.getDimensionPixelSize(R.styleable.FitSideBar_textSizeFocus, ScreenUtils.dp2px(16));
            mTextColorFocus = a.getColor(R.styleable.FitSideBar_textColorFocus, Color.parseColor("#FF4081"));
            mMinWidth = a.getDimensionPixelSize(R.styleable.FitSideBar_minWidth, ScreenUtils.dp2px(22));
            mMaxWidth = a.getDimensionPixelSize(R.styleable.FitSideBar_maxWidth, -1);
            mMinMarginHor = a.getDimensionPixelSize(R.styleable.FitSideBar_minHorizontalPadding, ScreenUtils.dp2px(1));
            mGravity = a.getInteger(R.styleable.FitSideBar_android_gravity, Gravity.CENTER);
            a.recycle();
        }
    }

    public void addIndex(String index) {
        indexList.add(index);
    }

    public void clear() {
        indexList.clear();
        invalidate();
    }

    public void build() {
        //如果动态添加、清空过index，需要执行requestLayout()才能更新布局宽高
        requestLayout();
        invalidate();
    }

    public void setOnSideIndexChangedListener(OnSideIndexChangedListener onSideIndexChangedListener) {
        this.onSideIndexChangedListener = onSideIndexChangedListener;
    }

    public void setOnSidebarStatusListener(OnSidebarStatusListener onSidebarStatusListener) {
        this.onSidebarStatusListener = onSidebarStatusListener;
    }

    public void setGravity(int mGravity) {
        this.mGravity = mGravity;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getFitWidth(widthMeasureSpec);
        setMeasuredDimension(width, heightMeasureSpec);
    }

    private int getFitWidth(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int width = size;
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:// 没有指定大小
            case MeasureSpec.AT_MOST:// wrap_content
                // 这两种情况将宽度限制在max与min之间，并符合text的最大size
                width = mMinWidth;
                for (int i = 0; i < indexList.size(); i++) {
                    mPaint.setTextSize(mTextSize);
                    // 只能得到宽度，数值比rect取到的略大
//                    float textWidth = mPaint.measureText(indexList.get(i));
                    Rect rect = getTextRect(indexList.get(i));
                    if (rect.width() + 2 * mMinMarginHor > width) {
                        width = rect.width() + 2 * mMinMarginHor;
                    }
                }
                if (mMaxWidth != -1 && width > mMaxWidth) {
                    width = mMaxWidth;
                }
                break;
                // match_parent或者指定了大小
            case MeasureSpec.EXACTLY:
                width = size;
                break;
        }
        return width;
    }

    private Rect getTextRect(String text) {
        Rect rect = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int width = getWidth();

        if (indexList.size() == 0) {
            return;
        }
        int singleHeight = height / indexList.size();
        // singleHeight取的整数，所以再乘以数量后肯定小于等于height
        int extraVertical = height - singleHeight * indexList.size();
        // 根据extraVertical进行整体下移
        int offsetVertical = extraVertical + getPaddingTop();

        for (int i = 0; i < indexList.size(); i++) {
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mPaint.setAntiAlias(true);
            mPaint.setColor(mTextColor);
            mPaint.setTextSize(mTextSize);
            if (i == mSelection) {
                mPaint.setColor(mTextColorFocus);
                mPaint.setTextSize(mTextSizeFocus);
                mPaint.setFakeBoldText(true);
            }
            float xPos;
            Rect rect = getTextRect(indexList.get(i));
            if (mGravity == Gravity.LEFT) {
                xPos = 0;
            }
            else if (mGravity == Gravity.RIGHT) {
                xPos = width - rect.width() - 2 * mMinMarginHor;
            }
            // center as default
            else {
                xPos = width / 2 - rect.width() / 2;
            }
            // 纵向居中
            float offsetY = singleHeight / 2 - rect.height() / 2;
            // index的实际y位置
            float yPos = singleHeight * i + offsetY + offsetVertical;
            canvas.drawText(indexList.get(i), xPos, yPos, mPaint);
            mPaint.reset();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = mSelection;
        final int c = (int) (y / getHeight() * indexList.size());

        switch (action) {
            case MotionEvent.ACTION_UP:
                mSelection = -1;//
                invalidate();
                if (onSidebarStatusListener != null) {
                    onSidebarStatusListener.onChangeFinished();
                }
                break;

            default:
                if (oldChoose != c) {
                    if (c >= 0 && c < indexList.size()) {
                        if (onSideIndexChangedListener != null) {
                            onSideIndexChangedListener.onSideIndexChanged(indexList.get(c));
                        }
                        if (onSidebarStatusListener != null) {
                            onSidebarStatusListener.onSideIndexChanged(indexList.get(c));
                        }

                        mSelection = c;
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    public interface OnSideIndexChangedListener {
        void onSideIndexChanged(String index);
    }

    public interface OnSidebarStatusListener extends OnSideIndexChangedListener {
        void onChangeFinished();
    }
}
