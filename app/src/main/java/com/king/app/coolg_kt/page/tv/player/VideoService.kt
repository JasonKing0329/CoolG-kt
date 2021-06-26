package com.king.app.coolg_kt.page.tv.player

import android.media.MediaPlayer
import android.widget.VideoView

interface VideoService {
    fun onClickVideoView(view: VideoView)
    fun onPlayForward(view: VideoView, progress: Int)
    fun onPlayBackward(view: VideoView, progress: Int)
    fun onCompletion(view: VideoView, player: MediaPlayer)
    fun onPrepared(view: VideoView, player: MediaPlayer)
    fun onError(
        view: VideoView,
        player: MediaPlayer,
        fwError: Int,
        impError: Int
    )

    fun onUnexpectedTerminate(progress: Int)
}