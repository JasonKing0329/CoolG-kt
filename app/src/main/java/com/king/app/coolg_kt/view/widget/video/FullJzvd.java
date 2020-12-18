package com.king.app.coolg_kt.view.widget.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.DebugLog;
import com.king.app.coolg_kt.utils.FormatUtil;

import cn.jzvd.JZDataSource;
import cn.jzvd.JzvdStd;

/**
 * @description: 自定义全屏播放器
 * @author：Jing
 * @date: 2020/12/17 22:34
 */
public class FullJzvd extends JzvdStd {

    private OnVideoListener onVideoListener;

    private OnVideoListListener onVideoListListener;

    private OnVideoDurationListener onVideoDurationListener;

    private OnVideoClickListener onVideoClickListener;

    private boolean isSeekToAfterPrepared;

    private boolean isInitVideo = true;

    public FullJzvd(Context context) {
        super(context);
    }

    public FullJzvd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnVideoListener(OnVideoListener onVideoListener) {
        this.onVideoListener = onVideoListener;
    }

    public OnVideoListener getOnVideoListener() {
        return onVideoListener;
    }

    public void setOnVideoListListener(OnVideoListListener onVideoListListener) {
        this.onVideoListListener = onVideoListListener;
    }

    public OnVideoListListener getOnVideoListListener() {
        return onVideoListListener;
    }

    public void setOnVideoDurationListener(OnVideoDurationListener onVideoDurationListener) {
        this.onVideoDurationListener = onVideoDurationListener;
    }

    public OnVideoDurationListener getOnVideoDurationListener() {
        return onVideoDurationListener;
    }

    public void setOnVideoClickListener(OnVideoClickListener onVideoClickListener) {
        this.onVideoClickListener = onVideoClickListener;
    }

    public OnVideoClickListener getOnVideoClickListener() {
        return onVideoClickListener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_full_jzvd;
    }

    @Override
    public void init(Context context) {
        super.init(context);

        // 扩展的4个按钮
        findViewById(R.id.app_video_last).setOnClickListener(v -> {
            if (onVideoListListener != null) {
                onVideoListListener.playPrevious();
            }
        });

        findViewById(R.id.app_video_next).setOnClickListener(v -> {
            if (onVideoListListener != null) {
                onVideoListListener.playNext();
            }
        });

        findViewById(R.id.app_video_clarity).setOnClickListener(v -> {

        });

        findViewById(R.id.app_video_list).setOnClickListener(v -> {
            if (onVideoListListener != null) {
                onVideoListListener.showPlayList();
            }
        });
    }

    /**
     * 自定义播放、暂停按钮。禁用父类JzvdStd的更改
     */
    @Override
    public void updateStartImage() { }
    @Override
    public void changeStartButtonSize(int size) { }

    /**
     * 覆盖点击播放按钮事件。这里可以检测处于播放还是暂停状态
     */
    @Override
    protected void clickStart() {
        super.clickStart();
    }

    /**
     * 按下播放按钮后，确认是要执行播放事件，super方法中开始准备资源、加载视频
     */
    @Override
    public void startVideo() {
        DebugLog.e();
        if (onVideoListener != null) {
            onVideoListener.onStart();
        }
        // 在这里处理从头播放/恢复播放
        if (onVideoListener == null) {
            super.startVideo();
        }
        else {
            if (isInitVideo && onVideoListener.getStartSeek() > 0) {
                restartOrRestore();
            }
            else {
                super.startVideo();
            }
        }
    }

    private void restartOrRestore() {
        String message = "This video has been played to " + FormatUtil.formatTime(onVideoListener.getStartSeek()) + " last time. Restore or restart?";
        new AlertDialog.Builder(getContext())
                .setTitle(null)
                .setMessage(message)
                .setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSeekToAfterPrepared = true;
                        isInitVideo = false;
                        FullJzvd.super.startVideo();
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSeekToAfterPrepared = false;
                        isInitVideo = false;
                        FullJzvd.super.startVideo();
                    }
                })
                .show();
    }

    /**
     * startVideo之后，资源一切就绪，即将开始播放。在这里可以处理跳进度
     */
    @Override
    public void onPrepared() {
        super.onPrepared();
        if (onVideoDurationListener != null) {
            onVideoDurationListener.onReceiveDuration(getDuration());
        }
        if (onVideoListener != null) {
            if (isSeekToAfterPrepared) {
                mediaInterface.seekTo(onVideoListener.getStartSeek());
            }
        }
    }

    /**
     * startVideo之后，首次开始播放或从pause恢复到play时
     */
    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        DebugLog.e();
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

    /**
     * 设置播放地址
     * @param url
     * @param title
     */
    public void setPlayUrl(String url, String title) {
        // 第一次使用setUp，不自动播放
        if (jzDataSource == null) {
            setUp(url, title);
        }
        // 以后的调用更换url，但父类的changeUrl直接调用了startVideo，通过覆盖onStatePreparingChangeUrl禁止自动播放
        else {
            changeUrl(new JZDataSource(url, title), 0);
        }
    }

    /**
     * 覆盖父类的更换播放地址逻辑，去掉更换url后自动播放
     * 外界调用startVideo来启动
     */
    @Override
    public void onStatePreparingChangeUrl() {
        state = STATE_PREPARING_CHANGE_URL;
        releaseAllVideos();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        mediaInterface.pause();
        onStatePause();
    }
}
