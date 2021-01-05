package com.king.app.coolg_kt.page.video.player

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.view.animation.*
import androidx.lifecycle.Observer
import cn.jzvd.Jzvd
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityVideoPlayerBinding
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

    private val ftList = PlayListFragment()

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun getContentView(): Int = R.layout.activity_video_player

    override fun createViewModel(): PlayerViewModel = generateViewModel(PlayerViewModel::class.java)

    override fun initView() {

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

            override fun onError() {
                showConfirmCancelMessage("Load video failed, do you want to fetch the url of current video?",
                    DialogInterface.OnClickListener { dialog, which -> mModel.reloadPlayUrl() },
                    null)
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
                if (mBinding.ftList.visibility == View.VISIBLE) {
                    dismissPlayList()
                }
                else {
                    showList()
                }
            }

            override fun playPrevious() {
                mModel.updatePlayToDb()
                mModel.playPrevious()
            }
        }
    }

    override fun initData() {
        mModel.closeListObserver.observe(this, Observer{ dismissPlayList() })

        // 第一次为setup，只装载不播放
        mModel.onlySetupVideo.observe(this, Observer{ bean -> mBinding.videoView.setUp(bean.url, bean.name) })
        // 后面的必播放（但是初始化如果列表为空，这里就要setUp后立即播放。其他情况直接changeUrl）
        mModel.playVideo.observe(this, Observer{ bean -> mBinding.videoView.playUrl(bean.url, bean.name) })

        mModel.stopVideoObserver.observe(this, Observer{ mBinding.videoView.pause() })
        mModel.focusToIndex.observe(this, Observer { ftList.focusToIndex(it) })
        mModel.itemsObserver.observe(this, Observer { ftList.showList(it) })
        mModel.askIfLoop.observe(this, Observer{
            showConfirmCancelMessage("There are no more videos to play. Do you want to play from beginning?",
                DialogInterface.OnClickListener { dialog, which ->  mModel.playFromBegin()},
                null)
        })
        mModel.retryLoadUrl.observe(this, Observer{
            showConfirmCancelMessage("Fetch url failed, do you wanna retry?",
                DialogInterface.OnClickListener { dialog, which -> it.retry()},
                null)
        })

        mModel.initAutoPlay = isInitAutoPlay()

        mBinding.ftList.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.ft_list, ftList, "PlayListFragment")
            .commit()
    }

    private fun dismissPlayList() {
        if (mBinding.ftList.visibility !== View.GONE) {
            mBinding.ftList.startAnimation(listDisappear())
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

    override fun onPause() {
        super.onPause()
        mModel?.updatePlayToDb()
        Jzvd.releaseAllVideos()
    }

    override fun onDestroy() {
        PlayListInstance.getInstance().destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
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