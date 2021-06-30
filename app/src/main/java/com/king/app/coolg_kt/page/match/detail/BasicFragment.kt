package com.king.app.coolg_kt.page.match.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.FragmentMatchDetailBasicBinding
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.widget.chart.adapter.BarChartAdapter
import com.king.app.coolg_kt.view.widget.chart.adapter.IAxis
import com.king.app.gdb.data.entity.match.MatchRankRecord

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 10:03
 */
class BasicFragment: AbsDetailChildFragment<FragmentMatchDetailBasicBinding, EmptyViewModel>() {

    private var periodType = 0

    private var tvLastSelectedPeriod: View? = null

    var colorBars = intArrayOf(
        Color.rgb(0x33, 0x99, 0xff), Color.rgb(0, 0xa5, 0xc4)
    )

    /**
     * y轴刻度
     */
    var DEGREE_POINT = intArrayOf(9999, 1000, 750, 500, 300, 200, 100, 50, 30, 10, 0)

    /**
     * y轴每一点之间包含的value映射
     * 比如300-500之间，将中间的200进行DEGREE_AREA等分（200除以DEGREE_AREA），即拥有300,320,340...500的value颗粒
     * 需要注意的是，DEGREE_AREA必须大于等于DEGREE_POINT中最小的间距（比如10,0是最小间距10-0=10，所以DEGREE_POINT必须至少为10）
     */
    var DEGREE_AREA = 10

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
        mBinding.groupRecordMatches.setOnClickListener { mainViewModel.showCareerPage.value = true }
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

        val list = mainViewModel.loadFinalRanks()
        initRankChart(list)
    }

    private fun initRankChart(list: List<MatchRankRecord>) {

        mBinding.barChartRank.setDrawValueText(true)
        mBinding.barChartRank.setDrawDashGrid(false)
        mBinding.barChartRank.setDrawAxisY(true)
        mBinding.barChartRank.setAxisX(object : IAxis {

            override fun getDegreeCount(): Int = list.size

            override fun getTotalWeight(): Int = list.size

            override fun getWeightAt(position: Int): Int = position

            override fun getTextAt(position: Int): String = "P${list[position].period}"

            override fun isNotDraw(position: Int): Boolean= false
        })
        mBinding.barChartRank.setAxisY(object : IAxis {

            override fun getDegreeCount(): Int = DEGREE_AREA * (DEGREE_POINT.size - 1) + 1

            override fun getTotalWeight(): Int = DEGREE_AREA * (DEGREE_POINT.size - 1) + 1

            override fun getWeightAt(position: Int): Int = position

            override fun getTextAt(position: Int): String {
                val rank = positionToRank(position)
                return rank.toString()
            }

            /**
             * 只显示DEGREE_POINT定义的刻度轴（中间被等分的隐藏）
             */
            override fun isNotDraw(position: Int): Boolean {
                // 0,10为最小间距，所以不能设置为1,10. 在这里把0也隐藏，显示1
                return when (val rank = positionToRank(position)) {
                    1 -> false
                    0 -> true
                    else -> !DEGREE_POINT.contains(rank)
                }
            }
        })
        mBinding.barChartRank.setAdapter(object : BarChartAdapter() {
            override fun getXCount(): Int = list.size

            override fun getBarColor(position: Int): Int = colorBars[position % 2]

            override fun getValueWeight(xIndex: Int): Int = rankToDegree(list[xIndex].rank)

            override fun getValueText(xIndex: Int): String = list[xIndex].rank.toString()
        })

    }

    /**
     * y 刻度对应的rank
     * @param position
     * @return
     */
    private fun positionToRank(position: Int): Int {
        val max = DEGREE_POINT[position / DEGREE_AREA]
        val min =
            if (position / DEGREE_AREA + 1 == DEGREE_POINT.size) max else DEGREE_POINT[position / DEGREE_AREA + 1]
        return max - (max - min) / DEGREE_AREA * (position % DEGREE_AREA)
    }

    /**
     * rank对应的y刻度
     * @param rank
     * @return
     */
    private fun rankToDegree(rank: Int): Int {
        var rank = rank
        if (rank == 0 || rank > 9999) {
            rank = 9999
        }
        var degree = 0
        for (i in DEGREE_POINT.indices) {
            if (i < DEGREE_POINT.size - 1) {
                if (rank <= DEGREE_POINT[i] && rank > DEGREE_POINT[i + 1]) {
                    val max = DEGREE_POINT[i]
                    val min = DEGREE_POINT[i + 1]
                    val piece = (max - min) / DEGREE_AREA
                    degree = i * DEGREE_AREA + (max - rank) / piece
                    break
                }
            }
        }
        return degree
    }

}