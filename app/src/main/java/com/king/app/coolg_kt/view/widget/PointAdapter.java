package com.king.app.coolg_kt.view.widget;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/8 16:50
 */
public abstract class PointAdapter {

    public abstract int getItemCount();

    public abstract int getPointColor(int position);

    public abstract int getTextColor(int position);

    public abstract String getText(int position);
}
