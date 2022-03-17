package com.king.app.coolg_kt.page.match.list

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.databinding.FragmentScorePlanBinding
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.page.match.DrawScore
import com.king.app.coolg_kt.page.match.ScoreItem
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.gdb.data.entity.ScorePlan

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/10/20 14:15
 */
class ScorePlanDialog: DraggableContentFragment<FragmentScorePlanBinding>() {

    var adapter = ScorePlanAdapter()

    var matchId: Long = 0

    var repository = DrawRepository()

    lateinit var editScore: DrawScore

    var defScore: DrawScore? = null

    var dbScore: DrawScore? = null

    override fun getBinding(inflater: LayoutInflater): FragmentScorePlanBinding = FragmentScorePlanBinding.inflate(inflater)

    override fun initData() {
        mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        mBinding.btnAdd.setOnClickListener {
            editScore.items.add(ScoreItem(0, 0, 0))
            showItems()
        }

        mBinding.tvOk.setOnClickListener { onConfirm() }

        createRounds()

        dbScore = repository.getScorePlan(matchId)

        adapter.onDeleteItemListener = object : ScorePlanAdapter.OnDeleteItemListener {
            override fun onDelete(position: Int) {
                editScore.items.removeAt(position)
                showItems()
            }
        }
        if (dbScore == null) {
            editScore = defScore!!
            sortItems()
            mBinding.cbDefault.isChecked = true
            mBinding.etPeriod.visibility = View.GONE
        }
        else {
            editScore = dbScore!!
            sortItems()
            mBinding.cbDefault.isChecked = false
            mBinding.etPeriod.setText(editScore.period.toString())
            mBinding.etPeriod.visibility = View.VISIBLE
        }
        showItems()

        mBinding.cbDefault.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                editScore = defScore!!
            }
            else {
                if (dbScore == null) {
                    dbScore = DrawScore(matchId, mutableListOf())
                    for (item in defScore!!.items) {
                        dbScore!!.items.add(item)
                    }
                }
                mBinding.etPeriod.visibility = View.VISIBLE
                editScore = dbScore!!
            }
            showItems()
        }
    }

    fun sortItems() {
        editScore.items.sortByDescending {
            MatchConstants.getRoundSortValue(it.round)
        }
    }

    fun showItems() {
        adapter.list = editScore.items
        adapter.notifyDataSetChanged()
    }

    fun createRounds() {
        var roundList = mutableListOf<RoundPack>()
        var match = CoolApplication.instance.database!!.getMatchDao().getMatch(matchId)
        defScore = repository.getDefaultScorePlan(match)
        when(match.draws) {
            128 -> {
                roundList.addAll(MatchConstants.ROUND_MAIN_DRAW128)
                roundList.addAll(MatchConstants.ROUND_QUALIFY)
            }
            64 -> {
                roundList.addAll(MatchConstants.ROUND_MAIN_DRAW64)
                roundList.addAll(MatchConstants.ROUND_QUALIFY)
            }
            32 -> {
                roundList.addAll(MatchConstants.ROUND_MAIN_DRAW32)
                roundList.addAll(MatchConstants.ROUND_QUALIFY)
            }
        }
        adapter.roundList = roundList
        var textList = mutableListOf<String>()
        roundList.forEach { textList.add(it.shortName) }
        adapter.roundTextList = textList
    }

    private fun onConfirm() {
        var period = 0
        kotlin.runCatching {
            period = mBinding.etPeriod.text.toString().toInt()
        }
        CoolApplication.instance.database!!.getMatchDao().insertOrReplaceScorePlan(ScorePlan(matchId, period, Gson().toJson(editScore)))
        showMessageShort("success")
        dismissAllowingStateLoss()
    }
}