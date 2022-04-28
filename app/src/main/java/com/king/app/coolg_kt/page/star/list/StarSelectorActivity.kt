package com.king.app.coolg_kt.page.star.list

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.*
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityStarSelectorBinding
import com.king.app.coolg_kt.model.bean.StudioStarWrap
import com.king.app.coolg_kt.page.pub.StudioTagAdapter
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.FitSideBar.OnSidebarStatusListener
import com.king.app.gdb.data.entity.FavorRecordOrder


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

    private val adapter = StarSelectorAdapter()
    private val studioAdapter = StudioTagAdapter()

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
        mBinding.fabSort.setOnClickListener { popupSort(it) }
        mBinding.fabSearch.setOnClickListener {
            if (mBinding.groupSearch.visibility != View.VISIBLE) {
                showSearchBar()
            }
            else {
                hideSearchBar()
            }
        }
        mBinding.ivCloseSearch.setOnClickListener {
            mBinding.etSearch.setText("")
        }
        mBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.apply {
                    mModel.onKeywordChanged(this.toString().trim())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
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
        mBinding.rvStar.adapter = adapter

        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        mBinding.rvStudio.layoutManager = manager
        mBinding.rvStudio.addItemDecoration(tagDecoration)
        mBinding.rvStudio.adapter = studioAdapter

        studioAdapter.listenerClick = object : BaseBindingAdapter.OnItemClickListener<StudioStarWrap> {
            override fun onClickItem(view: View, position: Int, data: StudioStarWrap) {
                mModel.changeStudio(data.studio.id)
            }
        }
    }

    var tagDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = ScreenUtils.dp2px(10f)
            outRect.top = ScreenUtils.dp2px(5f)
            outRect.bottom = ScreenUtils.dp2px(5f)
        }
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
        mModel.studiosObserver.observe(this) {
            studioAdapter.list = it
            studioAdapter.notifyDataSetChanged()
            mModel.loadStar()
        }
        mModel.starsObserver.observe(this, Observer { list ->
                adapter.list = list
                adapter.notifyDataSetChanged()
            }
        )
        mModel.indexObserver.observe(this) {
            mBinding.sidebar.clear()
            it.forEach { index ->
                mBinding.sidebar.addIndex(index)
            }
            mBinding.sidebar.build()
            mBinding.sidebar.visibility = View.VISIBLE
        }
        mModel.bSingleSelect = isSingleSelect
        mModel.mLimitMax = limitMax
        mModel.loadStudios()
    }

    private fun scrollToPosition(selection: Int) {
        val manager = mBinding.rvStar.layoutManager as LinearLayoutManager
        manager.scrollToPositionWithOffset(selection, 0)
    }

    private fun showSearchBar() {
        if (mBinding.groupSearch.visibility != View.VISIBLE) {

            // show search group and animate search icon
            mBinding.groupSearch.visibility = View.VISIBLE
            AlphaAnimation(0F, 1F).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
                mBinding.groupSearch.startAnimation(this)
            }
        }
    }

    private fun hideSearchBar() {
        AlphaAnimation(1F, 0F).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    mBinding.groupSearch.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {

                }
            })
            mBinding.groupSearch.startAnimation(this)
        }
    }

    private fun popupSort(anchor: View) {
        PopupMenu(this, anchor).apply {
            menuInflater.inflate(R.menu.player_sort, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_sort_name -> mModel.sortMode = AppConstants.STAR_SORT_NAME
                    R.id.menu_sort_records -> mModel.sortMode = AppConstants.STAR_SORT_RECORDS
                    R.id.menu_sort_rating -> mModel.sortMode = AppConstants.STAR_SORT_RATING
                    R.id.menu_sort_rating_face -> mModel.sortMode = AppConstants.STAR_SORT_RATING_FACE
                    R.id.menu_sort_rating_body -> mModel.sortMode = AppConstants.STAR_SORT_RATING_BODY
                    R.id.menu_sort_rating_dk -> mModel.sortMode = AppConstants.STAR_SORT_RATING_DK
                    R.id.menu_sort_rating_sexuality -> mModel.sortMode = AppConstants.STAR_SORT_RATING_SEXUALITY
                    R.id.menu_sort_rating_passion -> mModel.sortMode = AppConstants.STAR_SORT_RATING_PASSION
                    R.id.menu_sort_rating_video -> mModel.sortMode = AppConstants.STAR_SORT_RATING_VIDEO
                    R.id.menu_sort_random -> mModel.sortMode = AppConstants.STAR_SORT_RANDOM
                }
                false
            }
            show()
        }
    }
}