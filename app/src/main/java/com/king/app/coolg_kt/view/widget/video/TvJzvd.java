package com.king.app.coolg_kt.view.widget.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.avery.subtitle.widget.SimpleSubtitleView;
import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.page.tv.JZMediaIjk;
import com.king.app.coolg_kt.utils.DebugLog;
import com.king.app.coolg_kt.utils.FormatUtil;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

/**
 * @description: 自定义全屏播放器
 * @author：Jing
 * @date: 2020/12/17 22:34
 */
public class TvJzvd extends JzvdStd {

    private OnVideoListener onVideoListener;

    private OnVideoListListener onVideoListListener;

    private OnVideoDurationListener onVideoDurationListener;

    private OnVideoClickListener onVideoClickListener;

    private OnBackListener onBackListener;

    private ImageView ivBack;

    private SimpleSubtitleView subtitleView;

    private boolean isSeekToAfterPrepared;

    private boolean isInitVideo = true;

    public TvJzvd(Context context) {
        super(context);
    }

    public TvJzvd(Context context, AttributeSet attrs) {
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

    public void setOnBackListener(OnBackListener onBackListener) {
        this.onBackListener = onBackListener;
    }

    public OnBackListener getOnBackListener() {
        return onBackListener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_tv_jzvd;
    }

    @Override
    public void init(Context context) {
        super.init(context);

        // 扩展的5个按钮
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> {
            if (onBackListener != null) {
                onBackListener.onBack();
            }
        });
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

        subtitleView = findViewById(R.id.subtitle_view);
    }

    /**
     * 自定义播放、暂停按钮。禁用父类JzvdStd的更改
     */
    @Override
    public void updateStartImage() { }
    @Override
    public void changeStartButtonSize(int size) { }

    /**
     * setup第一个url后，会执行到这里。父类里没有显示bottom control，这里覆盖掉
     */
    @Override
    public void changeUiToNormal() {
        DebugLog.e();
        setControlBarVisible(false);
        setPlayIcon(true);
    }

    /**
     * 正在加载，覆盖父类，使control bar都可见
     */
    @Override
    public void changeUiToPreparing() {
        DebugLog.e();
        setControlBarVisible(true);
        setPlayIcon(false);
    }

    /**
     * 正在加载变化的url，覆盖父类，使control bar都可见
     */
    @Override
    public void changeUIToPreparingChangeUrl() {
        DebugLog.e();
        setControlBarVisible(true);
    }

    /**
     * 正在加载变化的url，覆盖父类，使control bar都可见
     */
    @Override
    public void changeUIToPreparingPlaying() {
        DebugLog.e();
        setControlBarVisible(true);
    }

    /**
     * 加载过程中父类什么也不显示，这里覆盖掉
     */
    @Override
    public void changeUiToComplete() {
        DebugLog.e();
        setControlBarVisible(false);
        setPlayIcon(true);
    }

    /**
     * error后父类什么也不显示，也无法启动，这里覆盖掉
     */
    @Override
    public void changeUiToError() {
        DebugLog.e();
        setControlBarVisible(false);
    }

    private void setControlBarVisible(boolean isLoading) {
        setAllControlsVisiblity(View.VISIBLE, View.VISIBLE, View.VISIBLE,
                isLoading ? View.VISIBLE:View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
    }

    @Override
    public void changeUiToPauseClear() {
        DebugLog.e();
        super.changeUiToPauseClear();
    }

    @Override
    public void changeUiToPauseShow() {
        DebugLog.e();
        super.changeUiToPauseShow();
    }

    @Override
    public void changeUiToPlayingClear() {
        DebugLog.e();
        super.changeUiToPlayingClear();
    }

    @Override
    public void changeUiToPlayingShow() {
        DebugLog.e();
        super.changeUiToPlayingShow();
    }

    /**
     * releaseAllVideos之后会执行这个
     */
    @Override
    public void onStateNormal() {
        DebugLog.e();
        super.onStateNormal();
    }

    /**
     * 覆盖点击播放按钮事件。这里可以检测处于播放还是暂停状态
     */
    @Override
    protected void clickStart() {
        DebugLog.e();
        if (jzDataSource == null || jzDataSource.urlsMap.isEmpty() || jzDataSource.getCurrentUrl() == null) {
            return;
        }
        // 长时间一直在加载中，令其中断
        if (state == STATE_PREPARING || state == STATE_PREPARING_PLAYING) {
            Jzvd.releaseAllVideos();
        }
        // 加载出错后支持重新开始加载
        else if (state == STATE_ERROR) {
            startVideo();
        }
        else {
            super.clickStart();
        }
    }

    /**
     * 扩展点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        DebugLog.e();
        super.onClick(v);
        // 正在转圈的时候，点击屏幕都执行top、bottom bar的唤起与消失
        if ((state == STATE_PREPARING || state == STATE_PREPARING_PLAYING) && v.getId() == R.id.surface_container) {
            DebugLog.e("surface_container");
            if (bottomContainer.getVisibility() == VISIBLE) {
                topContainer.setVisibility(INVISIBLE);
                bottomContainer.setVisibility(INVISIBLE);
                startButton.setVisibility(INVISIBLE);
            }
            else {
                topContainer.setVisibility(VISIBLE);
                bottomContainer.setVisibility(VISIBLE);
                startButton.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 覆盖父类点击事件，播放与暂停全部交于startButton，不允许通过点击海报执行播放    */
    @Override
    protected void clickPoster() {
        DebugLog.e();
    }

    /**
     * 按下播放按钮后，确认是要执行播放事件，super方法中开始准备资源、加载视频
     */
    @Override
    public void startVideo() {
        DebugLog.e();
        super.startVideo();
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
                        TvJzvd.super.startVideo();
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSeekToAfterPrepared = false;
                        isInitVideo = false;
                        TvJzvd.super.startVideo();
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
        DebugLog.e();
//        if (mediaInterface instanceof JZMediaSystem) {
//            MediaPlayer player = ((JZMediaSystem) mediaInterface).mediaPlayer;
//            subtitleView.bindToMediaPlayer(player);
//            subtitleView.setSubtitlePath("http://192.168.1.106:8080/JJGalleryServer/videos/i_scene/test.ass");
//        }
        if (onVideoDurationListener != null) {
            onVideoDurationListener.onReceiveDuration(getDuration());
        }
        if (onVideoListener != null) {
            if (isSeekToAfterPrepared) {
//                mediaInterface.seekTo(onVideoListener.getStartSeek());
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
        setPlayIcon(false);
    }

    private void setPlayIcon(boolean isPlayIcon) {
        if (isPlayIcon) {
            startButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
        else {
            startButton.setImageResource(R.drawable.ic_stop_white_36dp);
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
        setPlayIcon(true);
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
        setPlayIcon(false);
    }

    @Override
    public void onStateError() {
        super.onStateError();
        DebugLog.e();
        setPlayIcon(true);
        onVideoListener.onError();
    }

    /**
     * 更新播放位置
     */
    private void updatePosition() {
        DebugLog.e("" + mediaInterface.getCurrentPosition());
        onVideoListener.updatePlayPosition(mediaInterface.getCurrentPosition());
    }

    /**
     * 经实测小米电视E43K只能用ijk引擎才能正常播放，其他引擎只有声音，画面一直卡在转圈那里
     * 设置播放地址并播放，如果没有setUp过用setUp在clickStart，否则直接changeUrl
     * @param url
     * @param title
     */
    public void playUrl(String url, String title) {
        DebugLog.e(url);
        if (jzDataSource == null) {
            setUp(url, title, JzvdStd.SCREEN_FULLSCREEN, JZMediaIjk.class);
            clickStart();
        }
        // 以后的调用更换url，但父类的changeUrl直接调用了startVideo，通过覆盖onStatePreparingChangeUrl禁止自动播放
        else {
            // changeUrl，会在onStatePreparingChangeUrl状态时自动调用startVideo自动播放
            changeUrl(new JZDataSource(url, title), 0);
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        DebugLog.e();
        if (mediaInterface != null) {
            mediaInterface.pause();
            onStatePause();
        }
    }

    /**
     * 控制顶部与底部按钮的显示状况，自定义扩展的view跟随topCon和bottomCon就好
     * @param topCon
     * @param bottomCon
     * @param startBtn
     * @param loadingPro
     * @param posterImg
     * @param bottomPro
     * @param retryLayout
     */
    @Override
    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro, int posterImg, int bottomPro, int retryLayout) {
        DebugLog.e("bottomCon=" + bottomCon);
        super.setAllControlsVisiblity(topCon, bottomCon, startBtn, loadingPro, posterImg, bottomPro, retryLayout);
        ivBack.setVisibility(topCon);
    }

    @Override
    public void showProgressDialog(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration) {
        DebugLog.e();
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration);
    }

    public interface OnBackListener {
        void onBack();
    }

    /**
     * 拖动进度条，首先触发onInfo() -> setState(STATE_PREPARING_PLAYING) -> onStatePreparingPlaying -> changeUIToPreparingPlaying
     * 上面在onInfo中可以看到是MediaPlayer.MEDIA_INFO_BUFFERING_START
     * 那么对应的，结束就应该是MediaPlayer.MEDIA_INFO_BUFFERING_END
     * 实际结束时，经常会观测到继续拖动seekBar总是会跳回一开始的位置，发生不了进度改变，通过调试发现是
     * onStopTrackingTouch方法里面下面代码return了
     if (state != STATE_PLAYING &&
     state != STATE_PAUSE) return;
     * 因此，可以得知在MEDIA_INFO_BUFFERING_END后没有还原state，从而导致继续拖动进度条无效
     * 在这里覆盖一下父类方法，还原一下state就可以解决
     * 同样在这里可以隐藏loading，解决这个状态下已经开始播放了但还是一直在转圈的问题
     * @param what
     * @param extra
     */
    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            state = STATE_PLAYING;
            setControlBarVisible(false);
        }
    }

    /**
     * 覆盖父类的ontouch move方法，父类判断了screen类型，但screen在各种状态切换下会改变，直接覆盖去掉对screen的校验。FullJzvd一直是full_screen
     * @param x
     * @param y
     */
    @Override
    protected void touchActionMove(float x, float y) {
        Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
        float deltaX = x - mDownX;
        float deltaY = y - mDownY;
        float absDeltaX = Math.abs(deltaX);
        float absDeltaY = Math.abs(deltaY);
        //拖动的是NavigationBar和状态栏
        if (mDownX > JZUtils.getScreenWidth(getContext()) || mDownY < JZUtils.getStatusBarHeight(getContext())) {
            return;
        }
        if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
            if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                cancelProgressTimer();
                if (absDeltaX >= THRESHOLD) {
                    // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                    // 否则会因为mediaplayer的状态非法导致App Crash
                    if (state != STATE_ERROR) {
                        mChangePosition = true;
                        mGestureDownPosition = getCurrentPositionWhenPlaying();
                    }
                } else {
                    //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                    if (mDownX < mScreenHeight * 0.5f) {//左侧改变亮度
                        mChangeBrightness = true;
                        WindowManager.LayoutParams lp = JZUtils.getWindow(getContext()).getAttributes();
                        if (lp.screenBrightness < 0) {
                            try {
                                mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                Log.i(TAG, "current system brightness: " + mGestureDownBrightness);
                            } catch (Settings.SettingNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mGestureDownBrightness = lp.screenBrightness * 255;
                            Log.i(TAG, "current activity brightness: " + mGestureDownBrightness);
                        }
                    } else {//右侧改变声音
                        mChangeVolume = true;
                        mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    }
                }
            }
        }
        if (mChangePosition) {
            long totalTimeDuration = getDuration();
            mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
            if (mSeekTimePosition > totalTimeDuration)
                mSeekTimePosition = totalTimeDuration;
            String seekTime = JZUtils.stringForTime(mSeekTimePosition);
            String totalTime = JZUtils.stringForTime(totalTimeDuration);

            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        }
        if (mChangeVolume) {
            deltaY = -deltaY;
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
            //dialog中显示百分比
            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
            showVolumeDialog(-deltaY, volumePercent);
        }

        if (mChangeBrightness) {
            deltaY = -deltaY;
            int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
            WindowManager.LayoutParams params = JZUtils.getWindow(getContext()).getAttributes();
            if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                params.screenBrightness = 1;
            } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                params.screenBrightness = 0.01f;
            } else {
                params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
            }
            JZUtils.getWindow(getContext()).setAttributes(params);
            //dialog中显示百分比
            int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
            showBrightnessDialog(brightnessPercent);
//                        mDownY = y;
        }
    }

}
