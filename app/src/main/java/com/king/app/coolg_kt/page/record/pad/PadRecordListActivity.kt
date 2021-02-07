package com.king.app.coolg_kt.page.record.pad

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.flow_rc.FlowLayoutManager

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/7 9:55
 */
class PadRecordListActivity: PhoneRecordListActivity() {

    companion object {
        val EXTRA_STUDIO_ID = "studio_id"
        val EXTRA_SELECT_MODE = "select_mode"
        val RESP_RECORD_ID = "record_id"
        fun startPage(context: Context) {
            var intent = Intent(context, PadRecordListActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSelect(context: Activity, requestCode: Int) {
            var intent = Intent(context, PadRecordListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            context.startActivityForResult(intent, requestCode)
        }
        fun startStudioPage(context: Context, studioId: Long) {
            var intent = Intent(context, PadRecordListActivity::class.java)
            intent.putExtra(EXTRA_STUDIO_ID, studioId)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        initPad()
        initPub()
    }

    /**
     * pad设备下改为tag在左，records在右，且tag为流式布局
     */
    private fun initPad() {
        val paramTag = ConstraintLayout.LayoutParams(ScreenUtils.dp2px(360f), ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
        paramTag.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        paramTag.topToBottom = mBinding.actionbar.id
        paramTag.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        mBinding.rvTags.layoutParams = paramTag

        val paramRecord = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT)
        paramRecord.topToBottom = mBinding.actionbar.id
        paramRecord.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        paramRecord.startToEnd = mBinding.rvTags.id
        paramRecord.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        paramRecord.marginStart = ScreenUtils.dp2px(16f)
        mBinding.ftRecords.layoutParams = paramRecord

        val manager = FlowLayoutManager(this, false)
        mBinding.rvTags.layoutManager = manager
        mBinding.rvTags.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.left = ScreenUtils.dp2px(16f)
                outRect.top = ScreenUtils.dp2px(8f)
                outRect.bottom = ScreenUtils.dp2px(8f)
            }
        })
    }
}