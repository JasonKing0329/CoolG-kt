package com.king.app.coolg_kt.page.match.score

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchScoreBinding

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/16 16:36
 */
class ScoreActivity: BaseActivity<ActivityMatchScoreBinding, ScoreViewModel>() {

    companion object {
        val EXTRA_RECORD_ID = "record_id"
        fun startRecordPage(context: Context, recordId: Long) {
            var intent = Intent(context, ScoreActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            context.startActivity(intent)
        }
    }

    val adapter = ScoreItemAdapter()

    override fun getContentView(): Int = R.layout.activity_match_score

    override fun createViewModel(): ScoreViewModel = generateViewModel(ScoreViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        mBinding.tvWeek.setOnClickListener {
            mBinding.tvWeek.isSelected = true
            mBinding.tvYear.isSelected = false
            mBinding.dividerWeek.visibility = View.VISIBLE
            mBinding.dividerYear.visibility = View.INVISIBLE
            mModel.loadRankPeriod(getRecordId())
        }
        mBinding.tvYear.setOnClickListener {
            mBinding.tvWeek.isSelected = false
            mBinding.tvYear.isSelected = true
            mBinding.dividerWeek.visibility = View.INVISIBLE
            mBinding.dividerYear.visibility = View.VISIBLE
            mModel.loadRaceToFinal(getRecordId())
        }
    }

    override fun initData() {
        mModel.scoresObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })

        mModel.loadRankPeriod(getRecordId())
    }

    private fun getRecordId(): Long {
        return intent.getLongExtra(EXTRA_RECORD_ID, -1)
    }
}