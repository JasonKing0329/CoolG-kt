package com.king.app.coolg_kt.page.match.h2h

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchRecordH2hBinding
import com.king.app.coolg_kt.page.match.RecordH2hItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/8 14:48
 */
class RecordH2hActivity: BaseActivity<ActivityMatchRecordH2hBinding, RecordH2hViewModel>() {

    companion object {
        val EXTRA_RECORD_ID= "record_id"
        fun startPage(context: Context, recordId: Long) {
            var intent = Intent(context, RecordH2hActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_match_record_h2h

    override fun createViewModel(): RecordH2hViewModel = generateViewModel(RecordH2hViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun initData() {

        mModel.listObserver.observe(this, Observer {
            var adapter = RecordH2hAdapter()
            adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordH2hItem> {
                override fun onClickItem(view: View, position: Int, data: RecordH2hItem) {
                    H2hActivity.startH2hPage(this@RecordH2hActivity, data.record1.id!!, data.record2.id!!)
                }
            })
            adapter.list = it
            mBinding.rvList.adapter = adapter
        })
        mModel.imageChanged.observe(this, Observer {
            mBinding.rvList.adapter?.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.loadInfo(intent.getLongExtra(EXTRA_RECORD_ID, -1))
    }
}