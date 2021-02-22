package com.king.app.coolg_kt.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 页码控件
 * 支持上一页、下一页，具体某页
 * 支持最多显示多少页码，剩余的以...呈现，点击时能展示更多页码，保持最多显示的页码数更新页码范围
 * @author：Jing
 * @date: 2021/2/22 9:40
 */
public class PageIndicator extends LinearLayout {

    private final int ID_LAST = -101;
    private final int ID_NEXT = -102;
    private final int ID_MORE_LAST = -1011;
    private final int ID_MORE_NEXT = -1021;

    private int mTextColor;
    private int mTextSize;
    private int mItemWidth;
    private int mItemBackground;
    private int mItemMargin;
    private int mMaxPage;

    // 下标从0开始
    private int page;
    private int currentPage;

    private OnPageListener onPageListener;
    private TextView tvLast;
    private TextView tvNext;
    private TextView tvMoreLast;
    private TextView tvMoreNext;

    private List<TextView> pageList = new ArrayList<>();

    public PageIndicator(Context context) {
        super(context);
        init(null);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public void setOnPageListener(OnPageListener onPageListener) {
        this.onPageListener = onPageListener;
    }

    private void init(AttributeSet attrs) {
        if (attrs == null) {
            mTextSize = ScreenUtils.dp2px(14);
            mItemWidth = ScreenUtils.dp2px(30);
            mItemMargin = ScreenUtils.dp2px(8);
            mTextColor = Color.WHITE;
            mMaxPage = 10;
            mItemBackground = R.drawable.selector_border;
        } else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PageIndicator);
            mTextSize = a.getDimensionPixelSize(R.styleable.PageIndicator_pi_textSize, ScreenUtils.dp2px(14));
            mItemWidth = a.getDimensionPixelSize(R.styleable.PageIndicator_pi_itemWidth, ScreenUtils.dp2px(30));
            mItemMargin = a.getDimensionPixelSize(R.styleable.PageIndicator_pi_itemMargin, ScreenUtils.dp2px(8));
            mTextColor = a.getColor(R.styleable.PageIndicator_pi_textColor, Color.WHITE);
            mMaxPage = a.getInteger(R.styleable.PageIndicator_pi_maxPage, 10);
            mItemBackground = a.getResourceId(R.styleable.PageIndicator_pi_itemBackground, R.drawable.selector_border);
        }
        setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setPage(int page) {
        this.page = page;
        currentPage = 0;
        onPageChanged();
    }

    private void onPageChanged() {
        removeAllViews();
        pageList.clear();
        tvLast = addPageText(ID_LAST, "上一页");
        tvMoreLast = addPageText(ID_MORE_LAST, "...");
        for (int i = 0; i < page && i < mMaxPage; i ++) {
            TextView page = addPageText(i, String.valueOf(i + 1));
            pageList.add(page);
        }
        tvMoreNext = addPageText(ID_MORE_NEXT, "...");
        tvNext = addPageText(ID_NEXT, "下一页");
        onCurrentPageChanged();
    }

    private void onCurrentPageChanged() {

        // 判断是否更新页码区域
        findNewLastEndPage(currentPage);
        // more ... 的显示情况
        int showLastPage = pageList.get(pageList.size() - 1).getId();
        if (showLastPage == page - 1) {
            tvMoreNext.setVisibility(GONE);
        }
        else {
            tvMoreNext.setVisibility(VISIBLE);
        }
        int showFirstPage = pageList.get(0).getId();
        if (showFirstPage == 0) {
            tvMoreLast.setVisibility(GONE);
        }
        else {
            tvMoreLast.setVisibility(VISIBLE);
        }

        // 设置页码的选中情况
        for (int i = 0; i < getChildCount(); i ++) {
            View child = getChildAt(i);
            if (child.getId() == currentPage) {
                child.setSelected(true);
            }
            else {
                child.setSelected(false);
            }
        }
        // 上一页下一页的显示情况
        if (currentPage == 0) {
            tvLast.setVisibility(INVISIBLE);
        }
        else {
            tvLast.setVisibility(VISIBLE);
        }
        if (currentPage == page - 1) {
            tvNext.setVisibility(INVISIBLE);
        }
        else {
            tvNext.setVisibility(VISIBLE);
        }
    }

    /**
     * 以当前页码为基准，更新当前页码区域显示的全部页码
     * @param base
     * @return
     */
    private void findNewLastEndPage(int base) {
        int showFirstPage = pageList.get(0).getId();
        int showLastPage = pageList.get(pageList.size() - 1).getId();
        // 更多后面的页面
        if (base > showLastPage) {
            // 剩余页码总数，包括base
            int left = page - base;
            // 优先将base置于中心
            int center = mMaxPage / 2;
            // 右方总共可放置的页码数
            int rightTotal = mMaxPage - center;
            int offset = 0;
            // 剩余页码不足右方总共可放置的数量
            if (left < rightTotal) {
                rightTotal = left;
                int baseTarget = mMaxPage - rightTotal;
                offset = base - baseTarget;
            }
            // 剩余页码足够填补右方总共可放置的数量
            else {
                offset = base - center;
            }
            // 更新页码范围
            for (int i = 0; i < pageList.size(); i ++) {
                pageList.get(i).setId(i + offset);
                pageList.get(i).setText(String.valueOf(i + offset + 1));
            }
        }
        // 更多前面的页面
        else if (base < showFirstPage) {
            // 剩余页码总数，包括base
            int left = base + 1;
            // 优先将base置于中心
            int center = mMaxPage / 2;
            // 左方总共可放置的页码数
            int leftTotal = center;
            int offset = 0;
            // 剩余页码不足左方总共可放置的数量
            if (left < leftTotal) {
                leftTotal = left;
                int baseTarget = leftTotal - 1;
                offset = base - baseTarget;
            }
            // 剩余页码足够填补左方总共可放置的数量
            else {
                offset = base + 1 - center;
            }
            // 更新页码范围
            for (int i = 0; i < pageList.size(); i ++) {
                pageList.get(i).setId(i + offset);
                pageList.get(i).setText(String.valueOf(i + offset + 1));
            }
        }
    }

    /**
     * id即页码（下标从0开始）
     * @param id
     * @param text
     * @return
     */
    private TextView addPageText(int id, String text) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, mItemWidth);
        if (getChildCount() > 0) {
            params.leftMargin = mItemMargin;
        }
        TextView textView = new TextView(getContext());
        if (id == ID_LAST || id == ID_NEXT) {
            int padding = ScreenUtils.dp2px(10);
            textView.setPadding(padding, 0, padding, 0);
        }
        else {
            params.width = mItemWidth;
        }
        textView.setId(id);
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);
        textView.setTextColor(mTextColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        textView.setBackgroundResource(mItemBackground);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == ID_LAST) {
                    currentPage --;
                }
                else if (id == ID_NEXT) {
                    currentPage ++;
                }
                else if (id == ID_MORE_LAST) {
                    int firstPage = pageList.get(0).getId();
                    currentPage = firstPage - 1;
                }
                else if (id == ID_MORE_NEXT) {
                    int lastPage = pageList.get(pageList.size() - 1).getId();
                    currentPage = lastPage + 1;
                }
                else {
                    currentPage = id;
                }
                onCurrentPageChanged();
                onPageListener.onPage(currentPage);
            }
        });
        addView(textView, params);
        return textView;
    }

    public interface OnPageListener {
        /**
         * 下标从0开始
         * @param page
         */
        void onPage(int page);
    }
}
