package com.king.app.coolg_kt.page.image

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityImageManagerBinding
import com.king.app.coolg_kt.model.bean.ImageBean
import com.king.app.coolg_kt.page.match.list.MatchListActivity
import com.king.app.coolg_kt.page.video.order.PlayOrderActivity
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/8/4 9:41
 */
class ImageManagerActivity : BaseActivity<ActivityImageManagerBinding, ImageViewModel>() {

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_DATA = "data"
        const val TYPE_STAR = "type_star"
        const val TYPE_RECORD = "type_record"
    }
    
    private val REQUEST_SET_VIDEO_COVER = 101
    private val REQUEST_SET_MATCH_COVER = 102
    private var staggerAdapter: StaggerAdapter = StaggerAdapter()
    
    override fun getContentView(): Int = R.layout.activity_image_manager
    
    override fun createViewModel(): ImageViewModel = generateViewModel(ImageViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        mBinding.rvList.layoutManager = manager
        mBinding.actionbar.setOnSelectAllListener { select: Boolean ->
            mModel.onSelectAll(select)
            true
        }
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_delete ->  {
                    staggerAdapter.setSelectMode(true)
                    mBinding.actionbar.showConfirmStatus(menuId)
                    mBinding.actionbar.showSelectAll(true)
                }
            }
        }
        mBinding.actionbar.setOnConfirmListener {
            showConfirmCancelMessage("Are you sure to delete those images?"
                , DialogInterface.OnClickListener { dialog, which ->
                    mModel.deleteSelectedItems()
                    mBinding.actionbar.showSelectAll(false)
                    mBinding.actionbar.cancelConfirmStatus()
                    staggerAdapter!!.setSelectMode(false)
                }
                , null)
            false
        }
        mBinding.actionbar.setOnCancelListener {
            mBinding.actionbar.showSelectAll(false)
            staggerAdapter!!.setSelectMode(false)
            true
        }

        staggerAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<ImageBean> {
            override fun onClickItem(view: View, position: Int, data: ImageBean) {
                onApplyImage(data.url)
            }
        })
        mBinding.rvList.adapter = staggerAdapter
    }

    override fun initData() {
        mModel.imageList.observe(
            this,
            Observer<List<ImageBean>> { list: List<ImageBean> ->
//            if (isStaggerView) {
                showStaggerList(list)
            }
        )
        dispatchIntent(intent)
    }

    private fun dispatchIntent(intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE)
        if (TYPE_STAR == type) {
            mModel.loadStarImages(intent.getLongExtra(EXTRA_DATA, -1))
        } else if (TYPE_RECORD == type) {
            mModel.loadRecordImages(intent.getLongExtra(EXTRA_DATA, -1))
        }
    }

    private fun showStaggerList(list: List<ImageBean>) {
        staggerAdapter.list = list
        staggerAdapter.notifyDataSetChanged()
    }

    private fun onApplyImage(path: String?) {
        val options = arrayOf("Play Order", "Match")
        AlertDialogFragment()
            .setItems(options) { dialogInterface, i ->
                if (i === 0) {
                    onSetCoverForPlayOrder(path)
                } else if (i === 1) {
                    onSetCoverForMatch(path)
                }
            }
            .show(supportFragmentManager, "AlertDialogFragment")
    }

    private fun onSetCoverForMatch(path: String?) {
        mModel.setUrlToSetCover(path)
        MatchListActivity.startPageToSelect(this, REQUEST_SET_MATCH_COVER)
    }

    private fun onSetCoverForPlayOrder(path: String?) {
        mModel.setUrlToSetCover(path)
        PlayOrderActivity.startPageToSelect(this, REQUEST_SET_VIDEO_COVER)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SET_VIDEO_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                val list = data?.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
                mModel.setPlayOrderCover(list)
            }
        }
        else if (requestCode == REQUEST_SET_MATCH_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                val matchId = data?.getLongExtra(MatchListActivity.RESP_MATCH_ID, -1)!!
                mModel.setMatchCover(matchId)
            }
        }
    }
}