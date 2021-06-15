package com.king.app.coolg_kt.page.match.record

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchRecordMatchBinding

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/15 16:04
 */
class RecordMatchActivity:BaseActivity<ActivityMatchRecordMatchBinding, RecordMatchViewModel>() {

    companion object {
        val EXTRA_RECORD_ID = "record_id"
        val EXTRA_MATCH_ID = "match_id"

        fun startPage(context: Context, recordId: Long, matchId: Long) {
            var intent = Intent(context, RecordMatchActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            intent.putExtra(EXTRA_MATCH_ID, matchId)
            context.startActivity(intent)
        }
    }

    override fun isFullScreen(): Boolean {
        return true
    }
    override fun getContentView(): Int = R.layout.activity_match_record_match

    override fun createViewModel(): RecordMatchViewModel = generateViewModel(RecordMatchViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.rvRecords.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun initData() {
        mModel.mRecordId = intent.getLongExtra(EXTRA_RECORD_ID, -1)
        mModel.mMatchId = intent.getLongExtra(EXTRA_MATCH_ID, -1)
        mModel.itemsObserver.observe(this, Observer {
            val adapter = RecordMatchPageAdapter()
            adapter.list = it
            mBinding.rvRecords.adapter = adapter
        })
        mModel.loadData()
    }
}