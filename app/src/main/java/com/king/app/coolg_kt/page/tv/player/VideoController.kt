package com.king.app.coolg_kt.page.tv.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.VideoData
import com.king.app.coolg_kt.model.log.CoolLogger
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.video.TvVideoView

class VideoController(private val mContext: Context, private val videoView: TvVideoView) :
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {

    /**
     * 记录视频按原始比例拉伸后的大小
     */
    var mOriginWidth = ScreenUtils.getScreenWidth()
    var mOriginHeight = ScreenUtils.getScreenHeight()

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
        currentPosition = 0
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

        CoolLogger.logTv("VideoController onCompletion currentPosition=$currentPosition, total=${videoView.duration}")
        // 在tv端经常能碰到中途突然onCompletion，这种情况下进行重新加载
        if (videoView.duration - currentPosition > 5000) {// 以5秒作为判断阈值
            CoolLogger.logTv("onUnexpectedTerminate $currentPosition")
            videoService?.onUnexpectedTerminate(currentPosition)
        }
        else {
            currentPosition = 0
            isPaused = true
            videoService?.onCompletion(videoView, player)
        }
    }

    override fun onPrepared(player: MediaPlayer) {
        CoolLogger.logTv("VideoController OnPrepared")
        player.setOnVideoSizeChangedListener { mp, width, height ->
            resetScale(player.videoWidth, player.videoHeight)
        }
        videoService?.onPrepared(videoView, player)
    }

    /**
     * 根据原始比例拉伸至整个控件
     */
    private fun resetScale(videoWidth: Int, videoHeight: Int) {
        var maxWidth = ScreenUtils.getScreenWidth()
        var maxHeight = ScreenUtils.getScreenHeight()
        val scaleWidget = maxWidth.toFloat() / maxHeight.toFloat()
        val scaleVideo = videoWidth.toFloat() / videoHeight.toFloat()
        // 以高为基准，重新计算宽
        if (scaleWidget > scaleVideo) {
            val scale = maxHeight.toFloat() / videoHeight.toFloat()
            mOriginWidth = (videoWidth * scale).toInt()
            mOriginHeight = maxHeight
        } else {
            val scale = maxWidth.toFloat() / videoWidth.toFloat()
            mOriginWidth = maxWidth
            mOriginHeight = (videoHeight * scale).toInt()
        }
        DebugLog.e("width=$mOriginWidth, height=$mOriginHeight")
        var param = videoView.layoutParams
        param.width = mOriginWidth
        param.height = mOriginHeight
        videoView.layoutParams = param
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        CoolLogger.logTv("VideoController onInfo what=$what extra=$extra")
        return false
    }

    override fun onError(
        player: MediaPlayer,
        framework_err: Int,
        impl_err: Int
    ): Boolean {
        CoolLogger.logTv("VideoController onError framework_err=$framework_err impl_err=$impl_err")
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
                    if (time - downTime < 200) {
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

    private fun getControlTime(): Int {
        return try {
            AppConstants.timeParamValues[SettingProperty.getForwardUnit()] * 1000
        } catch (e: Exception) {
            5000
        }
    }

    fun getForward(dis: Float): Int {
        val total = videoView.duration
        val remain = total - currentPosition
        val factor = getControlTime()
        val disFactor = 100
        val progress = dis.toInt() / disFactor * factor
        return if (progress < remain - factor) {
            progress
        } else -1
    }

    fun getBackward(dis: Float): Int {
        val remain = currentPosition
        val factor = getControlTime()
        val disFactor = 100
        val progress = dis.toInt() / disFactor * factor
        return if (progress < remain - factor) {
            progress
        } else -1
    }

    fun backward(): Boolean {
        val factor = getControlTime()
        val remain = currentPosition
        if (factor < remain - factor) {
            currentPosition -= factor
            videoView.seekTo(currentPosition)
            videoService?.onPlayBackward(videoView, factor)
            return true
        }
        return false
    }

    fun forward(): Boolean {
        val total = videoView.duration
        val remain = total - currentPosition
        val factor = getControlTime()
        if (factor < remain - factor) {
            currentPosition += factor
            videoView.seekTo(currentPosition)
            videoService?.onPlayForward(videoView, factor)
            return true
        }
        return false
    }

    fun playFromStart() {
        currentPosition = 0
        val isPlaying = videoView.isPlaying
        videoView.seekTo(0)
        if (!isPlaying) {
            videoView.start()
        }
    }

}