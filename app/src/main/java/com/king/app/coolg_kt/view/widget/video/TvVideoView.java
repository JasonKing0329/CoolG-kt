package com.king.app.coolg_kt.view.widget.video;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * @description: VideoView默认会执行
 *         setFocusable(true);
 *         setFocusableInTouchMode(true);
 *         requestFocus();
 * 在电视项目中影响焦点问题，去掉其focus
 * @author：Jing
 * @date: 2021/2/18 17:07
 */
public class TvVideoView extends VideoView {
    public TvVideoView(Context context) {
        super(context);
        init();
    }

    public TvVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TvVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TvVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setFocusable(false);
        setFocusableInTouchMode(false);
        clearFocus();
    }
}
