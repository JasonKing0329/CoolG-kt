package com.king.app.coolg_kt.page.star.timeline

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityStarTimelineBinding
import com.king.app.coolg_kt.model.bean.TimelineStar
import com.king.app.coolg_kt.page.star.phone.StarActivity

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/3/8 14:44
 */
class StarTimelineActivity: BaseActivity<ActivityStarTimelineBinding, StarTimelineViewModel>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, StarTimelineActivity::class.java)
            context.startActivity(intent)
        }
    }

    var adapter = StarTimelineAdapter()

    override fun getContentView(): Int = R.layout.activity_star_timeline

    override fun createViewModel(): StarTimelineViewModel = generateViewModel(StarTimelineViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_edit -> {
                    mBinding.actionbar.showConfirmStatus(it, true, getString(R.string.done))
                    adapter.isEditing = true
                    adapter.notifyDataSetChanged()
                }
                R.id.menu_hide -> {
                    mModel.toggleHiddenItems()
                    toggleShowHideMenu()
                }
            }
        }
        mBinding.actionbar.setOnConfirmListener {
            when(it) {
                R.id.menu_edit -> {
                    adapter.isEditing = false
                    adapter.notifyDataSetChanged()
                }
            }
            return@setOnConfirmListener true
        }
        toggleShowHideMenu()

        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter
        adapter.onHiddenChangedListener = object : StarTimelineAdapter.OnHiddenChangedListener {
            override fun onHiddenChanged(position: Int, starId: Long, hidden: Boolean) {
                mModel.updateHidden(position, starId, hidden)
            }
        }
        adapter.listenerClick = object : BaseBindingAdapter.OnItemClickListener<TimelineStar> {
            override fun onClickItem(view: View, position: Int, data: TimelineStar) {
                StarActivity.startPage(this@StarTimelineActivity, data.star.id!!)
            }
        }
    }

    private fun toggleShowHideMenu() {
        adapter.isShowHidden = mModel.isShowHiddenStar
        if (mModel.isShowHiddenStar) {
            mBinding.actionbar.updateMenuText(R.id.menu_hide, "Hide hidden items")
        }
        else {
            mBinding.actionbar.updateMenuText(R.id.menu_hide, "Show hidden items")
        }
    }

    override fun initData() {
        mModel.timelineItems.observe(this, {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.imageChanged.observe(this, {
            adapter.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.loadItems()
    }
}