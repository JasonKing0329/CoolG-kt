package com.king.app.coolg_kt.page.home.phone

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityHomeBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.home.HomeRecord
import com.king.app.coolg_kt.page.home.HomeStar
import com.king.app.coolg_kt.page.home.HomeViewModel
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.star.phone.StarActivity
import com.king.app.coolg_kt.page.star.phone.TagStarActivity
import com.king.app.coolg_kt.utils.DebugLog
import eightbitlab.com.blurview.RenderScriptBlur

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/23 10:04
 */
class PhoneHomeActivity: BaseActivity<ActivityHomeBinding, HomeViewModel>() {

    var adapter = HomeAdapter()

    override fun isFullScreen(): Boolean = true

    override fun getContentView(): Int = R.layout.activity_home

    override fun createViewModel(): HomeViewModel = generateViewModel(HomeViewModel::class.java)

    override fun initView() {
        setupBlurView()
        setupMenu()
        mBinding.btnTop.setOnClickListener {
            mBinding.rvList.scrollToPosition(0)
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

        adapter.onListListener = object : HomeAdapter.OnListListener {
            override fun onLoadMore() {
                mModel.loadMore()
            }

            override fun onClickRecord(view: View, position: Int, record: HomeRecord) {
                RecordActivity.startPage(this@PhoneHomeActivity, record.bean.bean.id!!)
            }

            override fun onClickStar(view: View, position: Int, star: HomeStar) {
                StarActivity.startPage(this@PhoneHomeActivity, star.bean.bean.starId)
            }

            override fun onAddPlay(record: HomeRecord) {
                TODO()
            }
        }
    }

    private fun setupMenu() {
        mBinding.groupMenuRecord.visibility = View.GONE
        mBinding.groupMenuStar.visibility = View.GONE
        mBinding.groupMenuVideo.visibility = View.GONE
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
        mBinding.groupMenuStar.setOnClickListener { TagStarActivity.startPage(this) }
        mBinding.groupMenuRecord.setOnClickListener { PhoneRecordListActivity.startPage(this) }
        mBinding.groupMenuVideo.setOnClickListener {  }
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
        mBinding.groupMenuStar.startAnimation(trans)
        mBinding.groupMenuRecord.startAnimation(trans)
        mBinding.groupMenuVideo.startAnimation(trans)
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
                mBinding.groupMenuStar.visibility = View.GONE
                mBinding.groupMenuRecord.visibility = View.GONE
                mBinding.groupMenuVideo.visibility = View.GONE
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

        ImageBindingAdapter.setStarUrl(mBinding.ivMenuStar, ImageProvider.getStarRandomPath("", null))
        ImageBindingAdapter.setStarUrl(mBinding.ivMenuRecord, ImageProvider.getStarRandomPath("", null))
        ImageBindingAdapter.setStarUrl(mBinding.ivMenuVideo, ImageProvider.getStarRandomPath("", null))

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
            val start: Int = adapter.itemCount - count - 1
            DebugLog.e("start=$start, count=$count")
            adapter.notifyItemRangeInserted(start, count)
        })

        mModel.loadData()
    }
}