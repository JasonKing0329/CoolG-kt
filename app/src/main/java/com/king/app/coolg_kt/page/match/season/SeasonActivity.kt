package com.king.app.coolg_kt.page.match.season

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.ActivitySeasonBinding
import com.king.app.coolg_kt.page.match.draw.DrawActivity
import com.king.app.coolg_kt.page.match.draw.FinalDrawActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.entity.match.MatchPeriod
import com.king.app.gdb.data.relation.MatchPeriodWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/4/17 11:02
 */
class SeasonActivity: BaseActivity<ActivitySeasonBinding, SeasonViewModel>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, SeasonActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var adapter = SeasonAdapter()

    override fun getContentView(): Int = R.layout.activity_season

    override fun createViewModel(): SeasonViewModel = generateViewModel(SeasonViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when (it) {
                R.id.menu_add -> {
                    if (mModel.isRankCreated()) {
                        editMatch(null)
                    }
                }
            }
        }
        mBinding.actionbar.registerPopupMenuOn(
            R.id.menu_filter,
            R.menu.menu_level
        ) {
            when(it.itemId) {
                R.id.menu_level_all -> mModel.filterByLevel(MatchConstants.MATCH_LEVEL_ALL)
                R.id.menu_level_gs -> mModel.filterByLevel(MatchConstants.MATCH_LEVEL_GS)
                R.id.menu_level_gm1000 -> mModel.filterByLevel(MatchConstants.MATCH_LEVEL_GM1000)
                R.id.menu_level_gm500 -> mModel.filterByLevel(MatchConstants.MATCH_LEVEL_GM500)
                R.id.menu_level_gm250 -> mModel.filterByLevel(MatchConstants.MATCH_LEVEL_GM250)
                R.id.menu_level_low -> mModel.filterByLevel(MatchConstants.MATCH_LEVEL_LOW)
                R.id.menu_level_micro -> mModel.filterByLevel(MatchConstants.MATCH_LEVEL_MICRO)
            }
            true
        }

        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration(){
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                outRect.top = ScreenUtils.dp2px(8f)
            }
        })

        mBinding.ivNext.setOnClickListener { mModel.nextPeriod() }
        mBinding.ivPrevious.setOnClickListener { mModel.lastPeriod() }
        mBinding.tvWeek.setOnClickListener {
            val list = mutableListOf<String>()
            for (i in mModel.endPeriod downTo 1) {
                list.add(i.toString())
            }
            if (list.size > 0) {
                AlertDialogFragment()
                    .setItems(list.toTypedArray()
                    ) { dialog, which ->
                        mModel.targetPeriod(mModel.endPeriod - which)
                    }
                    .show(supportFragmentManager, "AlertDialogFragment")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mModel != null) {
            mModel.loadMatches()
        }
    }

    private fun editMatch(editMatch: MatchPeriodWrap?) {
        var content = SeasonEditor()
        content.matchPeriod = editMatch
        content.onMatchListener = object : SeasonEditor.OnMatchListener {
            override fun onSeasonMatchUpdated(match: MatchPeriod) {
                mModel.insertOrUpdate(match)
            }
        }
        var dialog = DraggableDialogFragment()
        val title = if (editMatch == null) "Add" else "Edit"
        dialog.setTitle(title)
        dialog.contentFragment = content
        dialog.show(supportFragmentManager, "SeasonEditor")
    }

    override fun initData() {
        mModel.matchesObserver.observe(this, { showMatches(it) })
        mModel.imageChanged.observe(this, { adapter.notifyItemRangeChanged(it.start, it.count) })
        mModel.loadMatches()
    }

    private fun showMatches(list: List<MatchPeriodWrap>) {
        adapter.list = list
        if (mBinding.rvList.adapter == null) {
            adapter.onActionListener = object : SeasonAdapter.OnActionListener {
                override fun onDeleteItem(position: Int, bean: MatchPeriodWrap) {
                    showConfirmCancelMessage("Are you sure to delete this match?"
                        , DialogInterface.OnClickListener { dialog, which ->  mModel.deleteMatch(bean.bean)}
                        , null)
                }

                override fun onEditItem(position: Int, bean: MatchPeriodWrap) {
                    editMatch(bean)
                }
            }
            adapter.listenerClick = object : BaseBindingAdapter.OnItemClickListener<MatchPeriodWrap> {
                override fun onClickItem(view: View, position: Int, match: MatchPeriodWrap) {
                    if (match.match.level == MatchConstants.MATCH_LEVEL_FINAL) {
                        FinalDrawActivity.startPage(this@SeasonActivity, match.bean.id)
                    }
                    else {
                        DrawActivity.startPage(this@SeasonActivity, match.bean.id)
                    }
                }
            }
            mBinding.rvList.adapter = adapter
        }
        else {
            adapter.notifyDataSetChanged()
        }
    }

}