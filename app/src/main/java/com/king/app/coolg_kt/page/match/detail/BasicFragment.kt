package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.FragmentMatchDetailBasicBinding
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 10:03
 */
class BasicFragment: AbsDetailChildFragment<FragmentMatchDetailBasicBinding, EmptyViewModel>() {

    private var periodType = 0

    private var tvLastSelectedPeriod: View? = null

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView(view: View) {

        onSelectChanged(mBinding.tvPeriodRank)

        mBinding.tvPeriodRank.setOnClickListener {
            if (periodType != 0) {
                onSelectChanged(mBinding.tvPeriodRank)
                periodType = 0
                mainViewModel.loadBasic(0, 0)
            }
        }
        mBinding.tvPeriodAll.setOnClickListener {
            if (periodType != 1) {
                onSelectChanged(mBinding.tvPeriodAll)
                periodType = 1
                mainViewModel.loadBasic(1, 0)
            }
        }
        mBinding.tvPeriodSpecific.setOnClickListener {
            selectPeriod()
        }
        mBinding.groupRankHigh.setOnClickListener { mainViewModel.showRankDialog.value = true }
        mBinding.groupRankLow.setOnClickListener { mainViewModel.showRankDialog.value = true }
        mBinding.groupH2h.setOnClickListener { mainViewModel.showH2hPage.value = true }
    }

    private fun onSelectChanged(target: View) {
        tvLastSelectedPeriod?.isSelected = false
        target.isSelected = true
        tvLastSelectedPeriod = target
    }

    private fun selectPeriod() {
        val array = mainViewModel.getPeriodsToSelect()
        AlertDialogFragment()
            .setItems(array) { dialog, which ->
                onSelectChanged(mBinding.tvPeriodSpecific)
                periodType = 2
                mBinding.tvPeriodSpecific.isSelected = true

                mainViewModel.loadBasic(2, which + 1)
                mBinding.tvPeriodSpecific.text = "Period ${which + 1}"
            }
            .show(childFragmentManager, "selectPeriod")
    }

    override fun getBinding(inflater: LayoutInflater): FragmentMatchDetailBasicBinding = FragmentMatchDetailBasicBinding.inflate(inflater)

    override fun initData() {

        mainViewModel.loadBasic(0, 0)
        mBinding.bean = mainViewModel.detailBasic
    }
}