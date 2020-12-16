package com.king.app.coolg_kt.view.widget.flow;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by 景阳 on 2016/10/28.
 */

public abstract class BaseFlowAdapter<T> {

    protected List<T> data;

    private FlowLayout flowLayout;

    public void bindFlowLayout(FlowLayout layout) {
        flowLayout = layout;
        flowLayout.removeAllViews();
        for (int i = 0; i < getCount(); i ++) {
            flowLayout.addView(getView(i, flowLayout));
        }
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCount() {
        return data == null ? 0:data.size();
    }

    public abstract View getView(int position, ViewGroup group);

    protected View getChildView(int position) {
        if (position < flowLayout.getChildCount() && position > -1) {
            return flowLayout.getChildAt(position);
        }
        return null;
    }
}
