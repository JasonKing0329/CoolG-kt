package com.king.app.coolg_kt.page.match.titles

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
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.ActivityMatchFinalListBinding
import com.king.app.coolg_kt.page.match.FinalListItem
import com.king.app.coolg_kt.page.match.TitleCountItem
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.match.draw.DrawActivity
import com.king.app.coolg_kt.page.match.draw.FinalDrawActivity
import com.king.app.coolg_kt.page.match.h2h.H2hActivity
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/22 13:44
 */
class FinalListActivity: BaseActivity<ActivityMatchFinalListBinding, FinalListViewModel>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, FinalListActivity::class.java)
            context.startActivity(intent)
        }
    }

    val adapter = FinalListAdapter()
    val titlesAdapter = TitlesCountAdapter()

    private var dataType = 0 // 0 by date; 1 titles count; 2 highest rank

    override fun getContentView(): Int = R.layout.activity_match_final_list

    override fun createViewModel(): FinalListViewModel = generateViewModel(FinalListViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_filter -> filter()
                R.id.menu_date -> {
                    if (dataType != 0) {
                        dataType = 0
                        mModel.loadData()
                    }
                }
                R.id.menu_titles_count -> {
                    if (dataType != 1) {
                        dataType = 1
                        mModel.loadTitlesCount()
                    }
                }
            }
        }

        adapter.onClickRecordListener = object : FinalListAdapter.OnClickRecordListener {
            override fun onClickRecord(matchRecordWrap: MatchRecordWrap) {
                DetailActivity.startRecordPage(this@FinalListActivity, matchRecordWrap.bean.recordId)
            }

            override fun onH2H(item: FinalListItem) {
                H2hActivity.startH2hPage(this@FinalListActivity, item.recordWin.bean.recordId, item.recordLose.bean.recordId, item.match.bean.id, MatchConstants.ROUND_ID_F)
            }
        }
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FinalListItem> {
            override fun onClickItem(view: View, position: Int, data: FinalListItem) {
                if (data.match.match.level == MatchConstants.MATCH_LEVEL_FINAL) {
                    FinalDrawActivity.startPage(this@FinalListActivity, data.match.bean.id)
                }
                else {
                    DrawActivity.startPage(this@FinalListActivity, data.match.bean.id)
                }
            }
        })
        titlesAdapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<TitleCountItem> {
            override fun onClickItem(view: View, position: Int, data: TitleCountItem) {
                DetailActivity.startRecordPage(this@FinalListActivity, data.record.id!!)
            }
        }
    }

    override fun initData() {
        mModel.dataObserver.observe(this, Observer {
            mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            adapter.list = it
            mBinding.rvList.adapter = adapter
        })
        mModel.titlesCountObserver.observe(this, Observer {
            val manager = GridLayoutManager(this, 2)
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return titlesAdapter.getSpanSize(position)
                }
            }
            mBinding.rvList.layoutManager = manager
            titlesAdapter.list = it
            mBinding.rvList.adapter = titlesAdapter
        })
        mModel.imageChanged.observe(this, Observer {
            mBinding.rvList.adapter?.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.loadData()
    }

    private fun filter() {
        val elements = MatchConstants.MATCH_LEVEL.toMutableList()
        elements.add("All")
        AlertDialogFragment()
            .setItems(elements.toTypedArray()) { dialogInterface, i ->
                if (dataType == 0) {
                    mModel.filterByLevel(i)
                }
                else {
                    mModel.filterTitlesCountByLevel(i)
                }
            }
            .show(supportFragmentManager, "AlertDialogFragment")
    }
}