package com.king.app.coolg_kt.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.king.app.coolg_kt.utils.ScreenUtils;

/**
 * Desc: champion最左，runner-up右上方，两个sf右下方
 * 全部保持一样的宽高比
 *
 * @author：Jing Yang
 * @date: 2021/4/26 14:00
 */
public class SemiGroup extends LinearLayout {

    /**
     * 宽高比
     */
    private float ratio = 1.8f;

    public SemiGroup(Context context) {
        super(context);
        init();
    }

    public SemiGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SemiGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SemiGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    /**
     *
     * 4个image的宽高根据4元一次方程确定：
     *         假设宽高比为n（即width/height=n）
     *         代表SF的image宽高分别为x, y
     *         代表Champion的image宽高分别为x1, y1
     *         那么，代表Runner-up的宽高为2x, y1-y
     *         1. x = n*y
     *         2. 2x = n*(y1-y)
     *         3. x1 = n* y1
     *         4. x1 + 2x = w(w即layoutWidth)
     *  最后得出公式：
     *  x=w/5
     *  y=w/(5*n)
     *  x1=3*w/5
     *  y1=3*w/(5*n)
     * @param adapter
     */
    public void setAdapter(SemiAdapter adapter) {
        removeAllViews();
        int layoutWidth = ScreenUtils.getScreenWidth();
        int xSF = layoutWidth / 5;
        int ySF = (int) (layoutWidth / (5 * ratio));
        int xCha = 3 * xSF;
        int yCha = 3 * ySF;
        int xRU = 2 * xSF;
        int yRU = yCha - ySF;
        View champion = adapter.getView(0);
        addView(champion, xCha, yCha);

        LinearLayout groupRight = new LinearLayout(getContext());
        groupRight.setOrientation(VERTICAL);
        addView(groupRight, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        View runnerup = adapter.getView(1);
        groupRight.addView(runnerup, xRU, yRU);

        LinearLayout groupBottom = new LinearLayout(getContext());
        groupBottom.setOrientation(HORIZONTAL);
        groupRight.addView(groupBottom, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        View sf1 = adapter.getView(2);
        groupBottom.addView(sf1, xSF, ySF);
        View sf2 = adapter.getView(3);
        groupBottom.addView(sf2, xSF, ySF);
    }

    public static abstract class SemiAdapter {

        public abstract View getView(int position);
    }
}
