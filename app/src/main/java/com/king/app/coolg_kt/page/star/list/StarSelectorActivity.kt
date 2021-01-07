package com.king.app.coolg_kt.page.star.list

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityStarSelectorBinding
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.FitSideBar.OnSidebarStatusListener

class StarSelectorActivity : BaseActivity<ActivityStarSelectorBinding, StarSelectorViewModel>() {

    companion object {
        const val RESP_SELECT_RESULT = "resp_select_result"
        const val EXTRA_SINGLE = "select_single"
        const val EXTRA_LIMIT_MAX = "select_limit_max"
        fun startPage(activity: Activity, singleSelect: Boolean, limitMax: Int, requestCode: Int) {
            var intent = Intent(activity, StarSelectorActivity::class.java)
            intent.putExtra(EXTRA_SINGLE, singleSelect)
            intent.putExtra(EXTRA_LIMIT_MAX, limitMax)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    override fun createViewModel(): StarSelectorViewModel = generateViewModel(StarSelectorViewModel::class.java)

    override fun getContentView(): Int {
        return R.layout.activity_star_selector
    }

    override fun initView() {
        if (ScreenUtils.isTablet()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            mBinding.rvStar.layoutManager = GridLayoutManager(this, 3)
            mBinding.rvStar.addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildLayoutPosition(view)
                    outRect.top = ScreenUtils.dp2px(8f)
                    when {
                        position % 3 == 0 -> {
                            outRect.left = ScreenUtils.dp2px(16f)
                        }
                        position % 3 == 1 -> {
                            outRect.left = ScreenUtils.dp2px(16f)
                        }
                        else -> {
                            outRect.left = ScreenUtils.dp2px(16f)
                            outRect.right = ScreenUtils.dp2px(8f)
                        }
                    }
                }
            })
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mBinding.rvStar.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
            mBinding.rvStar.addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.top = ScreenUtils.dp2px(8f)
                }
            })
        }
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.showConfirmStatus(0, true, "Ok")
        mBinding.actionbar.setOnConfirmListener { actionId: Int ->
            setSelectResult()
            finish()
            true
        }
        mBinding.sidebar.setOnSidebarStatusListener(object : OnSidebarStatusListener {
            override fun onChangeFinished() {
                mBinding.tvIndexPopup.visibility = View.GONE
            }

            override fun onSideIndexChanged(index: String) {
                val selection = mModel.getLetterPosition(index)
                scrollToPosition(selection)
                mBinding.tvIndexPopup.text = index
                mBinding.tvIndexPopup.visibility = View.VISIBLE
            }
        })
    }

    private fun setSelectResult() {
        val intent = Intent()
        intent.putCharSequenceArrayListExtra(
            RESP_SELECT_RESULT,
            mModel.getSelectedItems()
        )
        setResult(Activity.RESULT_OK, intent)
    }

    private val isSingleSelect: Boolean
        private get() = intent.getBooleanExtra(EXTRA_SINGLE, false)

    private val limitMax: Int
        private get() = intent.getIntExtra(EXTRA_LIMIT_MAX, 0)

    override fun initData() {
        mModel.starsObserver.observe(this, Observer { list ->
                val adapter = StarSelectorAdapter()
                adapter.list = list
                mBinding.rvStar.adapter = adapter
            }
        )
        mModel.indexObserver.observe(this, Observer { index ->
                mBinding.sidebar.addIndex(index)
            }
        )
        mModel.indexBarObserver.observe(this, Observer {
                mBinding.sidebar.build()
                mBinding.sidebar.visibility = View.VISIBLE
            }
        )
        mModel.bSingleSelect = isSingleSelect
        mModel.mLimitMax = limitMax
        mModel.loadStars()
    }

    private fun scrollToPosition(selection: Int) {
        val manager = mBinding.rvStar.layoutManager as LinearLayoutManager
        manager.scrollToPositionWithOffset(selection, 0)
    }
}