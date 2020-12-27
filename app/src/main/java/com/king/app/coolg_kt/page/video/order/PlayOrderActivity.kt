package com.king.app.coolg_kt.page.video.order

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.databinding.ActivityPlayOrderBinding
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.video.order.PlayOrderActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.SimpleDialogs

class PlayOrderActivity : BaseActivity<ActivityPlayOrderBinding, PlayOrderViewModel>() {

    companion object {
        const val EXTRA_MULTI_SELECT = "select_multi"
        const val RESP_SELECT_RESULT = "select_result"
        fun startPageToSelect(activity: Activity, requestCode: Int) {
            var intent = Intent(activity, PlayOrderActivity::class.java)
            intent.putExtra(EXTRA_MULTI_SELECT, true)
            activity.startActivityForResult(intent, requestCode)
        }
    }
    
    private val ACTION_MULTI_SELECT = 11
    private val REQUEST_PLAY_LIST = 6010
    
    private var adapter = PlayOrderAdapter()
    private var isEditMode = false
    
    override fun createViewModel(): PlayOrderViewModel = generateViewModel(PlayOrderViewModel::class.java)

    override fun getContentView(): Int = R.layout.activity_play_order
    
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
                R.id.menu_add -> SimpleDialogs().openInputDialog(this@PlayOrderActivity, "Create new play order") { name: String -> mModel.addPlayOrder(name) }
                R.id.menu_edit -> {
                    mBinding.actionbar.showConfirmStatus(menuId, true, "Cancel")
                    isEditMode = true
                }
                R.id.menu_delete -> {
                    mBinding.actionbar.showConfirmStatus(menuId)
                    adapter.setMultiSelect(true)
                }
                R.id.menu_list_view_type -> {
                    var type: Int = SettingProperty.getVideoPlayOrderViewType()
                    if (type == AppConstants.VIEW_TYPE_GRID) {
                        type = AppConstants.VIEW_TYPE_LIST
                    } else {
                        type = AppConstants.VIEW_TYPE_GRID
                    }
                    SettingProperty.setVideoPlayOrderViewType(type)
                    updateListViewType()
                }
            }
        }
        mBinding.actionbar.setOnConfirmListener { actionId: Int ->
            when (actionId) {
                R.id.menu_delete -> SimpleDialogs().showConfirmCancelDialog(
                    this@PlayOrderActivity
                    , "Delete order will delete related items, continue?"
                    , DialogInterface.OnClickListener { dialogInterface: DialogInterface?, i: Int ->
                        mModel.executeDelete()
                        adapter.setMultiSelect(false)
                        mBinding.actionbar.cancelConfirmStatus()
                        setResultChanged()
                    }, null
                )
                R.id.menu_edit -> {
                    isEditMode = false
                    return@setOnConfirmListener true
                }
                ACTION_MULTI_SELECT -> {
                    val intent = Intent()
                    intent.putCharSequenceArrayListExtra(RESP_SELECT_RESULT, mModel.selectedItems)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            false
        }
        mBinding.actionbar.setOnCancelListener { actionId: Int ->
            when (actionId) {
                R.id.menu_delete -> adapter.setMultiSelect(false)
                ACTION_MULTI_SELECT -> finish()
            }
            true
        }
        if (isMultiSelect) {
            mBinding.actionbar.showConfirmStatus(ACTION_MULTI_SELECT)
        }
    }

    private val linearDecoration: ItemDecoration = object : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = ScreenUtils.dp2px(8f)
        }
    }
    private val gridDecoration: ItemDecoration = object : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildLayoutPosition(view)
            outRect.top = ScreenUtils.dp2px(8f)
            if (position % 2 == 0) {
                outRect.left = ScreenUtils.dp2px(8f)
                outRect.right = ScreenUtils.dp2px(4f)
            } else {
                outRect.left = ScreenUtils.dp2px(4f)
                outRect.right = ScreenUtils.dp2px(8f)
            }
        }
    }
    private val gridTabDecoration: ItemDecoration = object : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildLayoutPosition(view)
            if (position / 3 == 0) {
                outRect.top = ScreenUtils.dp2px(8f)
            } else {
                outRect.top = 0
            }
        }
    }

    private fun updateListViewType() {
        var type = SettingProperty.getVideoPlayOrderViewType()
        if (ScreenUtils.isTablet()) {
            type = AppConstants.VIEW_TYPE_GRID_TAB
            val gridLayoutManager = GridLayoutManager(this, 3)
            mBinding.rvList.layoutManager = gridLayoutManager
            mBinding.rvList.addItemDecoration(gridTabDecoration)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_list_view_type, false)
        } else {
            if (type == AppConstants.VIEW_TYPE_GRID) {
                mBinding.rvList.removeItemDecoration(linearDecoration)
                val gridLayoutManager = GridLayoutManager(this, 2)
                mBinding.rvList.layoutManager = gridLayoutManager
                mBinding.rvList.addItemDecoration(gridDecoration)
                mBinding.actionbar.updateMenuText(R.id.menu_list_view_type, "List View")
            } else {
                mBinding.rvList.removeItemDecoration(gridDecoration)
                mBinding.rvList.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                mBinding.rvList.addItemDecoration(linearDecoration)
                mBinding.actionbar.updateMenuText(R.id.menu_list_view_type, "Grid View")
            }
        }
        adapter.mViewType = type
    }

    private val isMultiSelect: Boolean
        private get() = intent.getBooleanExtra(EXTRA_MULTI_SELECT, false)

    override fun initData() {
        mModel.dataObserver.observe(this,
            Observer {
                if (mBinding.rvList.adapter == null) {
                    adapter.list = it
                    adapter.setMultiSelect(isMultiSelect)
                    adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<VideoPlayList> {
                        override fun onClickItem(view: View, position: Int, data: VideoPlayList) {
                            if (isEditMode) {
                                updateOrderName(position, data)
                            } else {
//                                Router.build("PlayList")
//                                    .with(PlayListActivity.EXTRA_ORDER_ID, data.playOrder!!.id)
//                                    .requestCode(REQUEST_PLAY_LIST)
//                                    .go(this@PlayOrderActivity)
                                TODO()
                            }
                        }
                    })
                    mBinding.rvList.adapter = adapter
                } else {
                    adapter.list = it
                    adapter.notifyDataSetChanged()
                }
            }
        )
        mModel.loadOrders()
    }

    private fun updateOrderName(position: Int, data: VideoPlayList) {
        SimpleDialogs().openInputDialog(this, "Rename", data.name) { name: String? ->
            mModel.updateOrderName(data, name!!)
            adapter.notifyItemChanged(position)
            setResultChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PLAY_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                mModel.loadOrders()
            }
        }
    }

    private fun setResultChanged() {
        setResult(Activity.RESULT_OK)
    }
}