package com.king.app.coolg_kt.page.video

import android.view.View
import android.widget.TextView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.view.widget.video.EmbedJzvd
import com.king.app.coolg_kt.view.widget.video.OnPlayEmptyUrlListener
import com.king.app.coolg_kt.view.widget.video.OnVideoListener
import com.king.lib.banner.CoolBannerAdapter

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/25 14:49
 */
class VideoRecAdapter : CoolBannerAdapter<PlayItemViewBean>() {

    var onPlayEmptyUrlListener: OnPlayEmptyUrlListener? = null
    var onPlayListener: OnPlayListener? = null

    /**
     * 是否截获全屏事件
     */
    var interceptFullScreen = false

    override fun getLayoutRes(): Int {
        return R.layout.adapter_video_recommend
    }

    override fun onBindView(
        view: View,
        position: Int,
        bean: PlayItemViewBean
    ) {
        val videoView: EmbedJzvd = view.findViewById(R.id.video_view)
        val tvRank = view.findViewById<TextView>(R.id.tv_rank)
        val item = list[position]
        ImageBindingAdapter.setRecordUrl(videoView.posterImageView, item.cover)
        videoView.posterImageView.setOnClickListener { onPlayListener?.onClickPlayItem(item) }
        // 横屏下必须设置这个，否则从全屏返回会激活屏幕为portrait方向（GiraffePlayer源码的setDisplayModel(DISPLAY_NORMAL)可以看出从全屏到非全屏执行了请求屏幕方向SCREEN_ORIENTATION_PORTRAIT）
//        if (ScreenUtils.isTablet()) {
//            videoView.videoInfo.isPortraitWhenFullScreen = false
//        }
        videoView.setUp(item.playUrl, "")
        videoView.setOnPlayEmptyUrlListener(onPlayEmptyUrlListener)
        if (interceptFullScreen) {
            videoView.interceptFullScreenListener = View.OnClickListener {
                onPlayListener?.onInterceptFullScreen(bean)
            }
        }
        videoView.onVideoListener = object : OnVideoListener {
            override fun getStartSeek(): Int {
                return 0
            }

            fun updatePlayPosition(currentPosition: Int) {}
            override fun onPlayComplete() {
                onPlayListener?.onPausePlay()
            }

            override fun updatePlayPosition(currentPosition: Long) {

            }

            override fun onPause() {
                onPlayListener?.onPausePlay()
            }

            override fun onDestroy() {
                onPlayListener?.onPausePlay()
            }

            override fun onStart() {
                onPlayListener?.onStartPlay()
            }
        }
        val testView = view.findViewById<TextView>(R.id.tv_index)
        testView.text = position.toString() + "--" + item.name
        bean.record.countRecord?.let {
            tvRank.text = "R-${it.rank}"
        }
    }

    interface OnPlayListener {
        fun onStartPlay()
        fun onPausePlay()
        fun onClickPlayItem(item: PlayItemViewBean)
        fun onInterceptFullScreen(item: PlayItemViewBean)
    }
}