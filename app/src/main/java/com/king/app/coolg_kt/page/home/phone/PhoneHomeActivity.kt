package com.king.app.coolg_kt.page.home.phone

import android.app.Activity
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityHomeBinding
import com.king.app.coolg_kt.page.home.HomeRecord
import com.king.app.coolg_kt.page.home.HomeStar
import com.king.app.coolg_kt.page.home.HomeViewModel
import com.king.app.coolg_kt.page.login.LoginActivity
import com.king.app.coolg_kt.page.match.MatchHomeActivity
import com.king.app.coolg_kt.page.record.pad.PadRecordListActivity
import com.king.app.coolg_kt.page.record.pad.RecordPadActivity
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.star.pad.StarsPadActivity
import com.king.app.coolg_kt.page.star.phone.StarActivity
import com.king.app.coolg_kt.page.star.phone.TagStarActivity
import com.king.app.coolg_kt.page.studio.phone.StudioActivity
import com.king.app.coolg_kt.page.video.order.PlayOrderActivity
import com.king.app.coolg_kt.page.video.phone.VideoHomePhoneActivity
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import eightbitlab.com.blurview.RenderScriptBlur
import kotlin.math.abs

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/23 10:04
 */
class PhoneHomeActivity: BaseActivity<ActivityHomeBinding, HomeViewModel>() {

    private val REQUEST_VIDEO_ORDER = 101

    var adapter = HomeAdapter()

    override fun getContentView(): Int = R.layout.activity_home

    override fun createViewModel(): HomeViewModel = generateViewModel(HomeViewModel::class.java)

    override fun initView() {
        fullscreen()

        mBinding.model = mModel

        setupBlurView()
        setupMenu()
        mBinding.btnTop.setOnClickListener {
            mBinding.rvList.scrollToPosition(0)
        }
        mBinding.btnHome.setOnClickListener {
            LoginActivity.startAsSuperUser(this)
            finish()
        }

        mBinding.rvList.setEnableLoadMore(true)
        var manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position)
            }
        }
        mBinding.rvList.layoutManager = manager
        mBinding.rvList.setOnLoadMoreListener { mModel.loadMore() }
        mBinding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var first = manager.findFirstVisibleItemPosition()
                mBinding.tvDate.text = adapter.getItemDate(first)
            }
        })

        adapter.onListListener = object : HomeAdapter.OnListListener {
            override fun onLoadMore() {
                mModel.loadMore()
            }

            override fun onClickRecord(view: View, position: Int, record: HomeRecord) {

                if (ScreenUtils.isTablet()) {
                    RecordPadActivity.startPage(this@PhoneHomeActivity, record.bean.bean.id!!)
                }
                else {
                    RecordActivity.startPage(this@PhoneHomeActivity, record.bean.bean.id!!)
                }
            }

            override fun onClickStar(view: View, position: Int, star: HomeStar) {
                StarActivity.startPage(this@PhoneHomeActivity, star.bean.bean.starId)
            }

            override fun onAddPlay(record: HomeRecord) {
                mModel.saveRecordToAddViewOrder(record.bean.bean)
                PlayOrderActivity.startPageToSelect(this@PhoneHomeActivity, REQUEST_VIDEO_ORDER)
            }
        }
    }

    private fun setupMenu() {
        mBinding.groupMenuRecord.visibility = View.GONE
        mBinding.groupMenuStar.visibility = View.GONE
        mBinding.groupMenuVideo.visibility = View.GONE
        mBinding.groupMenuStudio.visibility = View.GONE
        mBinding.tvMatch.visibility = View.GONE
        mBinding.btnMenu.setOnClickListener {
            if (!isAnimating) {
                if (mBinding.blurView.visibility != View.VISIBLE) {
                    appearMenu()
                }
                else{
                    disappearMenu()
                }
            }
        }
        mBinding.blurView.setOnTouchListener(touchListener)
        mBinding.groupMenuStar.setOnClickListener {
            if (ScreenUtils.isTablet()) {
                StarsPadActivity.startPage(this)
            }
            else {
                TagStarActivity.startPage(this)
            }
        }
        mBinding.groupMenuRecord.setOnClickListener {
            if (ScreenUtils.isTablet()) {
                PadRecordListActivity.startPage(this)
            }
            else {
                PhoneRecordListActivity.startPage(this)
            }
        }
        mBinding.groupMenuVideo.setOnClickListener { VideoHomePhoneActivity.startPage(this) }
        mBinding.groupMenuStudio.setOnClickListener { StudioActivity.startPage(this) }
        mBinding.tvMatch.setOnClickListener { MatchHomeActivity.startPage(this) }
    }

    /**
     * blurView的onClick事件太容易触发，没有控制时间和距离。通过onTouch来控制，不过这样就得消耗touch事件，让底层的list滑动不起来了
     */
    var downTime = 0L
    var downX = 0f
    var downY = 0f
    private var touchListener = View.OnTouchListener { v, event ->
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                downTime = System.currentTimeMillis()
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_UP -> {
                var moveTime = System.currentTimeMillis() - downTime
                var moveX = event.x - downX
                var moveY = event.y - downY
                DebugLog.e("moveTime=$moveTime, moveX=$moveX, moveY=$moveY")
                if (moveTime < 300 && abs(moveX) < 50 && abs(moveY) < 50) {
                    if (!isAnimating) {
                        disappearMenu()
                    }
                }
            }
        }
        true
    }

    private var isAnimating = false
    private var animTime: Long = 700

    private fun appearMenu() {
        // blur view
        isAnimating = true
        mBinding.blurView.visibility = View.VISIBLE
        var anim = AlphaAnimation(0f, 1f)
        anim.duration = animTime
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                isAnimating = false
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        mBinding.blurView.startAnimation(anim)

        // menu items
        var trans = TranslateAnimation(TranslateAnimation.RELATIVE_TO_PARENT, 1f, TranslateAnimation.RELATIVE_TO_PARENT,
            0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f)
        trans.duration = animTime
        mBinding.groupMenuStar.visibility = View.VISIBLE
        mBinding.groupMenuRecord.visibility = View.VISIBLE
        mBinding.groupMenuVideo.visibility = View.VISIBLE
        mBinding.groupMenuStudio.visibility = View.VISIBLE
        mBinding.tvMatch.visibility = View.VISIBLE
        mBinding.groupMenuStar.startAnimation(trans)
        mBinding.groupMenuRecord.startAnimation(trans)
        mBinding.groupMenuVideo.startAnimation(trans)
        mBinding.groupMenuStudio.startAnimation(trans)
        mBinding.tvMatch.startAnimation(trans)
    }

    private fun disappearMenu() {
        isAnimating = true
        var anim = AlphaAnimation(1f, 0f)
        anim.duration = animTime
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                mBinding.blurView.visibility = View.GONE
                isAnimating = false
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        mBinding.blurView.startAnimation(anim)

        // 很奇怪只要这几个item用了消失的动画，下次出现就会被blurView覆盖。只要不设置消失的动画，改为立马设置为gone，下一次才能出现在正确的图层上
        mBinding.groupMenuStar.visibility = View.GONE
        mBinding.groupMenuRecord.visibility = View.GONE
        mBinding.groupMenuVideo.visibility = View.GONE
        mBinding.groupMenuStudio.visibility = View.GONE
        mBinding.tvMatch.visibility = View.GONE
    }

    private fun setupBlurView() {
        //set background, if your root layout doesn't have one
        val windowBackground = window.decorView.background
        val radius = 10f
        mBinding.blurView.setupWith(mBinding.root)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
            .setHasFixedTransformationMatrix(true);
    }

    override fun initData() {

        mModel.dataLoaded.observe(this, Observer {
            if (mBinding.rvList.adapter == null) {
                adapter.list = mModel.viewList
                mBinding.rvList.adapter = adapter
            }
            else {
                adapter.list = mModel.viewList
                adapter.notifyDataSetChanged()
            }
        })
        mModel.newRecordsObserver.observe(this, Observer { count ->
            var start = adapter.itemCount - count
            DebugLog.e("start=$start, count=$count")
            adapter.notifyItemRangeInserted(start, count)
        })

        mModel.createMenuIconUrl()
        mModel.loadData()
    }

    override fun onBackPressed() {
        if (mBinding.blurView.visibility == View.VISIBLE) {
            if (!isAnimating) {
                disappearMenu()
            }
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_VIDEO_ORDER -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (resultCode == RESULT_OK) {
                        val list = data?.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
                        mModel.insertToPlayList(list)
                    }
                }
            }
        }
    }
}