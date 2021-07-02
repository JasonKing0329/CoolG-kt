package com.king.app.coolg_kt.page.match.season

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.databinding.FragmentSeasonEditorBinding
import com.king.app.coolg_kt.page.match.list.MatchListActivity
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.coolg_kt.view.DateManager
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchPeriod
import com.king.app.gdb.data.relation.MatchPeriodWrap
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/4/17 11:17
 */
class SeasonEditor: DraggableContentFragment<FragmentSeasonEditorBinding>() {

    val REQUEST_SELECT_MATCH = 11

    var matchPeriod: MatchPeriodWrap? = null

    var match: Match? = null

    var onMatchListener: OnMatchListener? = null

    var date: Long = 0

    var dateManager = DateManager()

    override fun getBinding(inflater: LayoutInflater): FragmentSeasonEditorBinding = FragmentSeasonEditorBinding.inflate(inflater)

    override fun initData() {
        mBinding.tvOk.setOnClickListener { onConfirm() }
        mBinding.btnDate.setOnClickListener {
            dateManager.date = Date(date)
            dateManager.pickDate(requireContext(), object : DateManager.OnDateListener {
                override fun onDateSet() {
                    date = dateManager.date!!.time
                    mBinding.btnDate.text = FormatUtil.formatDate(date)
                }
            })
        }
        // new
        if (matchPeriod == null) {
            CoolApplication.instance.database!!.getMatchDao().getLastCompletedMatchPeriod()?.let {
                mBinding.etPeriod.setText(it.period.toString())
            }
            date = System.currentTimeMillis()
            mBinding.btnDate.text = FormatUtil.formatDate(date)
        }
        // update
        else {
            match = matchPeriod!!.match
            date = matchPeriod!!.bean.date
            mBinding.tvSelectMatch.text = matchPeriod!!.match.name
            mBinding.btnDate.text = FormatUtil.formatDate(date)
            mBinding.etPeriod.setText(matchPeriod!!.bean.period.toString())
            mBinding.tvOrderInPeriod.text = "W${matchPeriod!!.bean.orderInPeriod}"
            mBinding.etWcMain.setText(matchPeriod!!.bean.mainWildcard.toString())
            mBinding.etWcQualify.setText(matchPeriod!!.bean.qualifyWildcard.toString())
        }
        mBinding.tvSelectMatch.setOnClickListener {
            MatchListActivity.startPageToSelect(this@SeasonEditor, REQUEST_SELECT_MATCH)
        }
    }

    private fun onConfirm() {
        if (match == null) {
            showMessageShort("Please select match!")
            return
        }
        var period: Int
        try {
            period = mBinding.etPeriod.text.toString().toInt()
        } catch (e: Exception){
            showMessageShort("Error period")
            return
        }
        var wcMain: Int
        try {
            wcMain = mBinding.etWcMain.text.toString().toInt()
        } catch (e: Exception){
            showMessageShort("Error period")
            return
        }
        var wcQualify: Int
        try {
            wcQualify = mBinding.etWcQualify.text.toString().toInt()
        } catch (e: Exception){
            showMessageShort("Error period")
            return
        }
        if (matchPeriod == null) {
            val bean = MatchPeriod(0, match!!.id, date, period, match!!.orderInPeriod, false, false, wcMain, wcQualify)
            onMatchListener?.onSeasonMatchUpdated(bean)
        }
        else {
            matchPeriod?.let {
                it.bean.matchId = match!!.id
                it.bean.period = period
                it.bean.date = date
                it.bean.mainWildcard = wcMain
                it.bean.qualifyWildcard = wcQualify

                onMatchListener?.onSeasonMatchUpdated(it.bean)
            }
        }
        dismissAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_MATCH) {
            if (resultCode == Activity.RESULT_OK) {
                var matchId = data?.getLongExtra(MatchListActivity.RESP_MATCH_ID, -1)!!
                match = CoolApplication.instance.database!!.getMatchDao().getMatch(matchId)
                match?.let {
                    mBinding.tvSelectMatch.text = it.name
                }
            }
        }
    }

    interface OnMatchListener {
        fun onSeasonMatchUpdated(match: MatchPeriod)
    }
}