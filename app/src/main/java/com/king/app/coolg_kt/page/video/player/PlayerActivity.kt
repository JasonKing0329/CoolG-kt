package com.king.app.coolg_kt.page.video.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.animation.*
import androidx.lifecycle.Observer
import cn.jzvd.Jzvd
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityVideoPlayerBinding
import com.king.app.coolg_kt.model.bean.PlayList
import com.king.app.coolg_kt.view.widget.video.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/18 14:29
 */
class PlayerActivity: BaseActivity<ActivityVideoPlayerBinding, PlayerViewModel>() {

    companion object {
        val EXTRA_AUTO_PLAY = "auto_play"
        fun startPage(context: Context, autoPlay: Boolean) {
            var intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(EXTRA_AUTO_PLAY, autoPlay)
            context.startActivity(intent)
        }
    }

    private val ftList =
        PlayListFragment()

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun getContentView(): Int = R.layout.activity_video_player

    override fun createViewModel(): PlayerViewModel = generateViewModel(
        PlayerViewModel::class.java)

    override fun initView() {
        hideBottomUIMenu()

        mBinding.ftList.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.ft_list, ftList, "PlayListFragment")
            .commit()

        mBinding.videoView.onBackListener = FullJzvd.OnBackListener { onBackPressed() }
        mBinding.videoView.onVideoListener = object : OnVideoListener {
            override fun getStartSeek(): Int {
                return mModel.startSeek
            }

            override fun onPlayComplete() {
                mModel.resetPlayInDb()
                mModel.playNext()
            }

            override fun updatePlayPosition(currentPosition: Long) {
                mModel.updatePlayPosition(currentPosition)
            }

            override fun onPause() {
                mModel.updatePlayToDb()
            }

            override fun onDestroy() {
                mModel.updatePlayToDb()
            }

            override fun onStart() {

            }
        }
        mBinding.videoView.onVideoClickListener =
            OnVideoClickListener { dismissPlayList() }
        mBinding.videoView.onVideoDurationListener =
            OnVideoDurationListener { duration -> mModel.updateDuration(duration) }
        mBinding.videoView.onVideoListListener = object : OnVideoListListener {
            override fun playNext() {
                mModel.updatePlayToDb()
                mModel.playNext()
            }

            override fun showPlayList() {
                showList()
            }

            override fun playPrevious() {
                mModel.updatePlayToDb()
                mModel.playPrevious()
            }
        }
    }

    override fun initData() {
        mModel.closeListObserver.observe(this, Observer{ dismissPlayList() })
        mModel.prepareVideo.observe(this, Observer{ bean -> prepareItem(bean) })
        mModel.playVideo.observe(this, Observer{ bean -> playItem(bean) })
        mModel.stopVideoObserver.observe(this, Observer{ mBinding.videoView.pause() })
        mModel.videoUrlIsReady.observe(this, Observer{ bean -> urlIsReady(bean) })

        mModel.loadPlayItems(isInitAutoPlay())
    }

    private fun prepareItem(bean: PlayList.PlayItem) {
        if (bean.url == null) {
            mModel.loadPlayUrl(bean)
        }
        else {
            mBinding.videoView.setPlayUrl(bean.url, bean.name)
        }
    }

    private fun urlIsReady(bean: PlayList.PlayItem) {
        mBinding.videoView.setPlayUrl(bean.url, bean.name)
    }

    private fun playItem(bean: PlayList.PlayItem) {
        mBinding.videoView.startVideo()
    }

    private fun dismissPlayList() {
        if (mBinding.ftList.visibility !== View.GONE) {
            mBinding.ftList.startAnimation(listDisappear())
        }
    }

    private fun hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT in 12..18) { // lower api
            window.decorView.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = uiOptions
        }
    }

    private fun isInitAutoPlay(): Boolean {
        return intent.getBooleanExtra(EXTRA_AUTO_PLAY, true)
    }

    private fun showList() {
        if (mBinding.ftList.visibility != View.VISIBLE) {
            mBinding.ftList.startAnimation(listAppear())
        }
    }

    override fun onDestroy() {
        mModel?.updatePlayToDb()
        Jzvd.releaseAllVideos()
        PlayListInstance.getInstance().destroy()
        super.onDestroy()
    }

    private fun listAppear(): Animation {
        mBinding.ftList.visibility = View.VISIBLE
        val set = AnimationSet(true)
        set.duration = 500
        set.interpolator = DecelerateInterpolator()
        val translate = TranslateAnimation(mBinding.ftList.width.toFloat(), 0f, 0f, 0f)
        set.addAnimation(translate)
        val scale = ScaleAnimation(0f, 1f, 1f, 1f)
        set.addAnimation(scale)
        return set
    }

    private fun listDisappear(): Animation {
        val set = AnimationSet(true)
        set.duration = 500
        set.interpolator = DecelerateInterpolator()
        set.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mBinding.ftList.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        val translate = TranslateAnimation(
            0f,
            mBinding.ftList.width.toFloat(), 0f, 0f
        )
        set.addAnimation(translate)
        val scale = ScaleAnimation(1f, 0f, 1f, 1f)
        set.addAnimation(scale)
        return set
    }

}