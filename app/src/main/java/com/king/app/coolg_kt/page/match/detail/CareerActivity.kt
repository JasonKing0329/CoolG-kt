package com.king.app.coolg_kt.page.match.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchCareerBinding
import com.king.app.coolg_kt.page.match.CareerMatch
import com.king.app.coolg_kt.page.match.CareerRecord
import com.king.app.coolg_kt.page.match.draw.DrawActivity
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

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
        adapter.onRecordListener = object : CareerAdapter.OnRecordListener {
            override fun onClickRecord(position: Int, record: CareerRecord) {
                record.record?.let {
                    showRoadDialog(it.id!!, record.parent.matchPeriodId)
                }
            }
        }
        adapter.onMatchListener = object : CareerAdapter.OnMatchListener {
            override fun onClickMatch(position: Int, record: CareerMatch) {
                DrawActivity.startPage(this@CareerActivity, record.matchPeriodId)
            }
        }
        mBinding.rvList.adapter = adapter

        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_collapse_period -> adapter.expandAllPeriod(false)
                R.id.menu_collapse_match -> adapter.expandAllMatches(false)
                R.id.menu_expand_period -> adapter.expandAllPeriod(true)
                R.id.menu_expand_match -> adapter.expandAllMatches(true)
            }
        }
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

    private fun showRoadDialog(recordId: Long, matchPeriodId: Long) {
        var content = RoadDialog()
        content.matchPeriodId = matchPeriodId
        content.recordId = recordId
        var dialog = DraggableDialogFragment()
        dialog.setTitle("Upgrade Road")
        dialog.contentFragment = content
        dialog.show(supportFragmentManager, "RoadDialog")
    }

}