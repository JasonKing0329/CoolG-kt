package com.king.app.coolg_kt.page.tv.player

import android.content.Context
import android.content.Intent
import cn.jzvd.Jzvd
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityTvPlayerIjkBinding
import com.king.app.coolg_kt.view.widget.video.OnVideoListener
import com.king.app.coolg_kt.view.widget.video.TvJzvd

/**
 * @description:
 * 经实测，饺子播放器的默认JZMediaSystem引擎在小米电视上只出声音，不出画面（一直停止于转圈画面）
 * 切换为JZMediaIjk的引擎才能正常播放，所以可以用IjkPlayerActivity播放url，但是ijkplayer引擎又有个问题：不支持mkv和rmvb模式
 * 而且用饺子播放器其layout的controller部分基本上没法处理焦点问题，所以还是使用原生VideoView来做播放器(SystemPlayerActivity)
 * @author：Jing
 * @date: 2021/2/13 23:49
 */
@Deprecated("not support mkv, rmvb", ReplaceWith("SystemPlayerActivity"))
class IjkPlayerActivity: BaseActivity<ActivityTvPlayerIjkBinding, IjkPlayerViewModel>() {

    companion object {
        val EXTRA_URL = "url"
        fun startPage(context: Context, url: String) {
            var intent = Intent(context, IjkPlayerActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            context.startActivity(intent)
        }
    }

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun getContentView(): Int = R.layout.activity_tv_player_ijk

    override fun createViewModel(): IjkPlayerViewModel = generateViewModel(
        IjkPlayerViewModel::class.java)

    override fun initView() {
        mBinding.videoView.onBackListener = TvJzvd.OnBackListener { onBackPressed() }
        mBinding.videoView.onVideoListener = object : OnVideoListener {
            override fun getStartSeek(): Int {
                return 0
            }

            override fun onPlayComplete() {

            }

            override fun updatePlayPosition(currentPosition: Long) {

            }

            override fun onPause() {

            }

            override fun onError() {

            }

            override fun onDestroy() {

            }

            override fun onStart() {

            }
        }
        val url = intent.getStringExtra(EXTRA_URL)
        val name = url.substring(url.lastIndexOf("/"))
        mBinding.videoView.playUrl(url, name)
    }

    override fun initData() {

    }

    override fun onBackPressed() {
        Jzvd.releaseAllVideos()
        super.onBackPressed()
    }
}