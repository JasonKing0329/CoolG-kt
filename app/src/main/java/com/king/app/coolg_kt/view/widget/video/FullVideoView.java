package com.king.app.coolg_kt.view.widget.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.DebugLog;
import com.king.app.coolg_kt.utils.FormatUtil;

import java.lang.reflect.Field;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkTimedText;

/**
 * Desc: 扩展GiraffePlayer开源框架的VideoView
 * 支持记录与恢复播放时间
 * 各种控制UI元素更换为自己的布局，加入列表、前/下一个、菜单按钮
 * 
 * 与另一个扩展的EmbedVideoView的区别是：
 * EmbedVideoView沿用了VideoView的全部UI元素，只扩展VideoView本身
 * 所以EmbedVideoView作为嵌入到各种页面中的视频，可以切换横竖屏以及浮窗
 *
 * FullVideoView采用反射修改了VideoView内部的MediaController实例
 * 利用派生的FullMediaController来重新布局各种控制类型UI元素（xml与java文件大体上照搬原DefaultMediaController与其引用的.xml，但扩展自己的UI需求）
 * 所以适合作为一个完整的横屏视频使用，支持播放列表
 * @author：Jing Yang
 * @date: 2018/11/15 15:22
 */
public class FullVideoView extends VideoView {

    private FullMediaController mediaController;

    private OnVideoListener onVideoListener;

    private OnVideoListListener onVideoListListener;

    private OnVideoClickListener onVideoClickListener;

    private OnPlayEmptyUrlListener onPlayEmptyUrlListener;

    private OnVideoDurationListener onVideoDurationListener;

    private boolean isInitVideo = true;

    private boolean isSeekToAfterPrepared;

    public FullVideoView(Context context) {
        super(context);
        initThis();
    }

    public FullVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initThis();
    }

    public FullVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initThis();
    }

    private void initThis() {
        mediaController = new FullMediaController(getContext());
        try {
            Field field = VideoView.class.getDeclaredField("mediaController");
            field.setAccessible(true);
            field.set(this, mediaController);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        getContainer().removeAllViews();
        mediaController.bind(this);

        expandParent();
    }

    public void setOnVideoListener(OnVideoListener onVideoListener) {
        this.onVideoListener = onVideoListener;
    }

    public void setOnVideoDurationListener(OnVideoDurationListener onVideoDurationListener) {
        this.onVideoDurationListener = onVideoDurationListener;
    }

    public void setOnVideoListListener(OnVideoListListener onVideoListListener) {
        this.onVideoListListener = onVideoListListener;
        mediaController.setOnVideoListListener(onVideoListListener);
    }

    public void setOnPlayEmptyUrlListener(OnPlayEmptyUrlListener onPlayEmptyUrlListener) {
        this.onPlayEmptyUrlListener = onPlayEmptyUrlListener;
        mediaController.setOnPlayEmptyUrlListener(onPlayEmptyUrlListener);
    }

    public void setOnVideoClickListener(OnVideoClickListener onVideoClickListener) {
        this.onVideoClickListener = onVideoClickListener;
        mediaController.setOnVideoClickListener(onVideoClickListener);
    }

    private void expandParent() {
        // 覆盖videoView里封装的播放按键的监听事件，处理恢复播放时间的功能
        findViewById(R.id.app_video_play).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getVideoInfo().getUri() == null) {
                    if (onPlayEmptyUrlListener != null) {
                        onPlayEmptyUrlListener.onPlayEmptyUrl(getVideoInfo().getFingerprint(), new UrlCallback() {
                            @Override
                            public void onReceiveUrl(String url) {
                                setVideoPath(url);
                                prepare();
                                play();
                            }
                        });
                        return;
                    }
                }
                onClickPlay();
            }
        });
    }

    private void onClickPlay() {
        GiraffePlayer player = getPlayer();
        if (player.isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        try {
            GiraffePlayer player = getPlayer();
            if (onVideoListener == null) {
                player.start();
            }
            else {
                if (isInitVideo && onVideoListener.getStartSeek() > 0) {
                    restartOrRestore(player);
                }
                else {
                    player.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        try {
            getPlayer().pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restartOrRestore(GiraffePlayer player) {
        String message = "This video has been played to " + FormatUtil.formatTime(onVideoListener.getStartSeek()) + " last time. Restore or restart?";
        new AlertDialog.Builder(getContext())
                .setTitle(null)
                .setMessage(message)
                .setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSeekToAfterPrepared = true;
                        isInitVideo = false;
                        player.start();
                    }
                })
                .setNegativeButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSeekToAfterPrepared = false;
                        isInitVideo = false;
                        player.start();
                    }
                })
                .show();
    }

    public void prepare() {
        setPlayerListener(new PlayerListener() {
            @Override
            public void onPrepared(GiraffePlayer giraffePlayer) {
                DebugLog.e("current=" + giraffePlayer.getCurrentPosition() + ", total=" + giraffePlayer.getDuration());

                if (onVideoDurationListener != null) {
                    onVideoDurationListener.onReceiveDuration(giraffePlayer.getDuration());
                }
                if (onVideoListener != null) {
                    // seekTo放在这里调比较准确，如果放在setVideoPath或者start()之后不准确
                    if (isSeekToAfterPrepared) {
                        giraffePlayer.seekTo(onVideoListener.getStartSeek());
                    }
                }
            }

            @Override
            public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {
                DebugLog.e("percent " + percent);
                // 按返回键退出页面时只能监听到onRelease，但是在onRelease里获取CurrentPosition已经是0了
                // onBufferingUpdate是一直在执行的，所以在这里仅在内存中更新当前播放位置，再在onRelease时保存到数据库中updateToDb
                // 但是，调试发现如果通过任务栏划掉程序，onRelease不会被执行，所以在activity的onDestroy里再执行一次updateToDb
                // 三重保证最大限度优化记录播放的位置
                if (onVideoListener != null) {
                    onVideoListener.updatePlayPosition(giraffePlayer.getCurrentPosition());
                }
            }

            @Override
            public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
                DebugLog.e("what " + what + ", extra " + extra);
                return false;
            }

            @Override
            public void onCompletion(GiraffePlayer giraffePlayer) {
                DebugLog.e("current=" + giraffePlayer.getCurrentPosition() + ", total=" + giraffePlayer.getDuration());
                // 播放完毕重置播放位置记录
                if (onVideoListener != null) {
                    onVideoListener.onPlayComplete();
                }
            }

            @Override
            public void onSeekComplete(GiraffePlayer giraffePlayer) {
                DebugLog.e("");
            }

            @Override
            public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
                DebugLog.e("what " + what + ", extra " + extra);
                return false;
            }

            @Override
            public void onPause(GiraffePlayer giraffePlayer) {
                DebugLog.e("current=" + giraffePlayer.getCurrentPosition() + ", total=" + giraffePlayer.getDuration());
                if (onVideoListener != null) {
                    onVideoListener.updatePlayPosition(giraffePlayer.getCurrentPosition());
                    onVideoListener.onPause();
                }
            }

            @Override
            public void onRelease(GiraffePlayer giraffePlayer) {
                DebugLog.e("current=" + giraffePlayer.getCurrentPosition() + ", total=" + giraffePlayer.getDuration());
                if (onVideoListener != null) {
                    onVideoListener.onDestroy();
                }
            }

            @Override
            public void onStart(GiraffePlayer giraffePlayer) {
                DebugLog.e("current=" + giraffePlayer.getCurrentPosition() + ", total=" + giraffePlayer.getDuration());
            }

            @Override
            public void onTargetStateChange(int oldState, int newState) {
                DebugLog.e("oldState " + oldState + ", newState " + newState);
            }

            @Override
            public void onCurrentStateChange(int oldState, int newState) {
                DebugLog.e("oldState " + oldState + ", newState " + newState);
            }

            @Override
            public void onDisplayModelChange(int oldModel, int newModel) {
                DebugLog.e("oldModel " + oldModel + ", newModel " + newModel);
            }

            @Override
            public void onPreparing(GiraffePlayer giraffePlayer) {
                DebugLog.e("");
            }

            @Override
            public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {
                DebugLog.e("");
            }

            @Override
            public void onLazyLoadProgress(GiraffePlayer giraffePlayer, int progress) {
                DebugLog.e("progress " + progress);
            }

            @Override
            public void onLazyLoadError(GiraffePlayer giraffePlayer, String message) {
                DebugLog.e("message " + message);
            }
        });
    }

    public void refreshTitle() {
        mediaController.refreshTitle();
    }
}