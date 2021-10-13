package com.king.app.coolg_kt.page.match.rank

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.FragmentPeriodSelectorBinding
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/10/13 16:03
 */
class PeriodFragment: DraggableContentFragment<FragmentPeriodSelectorBinding>() {

    var period = 0
    val rankRepository = RankRepository()
    val adapter = PeriodAdapter()
    var lastPeriod: PeriodPack? = null

    var onPeriodSelectListener: OnPeriodSelectListener? = null

    override fun getBinding(inflater: LayoutInflater): FragmentPeriodSelectorBinding = FragmentPeriodSelectorBinding.inflate(inflater)

    override fun initData() {

        mBinding.rvPeriods.layoutManager = GridLayoutManager(context, 8)
        mBinding.rvPeriods.adapter = adapter
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<Int> {
            override fun onClickItem(view: View, position: Int, data: Int) {
                onPeriodSelectListener?.onSelectPeriod(period, data)
                dismissAllowingStateLoss()
            }
        })

        mBinding.ivPrevious.setOnClickListener {
            period --
            togglePeriod(period)
        }

        mBinding.ivNext.setOnClickListener {
            period ++
            togglePeriod(period)
        }

        mBinding.tvPeriod.setOnClickListener {
            val list = mutableListOf<String>()
            for (i in 1..(lastPeriod?.endPeriod?:0)) {
                list.add(i.toString())
            }
            if (list.size > 0) {
                AlertDialogFragment()
                    .setItems(list.toTypedArray()
                    ) { dialog, which ->
                        period = which + 1
                        togglePeriod(period)
                    }
                    .show(childFragmentManager, "AlertDialogFragment")
            }
        }

        lastPeriod = rankRepository.getCompletedPeriodPack()
        togglePeriod(period)
    }

    fun togglePeriod(period: Int) {
        mBinding.tvPeriod.text = "P$period"
        mBinding.ivNext.visibility = if (period < lastPeriod?.endPeriod?:0) View.VISIBLE else View.INVISIBLE
        mBinding.ivPrevious.visibility = if (period > 1) View.VISIBLE else View.INVISIBLE
        loadPeriodWeeks(period)
    }

    private fun loadPeriodWeeks(period: Int) {
        var totalWeeks = MatchConstants.MAX_ORDER_IN_PERIOD
        lastPeriod?.apply {
            if (endPeriod == period) {
                totalWeeks = endPIO
            }
        }
        var list = mutableListOf<Int>()
        for (i in 1..totalWeeks) {
            list.add(i)
        }
        adapter.list = list
        adapter.notifyDataSetChanged()
    }

    interface OnPeriodSelectListener {
        fun onSelectPeriod(period: Int, orderInPeriod: Int)
    }
}