package com.king.app.coolg_kt.page.match.item

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchDetailBinding
import com.king.app.coolg_kt.page.match.MatchSemiItem
import com.king.app.coolg_kt.page.match.MatchSemiPack
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.match.draw.DrawActivity

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/4/26 14:55
 */
class MatchActivity: BaseActivity<ActivityMatchDetailBinding, MatchViewModel>() {

    companion object {
        val EXTRA_MATCH_ID = "match_id"
        fun startPage(context: Context, matchId: Long) {
            var intent = Intent(context, MatchActivity::class.java)
            intent.putExtra(EXTRA_MATCH_ID, matchId)
            context.startActivity(intent)
        }
    }

    val adapter = MatchSemiAdapter()

    override fun getContentView(): Int = R.layout.activity_match_detail

    override fun createViewModel(): MatchViewModel = generateViewModel(MatchViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<MatchSemiPack> {
            override fun onClickItem(view: View, position: Int, data: MatchSemiPack) {
                DrawActivity.startPage(this@MatchActivity, data.matchPeriodId)
            }
        })
        adapter.onRecordListener = object : MatchSemiAdapter.OnRecordListener {
            override fun onClickRecord(semiItem: MatchSemiItem) {
                DetailActivity.startRecordPage(this@MatchActivity, semiItem.recordId)
            }
        }
    }

    private fun getMatchId(): Long {
        return intent.getLongExtra(EXTRA_MATCH_ID, -1)
    }

    override fun initData() {
        mModel.itemsObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.matchId = getMatchId()
        mModel.loadItems()
    }
}