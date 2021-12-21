package com.king.app.coolg_kt.page.match.rank

import android.graphics.Color
import android.view.LayoutInflater
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.databinding.FragmentDialogRankBinding
import com.king.app.coolg_kt.page.match.AxisDegree
import com.king.app.coolg_kt.page.match.LineChartData
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.coolg_kt.view.widget.chart.adapter.IAxis
import com.king.app.coolg_kt.view.widget.chart.adapter.LineChartAdapter
import com.king.app.coolg_kt.view.widget.chart.adapter.LineData
import com.king.app.gdb.data.entity.match.MatchRankRecord

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/7 12:16
 */
class RankDialog: DraggableContentFragment<FragmentDialogRankBinding>() {

    /**
     * x轴每隔n个点显示一个刻度
     */
    var DEGREE_COMBINE = 4

    /**
     * y轴每个显示刻度之间的隐藏刻度
     */
    var DEGREE_AREA = 10

    /**
     * y轴显示的刻度
     */
    var DEGREE_POINT_LINE = intArrayOf(2000, 1200, 1000, 800, 600, 400, 200, 100, 80, 60, 50, 40, 30, 20, 10, 0)
    
    val database = CoolApplication.instance.database!!

    var recordId: Long = 0L

    var zoomParams = listOf(
        ZoomParam(24, 1f),
        ZoomParam(12, 2f),
        ZoomParam(8, 3f),
        ZoomParam(6, 4f),
        ZoomParam(4, 6f),
        ZoomParam(3, 8f)
    )
    var zoomCursor = 4

    data class ZoomParam(
        var degreeCombine: Int,
        var cellWidth: Float
    )

    /**
     * 根布局是match_parent，需要覆盖为true
     */
    override fun fixHeightAsMaxHeight(): Boolean {
        return true
    }

    override fun getBinding(inflater: LayoutInflater): FragmentDialogRankBinding = FragmentDialogRankBinding.inflate(inflater)

    override fun initData() {
        mBinding.ivZoomIn.setOnClickListener {
            if (zoomCursor < zoomParams.size - 1) {
                zoomCursor++
                zoomWith(zoomCursor)
            }
        }
        mBinding.ivZoomOut.setOnClickListener {
            if (zoomCursor > 0) {
                zoomCursor--
                zoomWith(zoomCursor)
            }
        }

        zoomWith(zoomCursor)
    }

    private fun zoomWith(cursor: Int) {
        DEGREE_COMBINE = zoomParams[cursor].degreeCombine
        mBinding.chartRank.setMinXCellWidth(ScreenUtils.dp2px(zoomParams[cursor].cellWidth))
        refresh()
    }

    private fun refresh() {
        loadRankData()?.apply {
            showChart(this)
        }
    }

    private fun loadRankData(): LineChartData? {
        val ranks = database.getMatchDao().getRecordRanks(recordId)
        if (ranks.isEmpty())
            return null

        val chartData = LineChartData()
        // axis y data
        chartData.axisYCount = DEGREE_AREA * (DEGREE_POINT_LINE.size - 1) + 1
        chartData.axisYTotalWeight = chartData.axisYCount
        for (i in 0 until chartData.axisYCount) {
            // 转换y轴数据坐标
            val rank: Int = positionToRankLine(i)
            val degree: AxisDegree<Int> = AxisDegree()
            degree.weight = i
            degree.isNotDraw = !isKeyDegree(rank)
            degree.text = rank.toString()
            degree.data = rank
            chartData.axisYDegreeList.add(degree)
        }

        val data = LineData()
        chartData.lineList.add(data)
        data.startX = 0
        data.endX = ranks.size - 1
        data.values = mutableListOf()
        data.valuesText = mutableListOf()
        data.colors = mutableListOf()
        // 颜色采用按周期，3色交替的方案
        var colors = listOf(Color.parseColor("#3399ff"), Color.parseColor("#C93437"), Color.parseColor("#BDB626"))
        var lastPeriod = -1
        var colorPack = ColorPack(0, -1)
        ranks.forEachIndexed { index, item ->
            if (item.period != lastPeriod) {
                lastPeriod = item.period
                nextColor(colors, colorPack)
            }
            data.colors.add(colorPack.color)
            // 构建每一个刻度(星期)对应的value
            buildWeek(data, ranks, index, 0, chartData)
        }
        chartData.axisXCount = ranks.size
        chartData.axisXTotalWeight = ranks.size
        return chartData
    }

    /**
     * y 刻度对应的rank
     * @param position
     * @return
     */
    private fun positionToRankLine(position: Int): Int {
        val max = DEGREE_POINT_LINE[position / DEGREE_AREA]
        val min = if (position / DEGREE_AREA + 1 == DEGREE_POINT_LINE.size) max 
        else DEGREE_POINT_LINE[position / DEGREE_AREA + 1]
        return max - (max - min) / DEGREE_AREA * (position % DEGREE_AREA)
    }

    /**
     * 需要刻画的y轴刻度
     * @param rank
     * @return
     */
    private fun isKeyDegree(rank: Int): Boolean {
        if (rank == 1) {
            return true
        }
        if (rank == 0) {
            return false
        }
        for (i in DEGREE_POINT_LINE.indices) {
            if (rank == DEGREE_POINT_LINE[i]) {
                return true
            }
        }
        return false
    }

    /**
     * 构建每一周rank映射于坐标轴的degree, value
     * @param data 所属的LineData（LineChart描述一组连续连线的数据）
     * @param rankList week rank
     * @param indexOfRank index in rankList
     * @param offset x坐标轴的位置为 indexOfRank + offset
     * @param chartData chart data
     */
    private fun buildWeek(
        data: LineData,
        rankList: List<MatchRankRecord>,
        indexOfRank: Int,
        offset: Int,
        chartData: LineChartData
    ) {
        val week = rankList[indexOfRank]
        val degreeValue = rankToDegreeLine(week.rank)
        data.values.add(degreeValue)
        val size = data.values.size
        // 名次变化才显示value
        if (size > 1) {
            // 出现名次变化才显示
            if (degreeValue != data.values[size - 2]) {
                data.valuesText.add(week.rank.toString())
            } else {
                data.valuesText.add(null)
            }
        } else {
            data.valuesText.add(week.rank.toString())
        }
        val degree: AxisDegree<MatchRankRecord> = AxisDegree()
        degree.weight = indexOfRank + offset
        degree.isNotDraw = false
        degree.text = if (indexOfRank % DEGREE_COMBINE == 0) "P${week.period}-W${week.orderInPeriod}"
        else ""
        degree.data = week
        chartData.axisXDegreeList.add(degree)
    }

    /**
     * rank对应的y刻度
     * @param rank
     * @return
     */
    private fun rankToDegreeLine(rank: Int): Int {
        var rank = rank
        if (rank == 0 || rank > 2000) {
            rank = 2000
        }
        var degree = 0
        for (i in DEGREE_POINT_LINE.indices) {
            if (i < DEGREE_POINT_LINE.size - 1) {
                if (rank <= DEGREE_POINT_LINE[i] && rank > DEGREE_POINT_LINE[i + 1]) {
                    val max = DEGREE_POINT_LINE[i]
                    val min = DEGREE_POINT_LINE[i + 1]
                    val piece = (max - min) / DEGREE_AREA
                    degree = i * DEGREE_AREA + (max - rank) / piece
                    break
                }
            }
        }
        return degree
    }

    private fun showChart(data: LineChartData) {
        mBinding.chartRank.setDegreeCombine(DEGREE_COMBINE)
        mBinding.chartRank.setDrawAxisY(true)
        mBinding.chartRank.setAxisX(object : IAxis {
            override fun getTextAt(position: Int): String {
                return data.axisXDegreeList[position].text?:""
            }

            override fun isNotDraw(position: Int): Boolean {
                return data.axisXDegreeList[position].isNotDraw
            }

            override fun getTotalWeight(): Int {
                return data.axisXTotalWeight
            }

            override fun getDegreeCount(): Int {
                return data.axisXCount
            }

            override fun getWeightAt(position: Int): Int {
                return data.axisXDegreeList[position].weight
            }
        })
        mBinding.chartRank.setAxisY(object : IAxis {
            override fun getTextAt(position: Int): String {
                return data.axisYDegreeList[position].text?:""
            }

            override fun isNotDraw(position: Int): Boolean {
                return data.axisYDegreeList[position].isNotDraw
            }

            override fun getTotalWeight(): Int {
                return data.axisYTotalWeight
            }

            override fun getDegreeCount(): Int {
                return data.axisYCount
            }

            override fun getWeightAt(position: Int): Int {
                return data.axisYDegreeList[position].weight
            }
        })
        mBinding.chartRank.setAdapter(object : LineChartAdapter() {
            override fun getLineData(lineIndex: Int): LineData {
                return data.lineList[lineIndex]
            }

            override fun getLineCount(): Int {
                return data.lineList.size
            }

        })
        mBinding.chartRank.scrollToEnd()
    }

    data class ColorPack (
        var color: Int,
        var index: Int
    )

    private fun nextColor(colors: List<Int>, pack: ColorPack) {
        pack.index ++
        if (pack.index >= colors.size) {
            pack.index = 0
        }
        pack.color = colors[pack.index]
    }

}