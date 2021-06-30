package com.king.app.coolg_kt.page.match.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchCareerBinding

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/30 14:36
 */
class CareerActivity: BaseActivity<ActivityMatchCareerBinding, CareerViewModel>() {

    val adapter = CareerAdapter()

    companion object {
        val EXTRA_RECORD_ID = "record_id"
        fun startPage(context: Context, recordId: Long) {
            var intent = Intent(context, CareerActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_match_career

    override fun createViewModel(): CareerViewModel = generateViewModel(CareerViewModel::class.java)

    override fun initView() {
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter
    }

    private fun getRecordId(): Long {
        return intent.getLongExtra(DetailActivity.EXTRA_RECORD_ID, -1)
    }

    override fun initData() {
        mModel.periodList.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.periodChanged.observe(this, Observer {
            adapter.notifyPeriodChanged(it)
        })
        mModel.loadData(getRecordId())
    }
}