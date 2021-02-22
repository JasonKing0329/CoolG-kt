package com.king.app.coolg_kt.page.tv.player

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.VideoView
import androidx.lifecycle.Observer
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityTvPlayerSystemBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.tv.popup.PlayerSetting
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.TvDialogFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * @description:
 * 经实测，饺子播放器的默认JZMediaSystem引擎在小米电视上只出声音，不出画面（一直停止于转圈画面）
 * 切换为JZMediaIjk的引擎才能正常播放，所以可以用IjkPlayerActivity播放url，但是ijkplayer引擎又有个问题：不支持mkv和rmvb模式
 * 而且用饺子播放器其layout的controller部分基本上没法处理焦点问题，所以还是使用原生VideoView来做播放器
 * @author：Jing
 * @date: 2021/2/14 13:23
 */
class SystemPlayerActivity:BaseActivity<ActivityTvPlayerSystemBinding, SystemPlayerViewModel>() {

    companion object {
        val EXTRA_URL = "url"
        val EXTRA_PATH = "path"
        fun startPage(context: Context, url: String, pathInServer: String?) {
            var intent = Intent(context, SystemPlayerActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_PATH, pathInServer)
            context.startActivity(intent)
        }
    }

    private lateinit var videoController: VideoController

    private var hideControlDisposable: Disposable? = null
    private var timeDisposable: Disposable? = null

    private val TIME_DISP_CTRLBAR = 5000L

    override fun getContentView(): Int = R.layout.activity_tv_player_system

    override fun createViewModel(): SystemPlayerViewModel = generateViewModel(
        SystemPlayerViewModel::class.java)

    override fun initView() {
        mModel.currentUrl = intent.getStringExtra(EXTRA_URL)
        mModel.currentPathInServer = intent.getStringExtra(EXTRA_PATH)

        videoController = VideoController(
            this,
            mBinding.videoView
        )

        mBinding.ivBack.setOnClickListener { onBackPressed() }
        mBinding.start.setOnClickListener {
            if (mBinding.videoView.isPlaying) {
                hideControlDisposable?.dispose()
                videoController.pause()
                updatePlayIcon(false)
            }
            else {
                mBinding.videoView.start()
                startControlBarTimer()
                updatePlayIcon(true)
            }
        }
        mBinding.appVideoNext.setOnClickListener {
            setVideoLoading(true)
            if (!videoController.forward()) {
                setVideoLoading(false)
                showMessageShort("Can't forward anymore!")
            }
        }
        mBinding.appVideoLast.setOnClickListener {
            setVideoLoading(true)
            if (!videoController.backward()) {
                setVideoLoading(false)
                showMessageShort("Can't backward anymore!")
            }
        }
        mBinding.tvFromStart.setOnClickListener {
            setVideoLoading(true)
            videoController.playFromStart()
        }
        mBinding.ivSetting.setOnClickListener {
            val content = PlayerSetting()
            val dialog = TvDialogFragment()
            dialog.contentFragment = content
            dialog.title = "设置"
            dialog.setSize((ScreenUtils.getScreenWidth() * 0.4).toInt(), ScreenUtils.getScreenHeight() / 2)
            dialog.show(supportFragmentManager, "PlayerSetting")
        }
        mBinding.bottomSeekProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                setVideoLoading(true)
                videoController.seekTo(seekBar.progress)
            }
        })

        videoController.videoService = object :
            VideoService {
            override fun onClickVideoView(view: VideoView) {
                changeControlBarStatus()
            }

            override fun onPlayForward(view: VideoView, progress: Int) {

                //play的情况timeProgressor会通知更新
                if (videoController.isPaused) {
                    changeProgress()
                }
            }

            override fun onPrepared(view: VideoView, player: MediaPlayer) {
                // 获取并显示总时间
                mBinding.total.text = videoController.duration
                mBinding.current.text = "00:00:00"
                mBinding.bottomSeekProgress.max = videoController.durationTime

                // 取消loading进度，并设置seekTo监听，结束时停止loading进度
                setVideoLoading(false)
                player.setOnSeekCompleteListener {
                    DebugLog.e("VideoController onSeekComplete")
                    setVideoLoading(false)
                }
                player.setOnBufferingUpdateListener { mp, percent ->

                }

                // 开始控制栏消失计时
                if (mBinding.layoutBottom.visibility == View.VISIBLE) {
                    startControlBarTimer()
                }

                // 搜索字幕
                mBinding.subtitleView.bindToMediaPlayer(player)
                mModel.searchSubtitle(mModel.currentPathInServer)

                // 跳转记录的时间
                if (SettingProperty.isRememberTvPlayTime()) {
                    val seekTo = mModel.findRememberTime(mModel.currentUrl)
                    if (seekTo > 0) {
                        setVideoLoading(true)
                        player.seekTo(seekTo)
                    }
                }
            }

            override fun onPlayBackward(view: VideoView, progress: Int) {

                //play的情况timeProgressor会通知更新
                if (videoController.isPaused) {
                    changeProgress()
                }
            }

            override fun onCompletion(view: VideoView, player: MediaPlayer) {
                if (SettingProperty.isAutoPlayNextTv()) {
                    mModel.nextVideo()
                }
            }

            override fun onError(
                view: VideoView,
                player: MediaPlayer,
                fwError: Int,
                impError: Int
            ) {
                showMessageShort("Error")
            }
        }
    }

    private fun setVideoLoading(isLoading: Boolean) {
        mBinding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun updatePlayIcon(isPlaying: Boolean) {
        if (isPlaying) {
            mBinding.start.setImageResource(R.drawable.ic_stop_white_36dp)
        }
        else {
            mBinding.start.setImageResource(R.drawable.ic_play_arrow_white_36dp)
        }

    }

    override fun initData() {
        mModel.subtitles.observe(this, Observer {
            AlertDialogFragment()
                .setItems(mModel.toArrays(it), DialogInterface.OnClickListener { dialog, which ->
                    val url = UrlUtil.toVideoUrl(it[which].sourceUrl)
                    mBinding.subtitleView.setSubtitlePath(url)
                })
                .show(supportFragmentManager, "AlertDialogFragment")
        })
        mModel.playNextVideo.observe(this, Observer { playVideo(mModel.currentUrl) })
        playVideo(mModel.currentUrl)
    }

    private fun playVideo(url: String) {
        val name = url.substring(url.lastIndexOf("/"))
        mBinding.videoView.setVideoURI(Uri.parse(url))
        mBinding.title.text = name

        setVideoLoading(true)
        videoController.play()
        loopCurrentTime()
        updatePlayIcon(true)
    }

    private fun changeControlBarStatus() {
        if (mBinding.layoutBottom.visibility == View.VISIBLE) {
            mBinding.layoutBottom.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.video_control_disappear
                )
            )
            mBinding.layoutBottom.visibility = View.INVISIBLE
            mBinding.layoutTop.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.video_topbar_disappear
                )
            )
            mBinding.layoutTop.visibility = View.INVISIBLE
        } else {
            mBinding.layoutBottom.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.video_control_appear
                )
            )
            mBinding.layoutBottom.visibility = View.VISIBLE
            mBinding.layoutTop.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    R.anim.video_topbar_appear
                )
            )
            mBinding.layoutTop.visibility = View.VISIBLE
        }
    }

    private fun startControlBarTimer() {
        hideControlDisposable?.dispose()
        hideControlDisposable = Observable.timer(TIME_DISP_CTRLBAR, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (mBinding.layoutBottom.visibility == View.VISIBLE) {
                    changeControlBarStatus()
                }
            }
    }

    fun changeProgress() {
        mBinding.current.text = videoController.currentTimeString
        mBinding.bottomSeekProgress.progress = videoController.currentTime
    }

    private fun loopCurrentTime() {
        timeDisposable?.dispose()
        timeDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { changeProgress() }
    }

    override fun onBackPressed() {
        mModel.updatePlayTime(mModel.currentUrl, videoController.currentTime)
        super.onBackPressed()
    }

    override fun onDestroy() {
        hideControlDisposable?.dispose()
        timeDisposable?.dispose()
        super.onDestroy()
    }

    private fun isCenterKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val uniqueDown = (event.repeatCount == 0
                && event.action == KeyEvent.ACTION_DOWN)
        DebugLog.e("keyCode=${event.keyCode} isUnique=$uniqueDown")
        if (uniqueDown) {
            // 点击中心键的事件
            if (isCenterKey(event.keyCode)) {
                // 控制栏显示时，焦点不在任何有事件的控件上，点击中心键控制栏消失
                if (mBinding.layoutBottom.visibility == View.VISIBLE) {
                    if (!mBinding.start.isFocused && !mBinding.appVideoNext.isFocused
                        && !mBinding.appVideoLast.isFocused && !mBinding.ivBack.isFocused && !mBinding.ivSetting.isFocused) {
                        videoController.performClickVideo()
                    }
                }
                // 控制栏不显示时，点击中心键显示控制栏并暂停
                else {
                    videoController.performClickVideo()
                    mBinding.start.requestFocus()
                }
            }
            else {
                when(event.keyCode) {
                    // 上下左右键
                    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // 控制栏消失时，唤起控制栏
                        if (mBinding.layoutBottom.visibility != View.VISIBLE) {
                            videoController.performClickVideo()
                        }
                        // 控制栏显示时，保持激活
                        else {
                            startControlBarTimer()
                        }
                    }
                    // 返回键，记录视频播放时间
                    KeyEvent.KEYCODE_BACK -> {
                        mModel.updatePlayTime(mModel.currentUrl, videoController.currentTime)
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}