package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.CareerCategoryMatch
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.gdb.data.entity.match.MatchItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/7 16:11
 */
class CareerMatchViewModel(application: Application): BaseViewModel(application) {

    val SORT_BY_WEEK = 0
    val SORT_BY_TIMES = 1
    val SORT_BY_LEVEL = 2

    private var totalMatches = mutableListOf<CareerCategoryMatch>()
    var matchesObserver = MutableLiveData<List<Any>>()
    var rangeChangedObserver = MutableLiveData<TimeWasteRange>()

    var mRecordId: Long = 0
    var rankRepository = RankRepository()

    private var isOnlyJoinedMatches = false
    private var sortType = SORT_BY_WEEK

    fun loadMatches() {
        basicAndTimeWaste(
            blockBasic = { basicMatches() },
            onCompleteBasic = { matchesObserver.value = it },
            blockWaste = { _, it ->  handleItem(it) },
            wasteNotifyCount = 5,
            onWasteRangeChanged = { start, count -> rangeChangedObserver.value = TimeWasteRange(start, count) },
            withBasicLoading = true
        )
    }

    private fun basicMatches(): List<Any> {
        var list = getDatabase().getMatchDao().getAllMatchesByOrder()
        list.forEach { match ->
            match.imgUrl = ImageProvider.parseCoverUrl(match.imgUrl)?:""
            val data = CareerCategoryMatch(match, 0, "", "")
            // 其他耗时操作都交给timewaste
            totalMatches.add(data)
        }
        return totalMatches
    }

    private fun loadDetails(data: CareerCategoryMatch) {
        val items = getDatabase().getMatchDao().getRecordMatchItems(mRecordId, data.match.id)
        var win = 0
        var lose = 0
        val periodMap = mutableMapOf<Long, MutableList<MatchItem>?>()
        items.forEach { item ->
            if (item.winnerId == mRecordId) {
                win ++
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
        data.winLose = "${win}胜${lose}负"
        // times
        data.times = periodMap.keys.size
        // best，只取最好的3个轮次
        val countList = mutableListOf<MatchCount>()
        periodMap.keys.forEach { matchPeriodId ->
            val count = toMatchCount(matchPeriodId, periodMap[matchPeriodId]!!)
            countList.add(count)
        }
        countList.sortByDescending { it.roundWeight }
        val bestMap = mutableMapOf<Int, MutableList<MatchCount>?>()
        for (i in countList.indices) {
            var bm = bestMap[countList[i].roundWeight]
            if (bm == null) {
                if (bestMap.keys.size == 3) {
                    break
                }
                bm = mutableListOf()
                bestMap[countList[i].roundWeight] = bm
            }
            bm.add(countList[i])
        }
        val bestBuffer = StringBuffer()
        bestMap.keys.sortedDescending().forEach {
            bestBuffer.append(toPeriodRound(bestMap[it]!!))
        }
        data.best = bestBuffer.toString()
    }

    private fun handleItem(data: Any) {
        if (data is CareerCategoryMatch)  {
            loadDetails(data)
        }
    }

    private fun toPeriodRound(list: List<MatchCount>): String {
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

    private fun toMatchCount(matchPeriodId: Long, list: List<MatchItem>): MatchCount {
        val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        var maxWeight = -9999
        var maxRoundItem: MatchItem? = null
        list.forEach {
            var weight = MatchConstants.getRoundSortValue(it.round)
            // Win要跟F分开算
            if (it.round == MatchConstants.ROUND_ID_F && it.winnerId == mRecordId) {
                weight ++
            }
            if (weight > maxWeight) {
                maxWeight = weight
                maxRoundItem = it
            }
        }
        val roundShort = MatchConstants.roundResultShort(maxRoundItem!!.round, maxRoundItem!!.winnerId == mRecordId)
        return MatchCount(matchPeriodId, matchPeriod.bean.period, roundShort, maxWeight)
    }

    data class MatchCount(
        var matchPeriodId: Long,
        var period: Int,
        var roundShort: String,
        var roundWeight: Int
    )

    private fun joinedMatch(it: CareerCategoryMatch): Boolean {
        return if (isOnlyJoinedMatches) {
            it.times > 0
        }
        else {
            true
        }
    }

    fun isJoinedMatchChanged(isChecked: Boolean) {
        isOnlyJoinedMatches = isChecked
        when(sortType) {
            SORT_BY_WEEK -> sortByWeek()
            SORT_BY_TIMES -> sortByTimes()
            SORT_BY_LEVEL -> sortByLevel()
        }
    }

    fun sortByWeek() {
        sortType = SORT_BY_WEEK
        val items = totalMatches
            .filter { joinedMatch(it) }
            .sortedBy { it.match.orderInPeriod }
        matchesObserver.value = items
    }

    fun sortByTimes() {
        sortType = SORT_BY_TIMES
        val items = totalMatches
            .filter { joinedMatch(it) }
            .sortedByDescending { it.times }
        matchesObserver.value = items
    }

    fun sortByLevel() {
        sortType = SORT_BY_LEVEL
        launchSingle(
            { groupByLevel() },
            withLoading = false
        ) {
            matchesObserver.value = it
        }
    }

    private fun groupByLevel(): List<Any> {
        val result = mutableListOf<Any>()
        val levelMap = mutableMapOf<Int, MutableList<CareerCategoryMatch>?>()
        totalMatches
            .filter { item -> joinedMatch(item) }
            .forEach { item ->
                var data = levelMap[item.match.level]
                if (data == null) {
                    data = mutableListOf()
                    levelMap[item.match.level] = data
                }
                data.add(item)
            }
        levelMap.keys.sorted().forEach { level ->
            result.add(MatchConstants.MATCH_LEVEL[level])
            // level下按week升序
            result.addAll(levelMap[level]!!.sortedBy { bean -> bean.match.orderInPeriod })
        }
        return result
    }
}