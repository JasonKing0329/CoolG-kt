package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.View
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.FragmentDrawPlanBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.gdb.data.relation.MatchPeriodWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/5/29 12:20
 */
class DrawPlanDialog: DraggableContentFragment<FragmentDrawPlanBinding>() {

    var matchPeriod: MatchPeriodWrap? = null
    var onDrawPlanListener: OnDrawPlanListener? = null
    var drawStrategy = SettingProperty.getDrawStrategy()

    override fun getBinding(inflater: LayoutInflater): FragmentDrawPlanBinding = FragmentDrawPlanBinding.inflate(inflater)

    override fun initData() {
        if (drawStrategy.gm1000 == null) {
            drawStrategy.gm1000 = GM1000Strategy()
        }
        if (drawStrategy.gm500 == null) {
            drawStrategy.gm500 = GM500Strategy()
        }
        if (drawStrategy.gm250 == null) {
            drawStrategy.gm250 = GM250Strategy()
        }
        if (drawStrategy.low == null) {
            drawStrategy.low = LowStrategy()
        }
        if (drawStrategy.micro == null) {
            drawStrategy.micro = MicroStrategy()
        }
        matchPeriod?.match?.let {
            when(it.level) {
                MatchConstants.MATCH_LEVEL_GM1000 -> {
                    mBinding.clGm1000.visibility = View.VISIBLE
                    mBinding.clGm500.visibility = View.GONE
                    mBinding.clLow.visibility = View.GONE
                    mBinding.clMicro.visibility = View.GONE
                    drawStrategy.gm1000?.let { str ->
                        mBinding.etGm1000Shuffle.setText(str.shuffleRate.toString())
                        mBinding.etGm1000Min.setText(str.lowRank.toString())
                    }
                }
                MatchConstants.MATCH_LEVEL_GM500 -> {
                    mBinding.clGm1000.visibility = View.GONE
                    mBinding.clGm500.visibility = View.VISIBLE
                    mBinding.clLow.visibility = View.GONE
                    mBinding.clMicro.visibility = View.GONE
                    drawStrategy.gm500?.let { str ->
                        mBinding.etGm500Top10.setText(str.top10.toString())
                        mBinding.etGm500Top20.setText(str.top20.toString())
                        mBinding.etGm500Top50.setText(str.top50.toString())
                        mBinding.etGm500MainMin.setText(str.mainLowRank.toString())
                        mBinding.etGm500QualifyMin.setText(str.qualifyLowRank.toString())
                    }
                }
                MatchConstants.MATCH_LEVEL_GM250 -> {
                    mBinding.clGm1000.visibility = View.GONE
                    mBinding.clGm500.visibility = View.VISIBLE
                    mBinding.clLow.visibility = View.GONE
                    mBinding.clMicro.visibility = View.GONE
                    drawStrategy.gm250?.let { str ->
                        mBinding.etGm500Top10.setText(str.top10.toString())
                        mBinding.etGm500Top20.setText(str.top20.toString())
                        mBinding.etGm500Top50.setText(str.top50.toString())
                        mBinding.etGm500MainMin.setText(str.mainLowRank.toString())
                        mBinding.etGm500QualifyMin.setText(str.qualifyLowRank.toString())
                    }
                }
                MatchConstants.MATCH_LEVEL_LOW -> {
                    mBinding.clGm1000.visibility = View.GONE
                    mBinding.clGm500.visibility = View.GONE
                    mBinding.clLow.visibility = View.VISIBLE
                    mBinding.clMicro.visibility = View.GONE
                    drawStrategy.low?.let { str ->
                        mBinding.etHigh.setText(str.rankTopLimit.toString())
                        mBinding.etLowMainMin.setText(str.mainLow.toString())
                        mBinding.etLowSeed.setText(str.mainSeedLow.toString())
                        mBinding.etLowQualifySeedMin.setText(str.qualifySeedLow.toString())
                        mBinding.etLowQualifyMin.setText(str.qualifyLow.toString())
                    }
                }
                MatchConstants.MATCH_LEVEL_MICRO -> {
                    mBinding.clGm1000.visibility = View.GONE
                    mBinding.clGm500.visibility = View.GONE
                    mBinding.clLow.visibility = View.GONE
                    mBinding.clMicro.visibility = View.VISIBLE
                    drawStrategy.micro?.let { str ->
                        mBinding.etMicroHigh.setText(str.rankTopLimit.toString())
                        mBinding.etMicroSeed.setText(str.mainSeedLow.toString())
                    }
                }
                else -> {}
            }
        }
        mBinding.tvOk.setOnClickListener {
            matchPeriod?.match?.let {
                when(it.level) {
                    MatchConstants.MATCH_LEVEL_GM1000 -> {
                        drawStrategy.gm1000!!.shuffleRate = mBinding.etGm1000Shuffle.text.toString().toInt()
                        drawStrategy.gm1000!!.lowRank = mBinding.etGm1000Min.text.toString().toInt()
                    }
                    MatchConstants.MATCH_LEVEL_GM500 -> {
                        drawStrategy.gm500!!.mainLowRank = mBinding.etGm500MainMin.text.toString().toInt()
                        drawStrategy.gm500!!.top10 = mBinding.etGm500Top10.text.toString().toInt()
                        drawStrategy.gm500!!.top20 = mBinding.etGm500Top20.text.toString().toInt()
                        drawStrategy.gm500!!.top50 = mBinding.etGm500Top50.text.toString().toInt()
                        drawStrategy.gm500!!.qualifyLowRank = mBinding.etGm500QualifyMin.text.toString().toInt()
                    }
                    MatchConstants.MATCH_LEVEL_GM250 -> {
                        drawStrategy.gm250!!.mainLowRank = mBinding.etGm500MainMin.text.toString().toInt()
                        drawStrategy.gm250!!.top10 = mBinding.etGm500Top10.text.toString().toInt()
                        drawStrategy.gm250!!.top20 = mBinding.etGm500Top20.text.toString().toInt()
                        drawStrategy.gm250!!.top50 = mBinding.etGm500Top50.text.toString().toInt()
                        drawStrategy.gm250!!.qualifyLowRank = mBinding.etGm500QualifyMin.text.toString().toInt()
                    }
                    MatchConstants.MATCH_LEVEL_LOW -> {
                        drawStrategy.low!!.rankTopLimit = mBinding.etHigh.text.toString().toInt()
                        drawStrategy.low!!.mainSeedLow = mBinding.etLowSeed.text.toString().toInt()
                        drawStrategy.low!!.mainLow = mBinding.etLowMainMin.text.toString().toInt()
                        drawStrategy.low!!.qualifySeedLow = mBinding.etLowQualifySeedMin.text.toString().toInt()
                        drawStrategy.low!!.qualifyLow = mBinding.etLowQualifyMin.text.toString().toInt()
                    }
                    MatchConstants.MATCH_LEVEL_MICRO -> {
                        drawStrategy.micro!!.rankTopLimit = mBinding.etMicroHigh.text.toString().toInt()
                        drawStrategy.micro!!.mainSeedLow = mBinding.etMicroSeed.text.toString().toInt()
                    }
                    else -> {}
                }
            }
            SettingProperty.setDrawStrategy(drawStrategy)
            onDrawPlanListener?.onSetPlan(drawStrategy)
            dismissAllowingStateLoss()
        }
    }

    interface OnDrawPlanListener {
        fun onSetPlan(drawStrategy: DrawStrategy)
    }
}