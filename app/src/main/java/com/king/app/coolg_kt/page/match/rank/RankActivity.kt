package com.king.app.coolg_kt.page.match.rank

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchRankBinding
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.coolg_kt.page.match.score.ScoreActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
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
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_create_rank -> createRank()
            }
        }
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
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = 0
                outRect.bottom = 0
            }
        })
    }

    /**
     *
    <item>Record-Period</item>
    <item>Record-RTF</item>
    <item>Star-Period</item>
    <item>Star-RTF</item>
     */
    private fun createRank() {
        when(mBinding.spType.selectedItemPosition) {
            0 -> {
                if (mModel.isLastRecordRankCreated()) {
                    showConfirmCancelMessage("Record ranks of last week have been already created, do you want to override it?",
                        DialogInterface.OnClickListener { dialog, which -> mModel.createRankRecord() },
                        null)
                }
                else {
                    mModel.createRankRecord()
                }
            }
            1 -> showMessageShort("Create record ranks can only be executed in Record-Period!")
            2 -> {
                if (mModel.isLastStarRankCreated()) {
                    showConfirmCancelMessage("Star Ranks of last week have been already created, do you want to override it?",
                        DialogInterface.OnClickListener { dialog, which -> mModel.createRankStar() },
                        null)
                }
                else {
                    mModel.createRankStar()
                }
            }
            3 -> showMessageShort("Create star ranks can only be executed in Star-Period!")
        }
    }

    override fun initData() {
        mModel.recordRanksObserver.observe(this, Observer {
            var adapter = RankAdapter<Record?>()
            adapter.onItemListener = object : RankAdapter.OnItemListener<Record?> {
                override fun onClickScore(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        ScoreActivity.startRecordPage(this@RankActivity, record.id!!)
                    }
                }

                override fun onClickId(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        RecordActivity.startPage(this@RankActivity, record.id!!)
                    }
                }
            }
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