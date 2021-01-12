package com.king.app.coolg_kt.page.record.phone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityRecordTagBinding
import com.king.app.coolg_kt.page.record.AbsRecordListActivity
import com.king.app.coolg_kt.page.record.RecordListViewModel
import com.king.app.coolg_kt.page.record.RecordTag
import com.king.app.coolg_kt.page.video.order.PlayOrderActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 9:49
 */
class PhoneRecordListActivity: AbsRecordListActivity<ActivityRecordTagBinding, RecordListViewModel>() {

    companion object {
        val EXTRA_STUDIO_ID = "studio_id"
        val EXTRA_SELECT_MODE = "select_mode"
        val RESP_RECORD_ID = "select_mode"
        fun startPage(context: Context) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSelect(context: Activity, requestCode: Int) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            context.startActivityForResult(intent, requestCode)
        }
        fun startStudioPage(context: Context, studioId: Long) {
            var intent = Intent(context, PhoneRecordListActivity::class.java)
            intent.putExtra(EXTRA_STUDIO_ID, studioId)
            context.startActivity(intent)
        }
    }

    var tagAdapter = HeadTagAdapter()

    var sceneAdapter = HeadTagAdapter()

    override fun getContentView(): Int = R.layout.activity_record_tag

    override fun createViewModel(): RecordListViewModel = generateViewModel(RecordListViewModel::class.java)

    override fun initView() {
        val manager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)
        mBinding.rvTags.layoutManager = manager
        mBinding.rvTags.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
        })
        tagAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordTag>{
            override fun onClickItem(view: View, position: Int, data: RecordTag) {
                mModel.loadRecordsByTag(data)
            }
        })
        mBinding.rvTags.adapter = tagAdapter

        mBinding.rvRecords.layoutManager = GridLayoutManager(this, 2)
        mBinding.rvRecords.setEnableLoadMore(true)
        mBinding.rvRecords.setOnLoadMoreListener { mModel.loadMoreRecords() }

        mBinding.fabTop.setOnClickListener { v -> mBinding.rvRecords.scrollToPosition(0) }

        // studio records page, hide tag bar and related menu
        var studioId = intent.getLongExtra(EXTRA_STUDIO_ID, 0)
        mModel.mOrderId = studioId
        if (studioId != 0L) {
            mBinding.rvTags.visibility = View.GONE
            mBinding.actionbar.setTitle(mModel.loadStudioTitle(studioId))
        }
        initActionBar(mBinding.actionbar)
    }

    override fun onClickRecord(view: View, position: Int, data: RecordWrap) {
        if (intent.getBooleanExtra(EXTRA_SELECT_MODE, false)) {
            val intent = Intent()
            intent.putExtra(RESP_RECORD_ID, data.bean.id)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        else {
            super.onClickRecord(view, position, data)
        }
    }
    /**
     * studio records page, hide tag bar and related menu
     */
    override fun isHideTagBar(): Boolean = intent.getLongExtra(EXTRA_STUDIO_ID, 0) != 0L

    override fun getRecordRecyclerView(): RecyclerView {
        return mBinding.rvRecords
    }

    override fun showTags(tags: List<RecordTag>) {
        tagAdapter.list = tags
        tagAdapter.notifyDataSetChanged()
    }

    override fun focusOnTag(position: Int) {
        tagAdapter.selection = position
        tagAdapter.notifyDataSetChanged()
    }

    override fun goToRecordPage(record: Record) {
        RecordActivity.startPage(this, record.id!!)
    }

    override fun addToPlayOrder(data: Record) {
        mModel.saveRecordToPlayOrder(data)
        PlayOrderActivity.startPageToSelect(this, REQUEST_VIDEO_ORDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                val list = data?.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
                mModel.addToPlay(list)
            }
        }
    }
}