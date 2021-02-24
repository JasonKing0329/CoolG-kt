package com.king.app.coolg_kt.page.match.draw

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchDrawFinalBinding
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.match.h2h.H2hActivity
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/19 17:47
 */
class FinalDrawActivity: BaseActivity<ActivityMatchDrawFinalBinding, FinalDrawViewModel>() {

    companion object {
        val EXTRA_MATCH_PERIOD_ID = "match_period_id"
        fun startPage(context: Context, id: Long) {
            var intent = Intent(context, FinalDrawActivity::class.java)
            intent.putExtra(EXTRA_MATCH_PERIOD_ID, id)
            context.startActivity(intent)
        }
    }

    val ACTION_SAVE_DRAW = 1111111111

    var isEditing = false

    var adapter = FinalDrawAdapter()

    override fun getContentView(): Int = R.layout.activity_match_draw_final

    override fun createViewModel(): FinalDrawViewModel = generateViewModel(FinalDrawViewModel::class.java)

    override fun initView() {
//        mBinding.model = mModel
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_edit -> {
                    isEditing = true
                    mBinding.actionbar.showConfirmStatus(it)
                }
                R.id.menu_create_draw -> {
                    if (mModel.isDrawExist()) {
                        showConfirmCancelMessage("Current match draw is already exist, do you want clear it?",
                            DialogInterface.OnClickListener { dialog, which -> mModel.createDraw() },
                            null)
                    }
                    else {
                        mModel.createDraw()
                    }
                }
                R.id.menu_create_score -> {
                    if (mModel.isDrawExist()) {
                        showConfirmCancelMessage("This action will clear all existed score data, continue?",
                            DialogInterface.OnClickListener { dialog, which -> mModel.createScore() },
                            null)
                    }
                    else {
                        mModel.createScore()
                    }
                }
            }
        }

        mBinding.actionbar.setOnConfirmListener {
            when(it) {
                ACTION_SAVE_DRAW -> mModel.saveDraw()
                R.id.menu_edit -> mModel.saveEdit()
            }
            false
        }
        mBinding.actionbar.setOnCancelListener {
            var autoCancel = when(it) {
                ACTION_SAVE_DRAW -> {
                    showConfirmCancelMessage("Are you sure to drop this draw?",
                        DialogInterface.OnClickListener { dialog, which -> mModel.cancelSaveDraw() },
                        null)
                    false
                }
                R.id.menu_edit -> {
                    if (mModel.isModified()) {
                        showConfirmCancelMessage("Are you sure to drop the edit?",
                            DialogInterface.OnClickListener { dialog, which ->
                                mModel.cancelEdit()
                                mBinding.actionbar.cancelConfirmStatus()
                            },
                            null)
                        false
                    }
                    else {
                        true
                    }
                }
                else -> true
            }
            autoCancel
        }

        val manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position)
            }
        }
        mBinding.rvList.layoutManager = manager

        adapter.onDrawListener = object : FinalDrawAdapter.OnDrawListener {
            override fun onClickPlayer(position: Int, bean: Record?) {
                bean?.let { recordPage(it) }
            }

            override fun onPlayerWin(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?) {
                if (isEditing) {
                    drawItem.winner = bean
                    drawItem.isChanged = true
                    adapter.notifyItemChanged(position)
                }
            }

            override fun onClickH2H(position: Int, drawItem: DrawItem) {
                kotlin.runCatching {
                    H2hActivity.startH2hPage(this@FinalDrawActivity, drawItem.matchRecord1!!.bean.recordId, drawItem.matchRecord2!!.bean.recordId)
                }
            }
        }
        mBinding.rvList.adapter = adapter
    }

    private fun recordPage(record: Record) {
        DetailActivity.startRecordPage(this, record.id!!)
    }

    override fun initData() {

        mModel.dataObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.newDrawCreated.observe(this, Observer {
            mBinding.actionbar.showConfirmStatus(ACTION_SAVE_DRAW)
        })
        mModel.cancelConfirmCancelStatus.observe(this, Observer { mBinding.actionbar.cancelConfirmStatus() })
        mModel.saveEditSuccess.observe(this, Observer {
            mBinding.actionbar.cancelConfirmStatus()
            isEditing = false
        })
        mModel.loadMatch(intent.getLongExtra(DrawActivity.EXTRA_MATCH_PERIOD_ID, -1))
    }
}