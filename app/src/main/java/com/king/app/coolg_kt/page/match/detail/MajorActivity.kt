package com.king.app.coolg_kt.page.match.detail

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchMajorBinding
import com.king.app.coolg_kt.page.match.MajorRank
import com.king.app.coolg_kt.page.match.MajorRound
import com.king.app.coolg_kt.page.match.rank.RankScoresFragment
import com.king.app.coolg_kt.page.match.record.RecordMatchActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

class MajorActivity: BaseActivity<ActivityMatchMajorBinding, MajorViewModel>() {

    companion object {
        val EXTRA_RECORD_ID = "record_id"
        fun startPage(context: Context, recordId: Long) {
            var intent = Intent(context, MajorActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            context.startActivity(intent)
        }
    }

    val adapter = MajorAdapter()

    override fun getContentView(): Int = R.layout.activity_match_major

    override fun createViewModel(): MajorViewModel = generateViewModel(MajorViewModel::class.java)

    override fun initView() {
        mModel.recordId = getRecordId()

        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        adapter.onHeadClickListener = object : HeadChildBindingAdapter.OnHeadClickListener<MajorRank> {
            override fun onClickHead(view: View, position: Int, data: MajorRank) {
                showScoreStructure(data)
            }
        }
        adapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<MajorRound> {
            override fun onClickItem(view: View, position: Int, data: MajorRound) {
                RecordMatchActivity.startPage(this@MajorActivity, mModel.recordId, data.matchId)
            }
        }
    }

    private fun getRecordId(): Long {
        return intent.getLongExtra(DetailActivity.EXTRA_RECORD_ID, -1)
    }

    override fun initData() {
        mModel.majorItems.observe(this) {
            adapter.list = it
            adapter.notifyDataSetChanged()
        }
        mModel.imageChanged.observe(this) { adapter.notifyItemRangeChanged(it.start, it.count) }
        mModel.loadMajors()
    }

    private fun showScoreStructure(data: MajorRank) {
        var content = RankScoresFragment()
        content.recordId = mModel.recordId
        content.period = data.period
        content.orderInPeriod = data.orderInPeriod
        var dialog = DraggableDialogFragment()
        val title = "Score Structure"
        dialog.setTitle(title)
        dialog.contentFragment = content
        dialog.maxHeight = ScreenUtils.getScreenHeight() * 4 / 5
        dialog.show(supportFragmentManager, "RankScoresFragment")
    }

}