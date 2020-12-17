package com.king.app.coolg_kt.view.widget.video;

import android.content.Context;
import android.util.AttributeSet;

import com.king.app.coolg_kt.R;

import cn.jzvd.JzvdStd;

/**
 * @description: 自定义全屏播放器
 * @author：Jing
 * @date: 2020/12/17 22:34
 */
public class FullJzvd extends JzvdStd {

    public FullJzvd(Context context) {
        super(context);
    }

    public FullJzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_video_controller;
    }
}
