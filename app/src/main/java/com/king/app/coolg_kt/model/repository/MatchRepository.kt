package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.RecordMatchCounter
import com.king.app.coolg_kt.page.match.RecordMatchCounterStringResult
import com.king.app.coolg_kt.page.match.detail.CareerMatchViewModel
import com.king.app.gdb.data.entity.match.MatchItem

class MatchRepository: BaseRepository() {

    /**
     * 统计record在match中的相关数据
     */
    fun countRecordMatchItems(counter: RecordMatchCounter): RecordMatchCounterStringResult {
        val result = RecordMatchCounterStringResult()
        val items = getDatabase().getMatchDao().getRecordMatchItems(counter.recordId, counter.matchId)
        var win = 0
        var lose = 0
        var titles = 0
        val periodMap = mutableMapOf<Long, MutableList<MatchItem>?>()
        if (counter.isCountTitles || counter.isCountWinLose) {
            items.forEach { item ->
                if (item.winnerId == counter.recordId) {
                    win ++
                    if (item.round == MatchConstants.ROUND_ID_F) {
                        titles ++
                    }
                }
                else if (item.winnerId?:0L != 0L) {
                    lose ++
                }
                var list = periodMap[item.matchId]
                if (list == null) {
                    list = mutableListOf()
                    periodMap[item.matchId] = list
                }
                list.add(item)
            }
            // win lose
            result.winLose = "${win}胜${lose}负"
            // titles
            result.titles = titles.toString()
        }
        // best，最多只取最好的2个轮次
        if (counter.isCountBest) {
            val countList = mutableListOf<CareerMatchViewModel.MatchCount>()
            periodMap.keys.forEach { matchPeriodId ->
                val count = toMatchCount(counter.recordId, matchPeriodId, periodMap[matchPeriodId]!!)
                countList.add(count)
            }
            countList.sortByDescending { it.roundWeight }
            val bestMap = mutableMapOf<Int, MutableList<CareerMatchViewModel.MatchCount>?>()
            for (i in countList.indices) {
                var bm = bestMap[countList[i].roundWeight]
                if (bm == null) {
                    if (bestMap.keys.size == 2) {
                        break
                    }
                    bm = mutableListOf()
                    bestMap[countList[i].roundWeight] = bm
                }
                bm.add(countList[i])
            }
            result.bestResults = mutableListOf()
            bestMap.keys.sortedDescending().forEachIndexed { index, key ->
                result.bestResults?.add(toPeriodRound(bestMap[key]!!))
            }
        }
        return result
    }

    private fun toMatchCount(recordId: Long, matchPeriodId: Long, list: List<MatchItem>): CareerMatchViewModel.MatchCount {
        val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        var maxWeight = -9999
        var maxRoundItem: MatchItem? = null
        list.forEach {
            var weight = MatchConstants.getRoundSortValue(it.round)
            // Win要跟F分开算
            if (it.round == MatchConstants.ROUND_ID_F && it.winnerId == recordId) {
                weight ++
            }
            if (weight > maxWeight) {
                maxWeight = weight
                maxRoundItem = it
            }
        }
        val roundShort = MatchConstants.roundResultShort(maxRoundItem!!.round, maxRoundItem!!.winnerId == recordId)
        return CareerMatchViewModel.MatchCount(
            matchPeriodId,
            matchPeriod.bean.period,
            roundShort,
            maxWeight
        )
    }

    private fun toPeriodRound(list: List<CareerMatchViewModel.MatchCount>): String {
        val buffer = StringBuffer()
        buffer.append(list[0].roundShort).append("(")
        list.forEachIndexed { index, it ->
            if (index > 0) {
                buffer.append(",")
            }
            buffer.append("P").append(it.period)
        }
        buffer.append(")  ")
        return buffer.toString()
    }

}