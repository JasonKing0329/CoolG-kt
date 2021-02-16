package com.king.app.coolg_kt.page.tv

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.VideoView
import com.king.app.coolg_kt.model.bean.VideoData
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.DebugLog

class VideoController(private val mContext: Context, private val videoView: VideoView) :
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

    var videoService: VideoService? = null
    private var currentPosition = 0
    private var srcPath: String? = null
    val videoData: VideoData? = null
    var isPaused = false
        private set

    init {
        videoView.setOnPreparedListener(this)
        videoView.setOnCompletionListener(this)
        videoView.setOnTouchListener(VideoTouchListener())
        videoView.setOnErrorListener(this)
    }

    fun setVideoPath(path: String?) {
        srcPath = path
        videoView.setVideoPath(path)
    }

    fun setVideoUri(uri: Uri) {
        videoView.setVideoURI(uri)
        srcPath = uri.path
    }

    fun play() {
        videoView.seekTo(currentPosition)
        videoView.start()
        isPaused = false
    }

    fun pause() {
        currentPosition = videoView.currentPosition
        videoView.pause()
        isPaused = true
    }

    fun stop() {
        videoView.pause()
        currentPosition = 0
        isPaused = true
    }

    fun seekTo(progress: Int) {
        DebugLog.e("VideoController seekTo $progress")
        videoView.seekTo(progress)
    }

    fun onPause() {
        if (videoView.isPlaying) {
            currentPosition = videoView.currentPosition
            videoView.pause()
        }
    }

    val duration: String
        get() {
            val time = videoView.duration.toLong()
            return VideoFormatter.formatTime(time)
        }

    val durationTime: Int
        get() = videoView.duration

    val currentTimeString: String
        get() {
            currentPosition = videoView.currentPosition
            return VideoFormatter.formatTime(currentPosition.toLong())
        }

    val currentTime: Int
        get() {
            currentPosition = videoView.currentPosition
            return currentPosition
        }

    override fun onCompletion(player: MediaPlayer) {
        DebugLog.e("VideoController onCompletion")
        currentPosition = 0
        isPaused = true
        videoService?.onCompletion(videoView, player)
    }

    override fun onPrepared(player: MediaPlayer) {
        DebugLog.e("VideoController OnPrepared")
        videoService?.onPrepared(videoView, player)
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        DebugLog.e("VideoController onInfo what=$what extra=$extra")
        return false
    }

    override fun onError(
        player: MediaPlayer,
        framework_err: Int,
        impl_err: Int
    ): Boolean {
        DebugLog.e("VideoController onError framework_err=$framework_err impl_err=$impl_err")
        videoService?.onError(videoView, player, framework_err, impl_err)
        return true //if false, it'll show framework popup dialog
    }

    private inner class VideoTouchListener : OnTouchListener {
        private val TAG = "VideoTouchListener"
        private var lastX = 0f
        private var lastY = 0f
        private var downTime: Long = 0
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    downTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP -> {
                    val x = event.x
                    val y = event.y
                    val time = System.currentTimeMillis()
                    Log.d(
                        TAG,
                        "time:" + (time - downTime) + ", disX:" + (x - lastX) + ", disY:" + (y - lastY)
                    )
                    if (x - lastX > 100 && y - lastY < 200 && y - lastY > -200) {
                        //forward
                        if (videoService != null) {
                            val progress = getForward(x - lastX)
                            if (progress != -1) {
                                currentPosition += progress
                                videoView.seekTo(currentPosition)
                                videoService!!.onPlayForward(videoView, progress)
                            }
                        }
                    } else if (x - lastX < -100 && y - lastY < 200 && y - lastY > -200) {
                        //backward
                        if (videoService != null) {
                            val progress = getBackward(lastX - x)
                            if (progress != -1) {
                                currentPosition -= progress
                                videoView.seekTo(currentPosition)
                                videoService!!.onPlayBackward(videoView, progress)
                            }
                        }
                    }
                    if (time - downTime < 100) {
                        if (x - lastX < 20 && x - lastX > -20 && y - lastY < 20 && y - lastY > -20) {
                            performClickVideo()
                        }
                    }
                }
                else -> {
                }
            }
            return true
        }
    }

    fun performClickVideo() {
        videoService?.onClickVideoView(videoView)
    }

    fun getForward(dis: Float): Int {
        val total = videoView.duration
        val remain = total - currentPosition
        val factor = SettingProperty.getForwardUnit() * 1000
        val disFactor = 100
        val progress = dis.toInt() / disFactor * factor
        return if (progress < remain - factor) {
            progress
        } else -1
    }

    fun getBackward(dis: Float): Int {
        val remain = currentPosition
        val factor = SettingProperty.getForwardUnit() * 1000
        val disFactor = 100
        val progress = dis.toInt() / disFactor * factor
        return if (progress < remain - factor) {
            progress
        } else -1
    }

    fun backward(): Boolean {
        val factor = SettingProperty.getForwardUnit() * 1000
        val remain = currentPosition
        if (factor < remain - factor) {
            currentPosition -= factor
            videoView.seekTo(currentPosition)
            if (videoService != null) {
                videoService!!.onPlayBackward(videoView, factor)
            }
            return true
        }
        return false
    }

    fun forward(): Boolean {
        val total = videoView.duration
        val remain = total - currentPosition
        val factor = SettingProperty.getForwardUnit() * 1000
        if (factor < remain - factor) {
            currentPosition += factor
            videoView.seekTo(currentPosition)
            if (videoService != null) {
                videoService!!.onPlayForward(videoView, factor)
            }
            return true
        }
        return false
    }
}