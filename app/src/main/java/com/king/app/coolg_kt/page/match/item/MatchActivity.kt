package com.king.app.coolg_kt.page.match.item

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchDetailBinding
import com.king.app.coolg_kt.page.match.MatchCountRecord
import com.king.app.coolg_kt.page.match.MatchRoundRecord
import com.king.app.coolg_kt.page.match.MatchSemiItem
import com.king.app.coolg_kt.page.match.MatchSemiPack
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.match.draw.DrawActivity
import com.king.app.coolg_kt.utils.RippleUtil

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

    val countAdapter = JoinAdapter()

    val roundAdapter = RoundAdapter()

    override fun getContentView(): Int = R.layout.activity_match_detail

    override fun createViewModel(): MatchViewModel = generateViewModel(MatchViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

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
        countAdapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<MatchCountRecord> {
            override fun onClickItem(view: View, position: Int, data: MatchCountRecord) {
                DetailActivity.startRecordPage(this@MatchActivity, data.recordId)
            }
        }
        roundAdapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<MatchRoundRecord> {
            override fun onClickItem(view: View, position: Int, data: MatchRoundRecord) {
                DetailActivity.startRecordPage(this@MatchActivity, data.recordId)
            }
        }

        mBinding.tvTimeline.setOnClickListener { setTimeLineView() }
        mBinding.tvCount.setOnClickListener { setCountView() }
        mBinding.tvRound.setOnClickListener { setRoundView() }

        mBinding.tvTimeline.background = RippleUtil.getRippleBackground(
            resources.getColor(R.color.white_dim),
            resources.getColor(R.color.ripple_color)
        )
        mBinding.tvCount.background = RippleUtil.getRippleBackground(
            resources.getColor(R.color.white_dim),
            resources.getColor(R.color.ripple_color)
        )
        mBinding.tvRound.background = RippleUtil.getRippleBackground(
            resources.getColor(R.color.white_dim),
            resources.getColor(R.color.ripple_color)
        )
    }

    private fun getMatchId(): Long {
        return intent.getLongExtra(EXTRA_MATCH_ID, -1)
    }

    override fun initData() {
        mModel.itemsObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.semiItemsRange.observe(this, Observer {
            adapter.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.countItemsObserver.observe(this, Observer {
            countAdapter.list = it
            mBinding.rvList.adapter = countAdapter
            adapter.notifyDataSetChanged()
        })
        mModel.countItemsRange.observe(this, Observer {
            countAdapter.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.roundItemsObserver.observe(this, Observer {
            roundAdapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.roundItemsRange.observe(this, Observer {
            roundAdapter.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.matchId = getMatchId()
        setTimeLineView()
    }

    private fun listAdapter() {
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun gridAdapter() {
        val gridManager = GridLayoutManager(this, 2)
        gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = roundAdapter.getSpanSize(position)
        }
        mBinding.rvList.layoutManager = gridManager
    }

    private fun setTimeLineView() {
        mBinding.tvTimeline.isSelected = true
        mBinding.tvCount.isSelected = false
        mBinding.tvRound.isSelected = false
        listAdapter()
        mBinding.rvList.adapter = adapter
        mModel.loadItems()
    }

    private fun setCountView() {
        mBinding.tvTimeline.isSelected = false
        mBinding.tvCount.isSelected = true
        mBinding.tvRound.isSelected = false
        listAdapter()
        mBinding.rvList.adapter = countAdapter
        mModel.loadCount()
    }

    private fun setRoundView() {
        mBinding.tvTimeline.isSelected = false
        mBinding.tvCount.isSelected = false
        mBinding.tvRound.isSelected = true
        gridAdapter()
        mBinding.rvList.adapter = roundAdapter
        mModel.loadRoundItems()
    }
}