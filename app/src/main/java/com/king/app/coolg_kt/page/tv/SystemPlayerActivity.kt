package com.king.app.coolg_kt.page.tv

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
import com.king.app.coolg_kt.model.http.UrlProvider
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
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

    override fun createViewModel(): SystemPlayerViewModel = generateViewModel(SystemPlayerViewModel::class.java)

    override fun initView() {
        videoController = VideoController(this, mBinding.videoView)
        mBinding.start.requestFocus()

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
            if (!videoController.forward()) {
                showMessageShort("Can't forward anymore!")
            }
        }
        mBinding.appVideoLast.setOnClickListener {
            if (!videoController.backward()) {
                showMessageShort("Can't backward anymore!")
            }
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

        videoController.videoService = object : VideoService {
            override fun onClickVideoView(view: VideoView) {
                changeControlBarStatus()
                if (mBinding.layoutBottom.visibility == View.VISIBLE) {
                    startControlBarTimer()
                }
            }

            override fun onPlayForward(view: VideoView, progress: Int) {

                //play的情况timeProgressor会通知更新
                if (videoController.isPaused) {
                    changeProgress()
                }
            }

            override fun onPrepared(view: VideoView, player: MediaPlayer) {
                mBinding.total.text = videoController.duration
                mBinding.current.text = "00:00:00"
                mBinding.bottomSeekProgress.max = videoController.durationTime

                setVideoLoading(false)
                player.setOnSeekCompleteListener {
                    DebugLog.e("VideoController onSeekComplete")
                    setVideoLoading(false)
                }
                player.setOnBufferingUpdateListener { mp, percent ->
                    DebugLog.e("VideoController onBufferingUpdate percent=$percent")
                }

                mBinding.subtitleView.bindToMediaPlayer(player)
                mModel.searchSubtitle(intent.getStringExtra(EXTRA_PATH))
            }

            override fun onPlayBackward(view: VideoView, progress: Int) {

                //play的情况timeProgressor会通知更新
                if (videoController.isPaused) {
                    changeProgress()
                }
            }

            override fun onCompletion(view: VideoView, player: MediaPlayer) {

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
        val url = intent.getStringExtra(EXTRA_URL)
        val name = url.substring(url.lastIndexOf("/"))
        mBinding.videoView.setVideoURI(Uri.parse(url))
        mBinding.title.text = name

        mModel.subtitles.observe(this, Observer {
            AlertDialogFragment()
                .setItems(mModel.toArrays(it), DialogInterface.OnClickListener { dialog, which ->
                    val url = UrlUtil.toVideoUrl(it[which].sourceUrl)
                    mBinding.subtitleView.setSubtitlePath(url)
                })
                .show(supportFragmentManager, "AlertDialogFragment")
        })

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
        timeDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { changeProgress() }
    }

    override fun onDestroy() {
        hideControlDisposable?.dispose()
        timeDisposable?.dispose()
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val uniqueDown = (event.repeatCount == 0
                && event.action == KeyEvent.ACTION_DOWN)
        DebugLog.e("keyCode=${event.keyCode} isUnique=$uniqueDown")
        if (uniqueDown) {
            when(event.keyCode) {
                KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                    if (mBinding.layoutBottom.visibility != View.VISIBLE) {
                        videoController.performClickVideo()
                        mBinding.start.requestFocus()
                    }
                    else {
                        if (mBinding.videoView.isFocused) {
                            videoController.performClickVideo()
                        }
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}