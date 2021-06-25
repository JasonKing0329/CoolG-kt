package com.king.app.coolg_kt.view.widget.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.ScreenUtils;
import com.king.app.coolg_kt.view.widget.chart.adapter.BarChartAdapter;

/**
 * Desc:bar chart属于x轴等分，y轴权值不固定的图表
 *
 * @author：Jing Yang
 * @date: 2018/7/3 16:26
 */
public class BarChart extends AxisChart {

    private BarChartAdapter mAdapter;

    private int mBarWidth = ScreenUtils.dp2px(50);

    private int mBarGap = ScreenUtils.dp2px(6);

    private int mValueTextSize = ScreenUtils.dp2px(10);

    private int mValueTextColor = Color.parseColor("#333333");

    private boolean mDrawValueText;

    public BarChart(Context context) {
        super(context);
        init(null);
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BarChart);
        mBarWidth = a.getDimensionPixelSize(R.styleable.BarChart_barWidth, ScreenUtils.dp2px(50));
        mBarGap = a.getDimensionPixelSize(R.styleable.BarChart_barGap, ScreenUtils.dp2px(5));
    }

    public void setDrawValueText(boolean mDrawValueText) {
        this.mDrawValueText = mDrawValueText;
    }

    public void setAdapter(BarChartAdapter mAdapter) {
        this.mAdapter = mAdapter;
        invalidate();
        requestLayout();
    }

    @Override
    protected int measureChartWidth(int defaultWidth) {
        if (mAdapter != null) {
            defaultWidth = (mBarWidth + mBarGap) * mAdapter.getXCount();
        }
        return defaultWidth;
    }

    @Override
    protected int measureChartHeight(int defaultHeight) {
        if (mAdapter != null) {
            defaultHeight = mXAxisTextHeight;
        }
        return defaultHeight;
    }

    @Override
    protected void drawContent(Canvas canvas) {
        if (mAdapter != null) {
            int startY = mOriginPoint.y;
            int startX = mOriginPoint.x;
            int height = startY - getPaddingTop();
            // 刻度区域的总宽度（不包括Y周及其文字，X轴的两端延长线）
            int width = getWidth() - getPaddingRight() - startX - mAxisLineXExtend * 2;

            for (int i = 0; i < mAdapter.getXCount(); i ++) {
                mPaint.setColor(mAdapter.getBarColor(i));
                mPaint.setStyle(Paint.Style.FILL);
                // draw bar 居中显示
                int degreeLeft = startX + mAxisLineXExtend + (int) ((float) axisX.getWeightAt(i) / (float) axisX.getTotalWeight() * width);
                int valueHeight = (int) ((float) mAdapter.getValueWeight(i) / (float) axisY.getTotalWeight() * height);
                int left = mBarGap / 2 + degreeLeft;
                int top = startY - valueHeight;
                int right = left + mBarWidth;
                int bottom = startY;
                canvas.drawRect(left, top, right, bottom, mPaint);
                // draw text
                drawValueText(mAdapter.getValueText(i), left, top, right, canvas);
            }
        }
    }

    /**
     * 绘制bar上面的value文字
     * @param valueText
     * @param left
     * @param top
     * @param right
     * @param canvas
     */
    private void drawValueText(String valueText, int left, int top, int right, Canvas canvas) {
        if (mDrawValueText && !TextUtils.isEmpty(valueText)) {
            // 居中显示文字

            Paint textPaint = new Paint();
            textPaint.setColor(mValueTextColor);
            textPaint.setTextSize(mValueTextSize);
            textPaint.setStyle(Paint.Style.FILL);
            //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
            textPaint.setTextAlign(Paint.Align.CENTER);

            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float textTop = fontMetrics.top;//为基线到字体上边框的距离
            float textBottom = fontMetrics.bottom;//为基线到字体下边框的距离

            int baseLine = (int) (top - textBottom - ScreenUtils.dp2px(5));//基线中间点的y轴计算公式

            int centerX = left + (right - left) / 2;
            canvas.drawText(valueText, centerX, baseLine, textPaint);
        }
    }

    /**
     * 重写X轴刻度文字，与bar一样居中显示
     * @param text
     * @param textX
     * @param textY
     * @param canvas
     */
    @Override
    protected void drawDegreeXText(String text, int textX, int textY, Canvas canvas) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(ScreenUtils.dp2px(12));
        textPaint.setColor(mAxisLineColor);

        // StaticLayout只能画在canvas的0,0上，因此必须通过translate画布实现，注意save和restore
        StaticLayout layout = new StaticLayout(text, textPaint
                , mBarWidth + mBarGap, Layout.Alignment.ALIGN_CENTER, 1.0f, 0f, true);
        canvas.save();
        canvas.translate(textX, textY);
        layout.draw(canvas);
        canvas.restore();
    }

    @Override
    protected boolean isDrawDegreeX(int position) {
        return true;
    }
}
