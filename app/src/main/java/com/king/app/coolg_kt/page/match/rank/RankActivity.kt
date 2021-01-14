package com.king.app.coolg_kt.page.match.rank

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchRankBinding
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 16:28
 */
class RankActivity: BaseActivity<ActivityMatchRankBinding, RankViewModel>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, RankActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_match_rank

    override fun createViewModel(): RankViewModel = generateViewModel(RankViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> mModel.loadRecordRankPeriod()
                    1 -> mModel.loadRecordRaceToFinal()
                    2 -> mModel.loadStarRankPeriod()
                    3 -> mModel.loadStarRaceToFinal()
                }
            }
        }

        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun initData() {
        mModel.recordRanksObserver.observe(this, Observer {
            var adapter = RankAdapter<Record?>()
            adapter.list = it
            mBinding.rvList.adapter = adapter
        })
        mModel.starRanksObserver.observe(this, Observer {
            var adapter = RankAdapter<Star?>()
            adapter.list = it
            mBinding.rvList.adapter = adapter
        })
    }
}