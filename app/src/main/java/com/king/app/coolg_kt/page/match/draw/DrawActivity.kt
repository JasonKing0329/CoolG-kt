package com.king.app.coolg_kt.page.match.draw

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.databinding.ActivityMatchDrawBinding
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 15:11
 */
class DrawActivity: BaseActivity<ActivityMatchDrawBinding, DrawViewModel>() {

    val ACTION_SAVE_DRAW = 1111111111

    var REQUEST_SELECT_WILDCARD = 11901

    companion object {
        val EXTRA_MATCH_PERIOD_ID = "match_period_id"
        fun startPage(context: Context, id: Long) {
            var intent = Intent(context, DrawActivity::class.java)
            intent.putExtra(EXTRA_MATCH_PERIOD_ID, id)
            context.startActivity(intent)
        }
    }

    val adapter = DrawAdapter()

    var isEditing = false

    override fun getContentView(): Int = R.layout.activity_match_draw

    override fun createViewModel(): DrawViewModel = generateViewModel(DrawViewModel::class.java)

    override fun initView() {

        mBinding.model = mModel
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
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter.onDrawListener = object : DrawAdapter.OnDrawListener {
            override fun onClickPlayer(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?) {
                bean?.let {
                    when(it.bean.type) {
                        MatchConstants.MATCH_RECORD_NORMAL -> recordPage(it.record)
                        MatchConstants.MATCH_RECORD_QUALIFY -> {
                            if (it.bean.recordId != 0L) {
                                recordPage(it.record)
                            }
                        }
                        MatchConstants.MATCH_RECORD_WILDCARD -> {
                            if (isEditing) {
                                selectWildCardRecord(position, drawItem, it)
                            }
                            else {
                                recordPage(it.record)
                            }
                        }
                    }
                }
            }

            override fun onPlayerWin(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?) {
                if (isEditing) {
                    drawItem.winner = bean
                    drawItem.isChanged = true
                    adapter.notifyItemChanged(position)
                }
            }
        }
        mBinding.rvList.adapter = adapter

        mBinding.spRound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                DebugLog.e()
                mModel.onRoundPositionChanged(position)
            }

        }

        mBinding.tvMain.isSelected = true
        mBinding.tvMain.setOnClickListener {
            mModel.drawType = MatchConstants.DRAW_MAIN
            mModel.onDrawTypeChanged()
            mBinding.tvMain.isSelected = true
            mBinding.tvQualify.isSelected = false
        }
        mBinding.tvQualify.setOnClickListener {
            mModel.drawType = MatchConstants.DRAW_QUALIFY
            mModel.onDrawTypeChanged()
            mBinding.tvMain.isSelected = false
            mBinding.tvQualify.isSelected = true
        }
    }

    private fun selectWildCardRecord(position: Int, drawItem: DrawItem, recordWrap: MatchRecordWrap) {
        mModel.mToSetWildCard = drawItem
        mModel.mToSetWildCardPosition = position
        mModel.mToSetWildCardRecord = recordWrap
        PhoneRecordListActivity.startPageToSelect(this, REQUEST_SELECT_WILDCARD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_WILDCARD) {
            if (resultCode == Activity.RESULT_OK) {
                val recordId = data?.getLongExtra(PhoneRecordListActivity.RESP_RECORD_ID, -1)
                mModel.setWildCard(recordId!!)
                adapter.notifyItemChanged(mModel.mToSetWildCardPosition!!)
            }
        }
    }

    private fun recordPage(record: Record?) {
        record?.let {
            RecordActivity.startPage(this, it.id!!)
        }
    }

    override fun initData() {

        mModel.cancelConfirmCancelStatus.observe(this, Observer { mBinding.actionbar.cancelConfirmStatus() })
        mModel.setRoundPosition.observe(this, Observer { mBinding.spRound.setSelection(it) })
        mModel.roundList.observe(this, Observer {
            mBinding.spRound.adapter = RoundAdapter(it)
        })
        mModel.newDrawCreated.observe(this, Observer {
            mBinding.actionbar.showConfirmStatus(ACTION_SAVE_DRAW)
        })
        mModel.itemsObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.saveEditSuccess.observe(this, Observer {
            mBinding.actionbar.cancelConfirmStatus()
            isEditing = false
        })
        mModel.loadMatch(intent.getLongExtra(EXTRA_MATCH_PERIOD_ID, -1))
    }

    class RoundAdapter constructor(var list: List<RoundPack>): BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var contentView = convertView
                ?: LayoutInflater.from(parent.context).inflate(android.R.layout.simple_dropdown_item_1line, null)
            val textView = contentView.findViewById<TextView>(android.R.id.text1)
            textView.text = list[position].shortName
            return contentView
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return list[position].id.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

    }
}