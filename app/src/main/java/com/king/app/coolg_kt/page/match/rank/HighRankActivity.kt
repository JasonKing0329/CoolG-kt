package com.king.app.coolg_kt.page.match.rank

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchRankHighBinding
import com.king.app.coolg_kt.page.match.HighRankItem
import com.king.app.coolg_kt.page.match.detail.DetailActivity

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/28 16:41
 */
class HighRankActivity: BaseActivity<ActivityMatchRankHighBinding, HighViewModel>() {

    val adapter = HighRankAdapter()

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, HighRankActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_match_rank_high

    override fun createViewModel(): HighViewModel = generateViewModel(HighViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        val manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position)
            }
        }
        mBinding.rvList.layoutManager = manager

        adapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<HighRankItem> {
            override fun onClickItem(view: View, position: Int, data: HighRankItem) {
                DetailActivity.startRecordPage(this@HighRankActivity, data.bean.record.id!!)
            }
        }
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        mModel.rangeChanged.observe(this, Observer {
            adapter.notifyGroupChanged(it.start)
        })
        mModel.itemsObserver.observe(this, Observer {
            adapter.groupList = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadHighestRanks()
    }
}