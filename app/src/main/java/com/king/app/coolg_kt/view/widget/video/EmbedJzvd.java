package com.king.app.coolg_kt.view.widget.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.DebugLog;
import com.king.app.coolg_kt.utils.ScreenUtils;

import cn.jzvd.JZUtils;
import cn.jzvd.JzvdStd;

/**
 * Desc: 嵌在布局里的播放器，继承JzvdStd，仅做一些按钮的事件拦截与处理
 *
 * @author：Jing Yang
 * @date: 2020/12/17 13:40
 */
public class EmbedJzvd extends JzvdStd {

    private OnClickListener interceptFullScreenListener;

    private OnPlayEmptyUrlListener onPlayEmptyUrlListener;

    private OnVideoListener onVideoListener;

    public EmbedJzvd(Context context) {
        super(context);
    }

    public EmbedJzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInterceptFullScreenListener(OnClickListener interceptFullScreenListener) {
        this.interceptFullScreenListener = interceptFullScreenListener;
    }

    public OnClickListener getInterceptFullScreenListener() {
        return interceptFullScreenListener;
    }

    public void setOnPlayEmptyUrlListener(OnPlayEmptyUrlListener onPlayEmptyUrlListener) {
        this.onPlayEmptyUrlListener = onPlayEmptyUrlListener;
    }

    public void setOnVideoListener(OnVideoListener onVideoListener) {
        this.onVideoListener = onVideoListener;
    }

    public OnVideoListener getOnVideoListener() {
        return onVideoListener;
    }

    /**
     * 正在播放
     * @return
     */
    public boolean isPlaying() {
        return state == STATE_PLAYING;
    }

    /**
     * 拦截null url
     */
    @Override
    protected void clickStart() {
        // url为空
//        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
//            if (onPlayEmptyUrlListener != null) {
//                onPlayEmptyUrlListener.onPlayEmptyUrl();
//                return;
//            }
//        }
        super.clickStart();
    }

    /**
     * 拦截全屏按钮
     */
    @Override
    protected void clickFullscreen() {
        if (interceptFullScreenListener == null) {
            super.clickFullscreen();
        }
        else {
            interceptFullScreenListener.onClick(fullscreenButton);
        }
    }

    /**
     * 原本的全屏事件
     */
    public void executeFullScreen() {
        super.clickFullscreen();
    }

    /**
     * 是否是小窗播放
     * @return
     */
    public boolean isTinyScreen() {
        return screen == SCREEN_TINY;
    }

    /**
     * Jzvd的小窗播放在使用scrollview滑动过程中控制显示隐藏有bug：
     *      小窗播放时快速向顶部滑动，崩溃 Attempt to read from field 'int android.view.View.mViewFlags' on a null object reference
     *       at android.view.ViewGroup.dispatchGetDisplayList(ViewGroup.java:4293)
             at android.view.View.updateDisplayListIfDirty(View.java:19069)
             at android.view.ThreadedRenderer.updateViewTreeDisplayList(ThreadedRenderer.java:686)
             at android.view.ThreadedRenderer.updateRootDisplayList(ThreadedRenderer.java:692)
             at android.view.ThreadedRenderer.draw(ThreadedRenderer.java:801)
             at android.view.ViewRootImpl.draw(ViewRootImpl.java:3373)
             at android.view.ViewRootImpl.performDraw(ViewRootImpl.java:3163)
             at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:2532)
             at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:1505)
     * 正常速度回滑不会有问题，猜测是因为小窗使用向DecorView中add, remove view来实现，因此在快速滑动中scroll view进行了全局的渲染，导致还没来得及remove的child是null
     */
    public void cancelTinyScreen() {
        gotoNormalScreen();
    }

    /**
     * 小窗播放
     */
    public void gotoTinyScreen() {
        DebugLog.e();
        if (state == STATE_NORMAL || state == STATE_ERROR || state == STATE_AUTO_COMPLETE)
            return;
        ViewGroup vg = (ViewGroup) getParent();
        jzvdContext = vg.getContext();
        blockLayoutParams = getLayoutParams();
        blockIndex = vg.indexOfChild(this);
        blockWidth = getWidth();
        blockHeight = getHeight();

        vg.removeView(this);
        cloneAJzvd(vg);
        CONTAINER_LIST.add(vg);

        ViewGroup vgg = (ViewGroup) (JZUtils.scanForActivity(getContext())).getWindow().getDecorView();//和他也没有关系
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.float_video_width),
                getResources().getDimensionPixelSize(R.dimen.float_video_height));
        lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        lp.rightMargin = ScreenUtils.dp2px(16);
        lp.bottomMargin = ScreenUtils.dp2px(16);
        //添加滑动事件等
        vgg.addView(this, lp);
        setScreenTiny();
    }

    /**
     * 按下播放按钮后，视频加载完毕开始播放
     */
    @Override
    public void startVideo() {
        super.startVideo();
        DebugLog.e();
        if (onVideoListener != null) {
            onVideoListener.onStart();
        }
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
        if (onVideoListener != null && onVideoListener.getStartSeek() > 0) {
            mediaInterface.seekTo(onVideoListener.getStartSeek());
        }
    }

    /**
     * startVideo之后，或从pause恢复到playing时
     */
    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        DebugLog.e();
        if (onVideoListener != null) {
//            onVideoListener.onStart();
        }
    }

    /**
     * 按下暂停按钮后
     */
    @Override
    public void onStatePause() {
        super.onStatePause();
        DebugLog.e();
        if (onVideoListener != null) {
            updatePosition();
            onVideoListener.onPause();
        }
    }

    /**
     * 视频播放完毕
     */
    @Override
    public void onCompletion() {
        super.onCompletion();
        DebugLog.e();
        if (onVideoListener != null) {
            updatePosition();
            onVideoListener.onPlayComplete();
        }
    }

    /**
     * 更新播放位置
     */
    private void updatePosition() {
        DebugLog.e("" + mediaInterface.getCurrentPosition());
        onVideoListener.updatePlayPosition(mediaInterface.getCurrentPosition());
    }
}
