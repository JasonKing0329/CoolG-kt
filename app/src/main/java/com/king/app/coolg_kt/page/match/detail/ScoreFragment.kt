package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.databinding.FragmentMatchDetailScoreBinding
import com.king.app.coolg_kt.page.match.ScoreBean
import com.king.app.coolg_kt.page.match.ScoreHead
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 10:03
 */
class ScoreFragment: AbsDetailChildFragment<FragmentMatchDetailScoreBinding, ScoreViewModel>() {

    val adapter = ScoreAdapter()

    override fun createViewModel(): ScoreViewModel = generateViewModel(ScoreViewModel::class.java)

    override fun getBinding(inflater: LayoutInflater): FragmentMatchDetailScoreBinding = FragmentMatchDetailScoreBinding.inflate(inflater)

    override fun initView(view: View) {

        mBinding.rvList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        adapter.onPageListener = object : ScoreAdapter.OnPageListener {

            override fun onClickScore(position: Int, data: ScoreBean) {
                showRoadDialog(data)
            }

            override fun onPeriodType(type: Int, scoreHead: ScoreHead) {
                when(type) {
                    0 -> {
                        if (scoreHead.selectedType != 0) {
                            mModel.loadRankPeriod()
                            scoreHead.selectedType = 0
                        }
                    }
                    1 -> {
                        if (scoreHead.selectedType != 1) {
                            mModel.loadRaceToFinal()
                            scoreHead.selectedType = 1
                        }
                    }
                    2 -> {
                        val array = mainViewModel.getPeriodsToSelect()
                        AlertDialogFragment()
                            .setItems(array) { dialog, which ->
                                mModel.loadPeriod(which + 1)
                                scoreHead.selectedType = 2
                                scoreHead.periodSpecificText = "Period ${which + 1}"
                            }
                            .show(childFragmentManager, "selectPeriod")
                    }
                }
            }
        }
    }

    private fun showRoadDialog(data: ScoreBean) {
        var content = RoadDialog()
        content.matchPeriodId = data.matchItem.matchId
        content.recordId = mainViewModel.mRecordId
        var dialog = DraggableDialogFragment()
        dialog.setTitle("Upgrade Road")
        dialog.contentFragment = content
        dialog.show(childFragmentManager, "RoadDialog")
    }

    override fun initData() {
        mModel.recordId = mainViewModel.recordWrap!!.bean.id!!
        mModel.scoresObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadRankPeriod()
    }
}