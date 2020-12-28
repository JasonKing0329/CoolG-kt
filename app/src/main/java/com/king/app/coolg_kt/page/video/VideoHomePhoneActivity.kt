package com.king.app.coolg_kt.page.video

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityVideoPhoneBinding
import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.bean.VideoGuy
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.model.setting.ViewProperty
import com.king.app.coolg_kt.page.pub.BannerSettingFragment
import com.king.app.coolg_kt.page.pub.BannerSettingFragment.OnAnimSettingListener
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.page.record.popup.RecommendFragment
import com.king.app.coolg_kt.page.record.popup.RecommendFragment.OnRecommendListener
import com.king.app.coolg_kt.page.video.VideoRecAdapter.OnPlayListener
import com.king.app.coolg_kt.page.video.order.PlayOrderActivity
import com.king.app.coolg_kt.page.video.phone.HomeAdapter
import com.king.app.coolg_kt.utils.BannerHelper
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.widget.video.OnPlayEmptyUrlListener
import com.king.app.coolg_kt.view.widget.video.UrlCallback
import tcking.github.com.giraffeplayer2.PlayerManager

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/22 15:26
 */
class VideoHomePhoneActivity : BaseActivity<ActivityVideoPhoneBinding, VideoHomeViewModel>() {
    private val REQUEST_INSERT_TO_PLAY_ORDER = 6051
    private val REQUEST_SET_PLAY_ORDER = 6052
    private val REQUEST_ENTER_PLAY_ORDER = 6053
    private var adapter = HomeAdapter()
    private var recAdapter = VideoRecAdapter()

    override fun createViewModel(): VideoHomeViewModel = generateViewModel(VideoHomeViewModel::class.java)
    
    override fun getContentView(): Int = R.layout.activity_video_phone

    override fun initView() {
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_refresh -> if (mBinding.banner.isEnableSwitch) {
                    mModel.loadRecommend()
                } else {
                    showMessageShort("Please stop video first!")
                }
                R.id.menu_desktop -> {
//                    Router.build("VideoServer")
//                        .go(this@VideoHomePhoneActivity)
                    TODO()
                }
                R.id.menu_recommend_setting -> {
                    val content = RecommendFragment()
                    content.isHideOnline = true
                    content.mBean = SettingProperty.getVideoRecBean()
                    content.onRecommendListener = object :
                        OnRecommendListener {
                        override fun onSetSql(bean: RecommendBean) {
                            mModel.updateRecommend(bean)
                        }
                    }
                    val dialogFragment = DraggableDialogFragment()
                    dialogFragment.setTitle("Recommend Setting")
                    dialogFragment.contentFragment = content
                    dialogFragment.maxHeight = ScreenUtils.getScreenHeight() * 2 / 3
                    dialogFragment.show(supportFragmentManager, "RecommendFragment")
                }
                R.id.menu_anim_setting -> showBannerSetting()
            }
        }
        mBinding.rvItems.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvItems.setEnableLoadMore(true)
        mBinding.rvItems.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildLayoutPosition(view)
                if (position > 0) {
                    outRect.top = ScreenUtils.dp2px(8f)
                }
            }
        })
        // 不自动加载更多
//        mBinding.rvItems.setOnLoadMoreListener(() -> mModel.loadMore());
        mBinding.fabPlay.setOnClickListener { playList(false) }

        // viewpager切换效果
        BannerHelper.setBannerParams(mBinding.banner, ViewProperty.getVideoHomeBannerParams())
    }

    override fun onResume() {
        super.onResume()
        mBinding.banner.startAutoPlay()
    }

    override fun onPause() {
        super.onPause()
        mBinding.banner.stopAutoPlay()
    }

    override fun initData() {
        adapter = HomeAdapter()
        adapter.onHeadActionListener = object : HomeAdapter.OnHeadActionListener {
            override fun onSetPlayList() {
                PlayOrderActivity.startPageToSelect(this@VideoHomePhoneActivity, REQUEST_SET_PLAY_ORDER)
            }

            override fun onPlayList() {
                PlayOrderActivity.startPageToSelect(this@VideoHomePhoneActivity, REQUEST_ENTER_PLAY_ORDER)
            }

            override fun onClickPlayList(order: VideoPlayList) {
//                Router.build("PlayList")
//                    .with(PlayListActivity.EXTRA_ORDER_ID, order.playOrder!!.id)
//                    .go(this@VideoHomePhoneActivity)
                TODO()
            }

            override fun onRefreshGuy() {
                mModel.loadHeadData()
            }

            override fun onGuy() {
//                Router.build("PopularStar")
//                    .go(this@VideoHomePhoneActivity)
                TODO()
            }

            override fun onClickGuy(guy: VideoGuy) {
//                Router.build("PlayStarList")
//                    .with(PlayStarListActivity.EXTRA_STAR_ID, guy.star!!.id)
//                    .go(this@VideoHomePhoneActivity)
                TODO()
            }
        }
        adapter.onListListener =
            object : HomeAdapter.OnListListener {
                override fun onLoadMore() {
                    mModel.loadMore()
                }

                override fun onClickItem(position: Int, bean: PlayItemViewBean) {
                    RecordActivity.startPage(this@VideoHomePhoneActivity, bean.record!!.bean.id!!)
                }

                override fun onAddToVideoOrder(bean: PlayItemViewBean) {
                    PlayOrderActivity.startPageToSelect(this@VideoHomePhoneActivity, REQUEST_INSERT_TO_PLAY_ORDER)
                }
            }
        adapter.onPlayEmptyUrlListener =
            OnPlayEmptyUrlListener { fingerprint, callback ->
                val position = fingerprint.toInt()
                mModel.getRecentPlayUrl(position, callback)
            }
        mBinding.rvItems.adapter = adapter
        mModel.headDataObserver.observe(
            this,
            Observer { data: VideoHeadData? ->
                adapter.setHeadData(data)
                adapter!!.notifyDataSetChanged()
            }
        )
        mModel.recentVideosObserver.observe(
            this,
            Observer<List<PlayItemViewBean?>> { list: List<PlayItemViewBean?>? ->
                adapter!!.list = list
                adapter!!.notifyDataSetChanged()
            }
        )
        // 获取url失败，重新启动轮播
        mModel.getPlayUrlFailed.observe(
            this,
            Observer { failed: Boolean? ->
                mBinding.banner.startAutoPlay()
                mBinding.banner.isEnableSwitch = true
            }
        )
        mModel.recommendObserver.observe(
            this,
            Observer<List<PlayItemViewBean?>> { list: List<PlayItemViewBean?> ->
                mBinding.banner.stopAutoPlay()
                if (list.size == 0) {
                    showMessageShort("No video to recommend")
                    mBinding.banner.adapter = null
                    return@observe
                }
                recAdapter = VideoRecAdapter()
                recAdapter!!.list = list
                recAdapter!!.interceptFullScreen = true
                // 只要按下播放键就停止轮播
                // url尚未获取，需要先获取url
                recAdapter!!.onPlayEmptyUrlListener =
                    OnPlayEmptyUrlListener { fingerprint: String, callback: UrlCallback? ->
                        mBinding.banner.stopAutoPlay()
                        mBinding.banner.isEnableSwitch = false
                        val position = fingerprint.toInt()
                        mModel.getRecommendPlayUrl(position, callback!!)
                    }
                recAdapter!!.onPlayListener = object : OnPlayListener {
                    override fun onStartPlay() {
                        // 有可能是url已获取的情况按播放键直接播放了
                        mBinding.banner.stopAutoPlay()
                        mBinding.banner.isEnableSwitch = false
                    }

                    override fun onPausePlay() {
                        mBinding.banner.startAutoPlay()
                        mBinding.banner.isEnableSwitch = true
                    }

                    override fun onClickPlayItem(item: PlayItemViewBean?) {
                        Router.build("RecordPhone")
                            .with(RecordActivity.EXTRA_RECORD_ID, item!!.record.getId())
                            .go(this@VideoHomePhoneActivity)
                    }

                    override fun onInterceptFullScreen(item: PlayItemViewBean?) {
                        mModel.playItem(item!!)
                    }
                }
                mBinding.banner.adapter = recAdapter
                mBinding.banner.startAutoPlay()
            }
        )
        mModel.videoPlayOnReadyObserver.observe(
            this,
            Observer { play: Boolean? -> playList(true) }
        )
        mModel.buildPage()
    }

    private fun playList(autoPlay: Boolean) {
        Router.build("Player")
            .with(PlayerActivity.EXTRA_AUTO_PLAY, autoPlay)
            .go(this)
    }

    private fun showBannerSetting() {
        val bannerSettingDialog = BannerSettingFragment()
        bannerSettingDialog.params = ViewProperty.getVideoHomeBannerParams()
        bannerSettingDialog.onAnimSettingListener = object : OnAnimSettingListener {
            fun onParamsUpdated(params: BannerParams?) {}
            fun onParamsSaved(params: BannerParams?) {
                ViewProperty.setVideoHomeBannerParams(params)
                BannerHelper.setBannerParams(mBinding.banner, params)
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = bannerSettingDialog
        dialogFragment.setTitle("Banner Setting")
        dialogFragment.show(supportFragmentManager, "BannerSettingFragment")
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_SET_PLAY_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                val list =
                    data!!.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
                mModel.updateVideoCoverPlayList(list)
            }
        } else if (requestCode == REQUEST_INSERT_TO_PLAY_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                val list =
                    data!!.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
                mModel.insertToPlayList(list)
            }
        } else if (requestCode == REQUEST_ENTER_PLAY_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                mModel.loadHeadData()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // videoView必须在manifest中所属activity指定
        // android:configChanges="orientation|screenSize",且其中两个参数缺一不可
        // 同时在onConfigurationChanged中加入相关代码。
        // 这样在点击全屏时才能顺畅地切换为全屏
        PlayerManager.getInstance().onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (PlayerManager.getInstance().onBackPressed()) {
            return
        }
        super.onBackPressed()
    }
}