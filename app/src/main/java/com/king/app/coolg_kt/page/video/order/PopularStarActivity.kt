package com.king.app.coolg_kt.page.video.order

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.databinding.ActivityVideoStarListBinding
import com.king.app.coolg_kt.model.bean.VideoGuy
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.star.list.StarSelectorActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/25 13:40
 */
class PopularStarActivity : BaseActivity<ActivityVideoStarListBinding, PopularStarViewModel>() {

    val REQUEST_SELECT_STAR = 6101

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, PopularStarActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var adapter = PopularStarAdapter()
    
    override fun createViewModel(): PopularStarViewModel = generateViewModel(PopularStarViewModel::class.java)
    
    override fun getContentView(): Int {
        return R.layout.activity_video_star_list
    }

    override fun initView() {
        requestedOrientation = if (ScreenUtils.isTablet()) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        updateListViewType()
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_add -> {
                    StarSelectorActivity.startPage(this@PopularStarActivity, false, 0, REQUEST_SELECT_STAR)
                }
                R.id.menu_delete -> {
                    mBinding.actionbar.showConfirmStatus(menuId)
                    adapter.setMultiSelect(true)
                }
                R.id.menu_sort -> AlertDialogFragment()
                    .setItems(resources.getStringArray(R.array.sort_video_star_order)) { dialog: DialogInterface?, which: Int ->
                        if (which == 0) {
                            mModel.sortByName()
                        } else if (which == 1) {
                            mModel.sortByVideo()
                        }
                    }
                    .show(supportFragmentManager, "AlertDialogFragment")
                R.id.menu_list_view_type -> {
                    var type = SettingProperty.getVideoStarOrderViewType()
                    type = if (type == PreferenceValue.VIEW_TYPE_GRID) {
                        PreferenceValue.VIEW_TYPE_LIST
                    } else {
                        PreferenceValue.VIEW_TYPE_GRID
                    }
                    SettingProperty.setVideoStarOrderViewType(type)
                    updateListViewType()
                    mModel.loadStars()
                }
            }
        }
        mBinding.actionbar.setOnConfirmListener { actionId: Int ->
            when (actionId) {
                R.id.menu_delete -> SimpleDialogs().showConfirmCancelDialog(
                    this@PopularStarActivity
                    , "Delete order will delete related items, continue?"
                    , DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int ->
                        mModel.executeDelete()
                        adapter.setMultiSelect(false)
                        mBinding.actionbar.cancelConfirmStatus()
                    }, null
                )
            }
            false
        }
        mBinding.actionbar.setOnCancelListener { actionId: Int ->
            when (actionId) {
                R.id.menu_delete -> adapter.setMultiSelect(false)
            }
            true
        }
    }

    private fun updateListViewType() {
        if (ScreenUtils.isTablet()) {
            val gridLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            mBinding.rvList.layoutManager = gridLayoutManager
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_list_view_type, false)
        } else {
            var type = SettingProperty.getVideoStarOrderViewType()
            if (type == PreferenceValue.VIEW_TYPE_GRID) {
                val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                mBinding.rvList.layoutManager = gridLayoutManager
                mBinding.actionbar.updateMenuText(R.id.menu_list_view_type, "List View")
            } else {
                mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                mBinding.actionbar.updateMenuText(R.id.menu_list_view_type, "Grid View")
            }
            mModel.mViewType = type
        }
    }

    override fun initData() {
        mModel.starsObserver.observe(this,
            Observer {  list ->
                if (mBinding.rvList.adapter == null) {
                    adapter.list = list
                    adapter.setOnItemClickListener(object :
                        BaseBindingAdapter.OnItemClickListener<VideoGuy> {
                        override fun onClickItem(view: View, position: Int, data: VideoGuy) {
                            data.star?.let {
                                PlayOrderItemsActivity.playStar(this@PopularStarActivity, it.id!!)
                            }
                        }
                    })
                    mBinding.rvList.adapter = adapter
                } else {
                    adapter.list = list
                    adapter.notifyDataSetChanged()
                }
            }
        )
        mModel.loadStars()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_STAR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val list = it.getCharSequenceArrayListExtra(StarSelectorActivity.RESP_SELECT_RESULT)
                    mModel.insertVideoCoverStar(list)
                }
            }
        }
    }
}