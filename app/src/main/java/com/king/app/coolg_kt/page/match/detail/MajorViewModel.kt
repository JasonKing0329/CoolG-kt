package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.MajorRank
import com.king.app.coolg_kt.page.match.MajorRound
import com.king.app.gdb.data.bean.RecordLevelResult

class MajorViewModel(application: Application): BaseViewModel(application) {

    val FIRST_TOP_100 = "first_top100"
    val FIRST_TOP_50 = "first_top50"
    val FIRST_TOP_20 = "first_top20"
    val FIRST_TOP_10 = "first_top10"
    val FIRST_TOP_1 = "first_top1"
    val NEW_HIGH = "new_high"

    val VIEWTYPE_RANK = 0
    val VIEWTYPE_ROUND = 0

    var list = mutableListOf<MajorItem>()
    var majorMap = mutableMapOf<String, Any>()

    val majorItems = MutableLiveData<List<Any>>()

    var repository = RankRepository()

    var recordId: Long = 0

    data class MajorItem(
        var viewType: Int,
        var period: Int,
        var orderInPeriod: Int,
        var date: String,
        var value: Any
    )

    fun loadMajors() {
        launchSingleThread(
            {
                rankPart()
                majorPart()
                list.sortWith(compareBy({it.period}, {it.orderInPeriod}))
                toViewList()
            },
            withLoading = true
        ) {
            majorItems.value = it
        }
    }

    private fun toViewList(): List<Any> {
        val result = mutableListOf<Any>()
        list.mapTo(result) {
            it.value
        }
        return result
    }

    private fun majorPart() {
        getDatabase().getMatchDao().getRecordResult(recordId)?.forEach {
            when(it.level) {
                MatchConstants.MATCH_LEVEL_LOW, MatchConstants.MATCH_LEVEL_GM250, MatchConstants.MATCH_LEVEL_GM500 -> {
                    checkFinal(it)
                }
                MatchConstants.MATCH_LEVEL_GM1000 -> {
                    checkFinal(it)
                    checkSf(it)
                }
                MatchConstants.MATCH_LEVEL_GS -> {
                    checkFinal(it)
                    checkSf(it)
                    checkQf(it)
                }
                MatchConstants.MATCH_LEVEL_FINAL -> {
                    checkFinal(it)
                    checkSf(it)
                    checkGroup(it)
                }
            }
        }
    }

    private fun checkFinal(result: RecordLevelResult) {
        if (result.round == MatchConstants.ROUND_ID_F) {
            var targetKey = ""
            // champion，检查是否已添加过同级别champion
            val keys = if (result.winnerId == recordId) {
                targetKey = "level_${result.level}_champion"
                listOf(targetKey)
            }
            // runner-up, champion比final轮次高，因此检查是否已添加过同级别champion->runner-up
            else {
                targetKey = "level_${result.level}_final"
                listOf("level_${result.level}_champion", targetKey)
            }
            val winOrLose = if (result.winnerId == recordId) "Champion"
            else "Final"
            val dataValue = "First ${MatchConstants.MATCH_LEVEL[result.level]} $winOrLose"
            checkMajorMap(keys, targetKey, dataValue, result)
        }
    }

    private fun checkSf(result: RecordLevelResult) {
        if (result.round == MatchConstants.ROUND_ID_SF) {
            val targetKey = "level_${result.level}_sf"
            // champion,runner-up比sf轮次高，因此检查是否已添加过同级别champion->runner-up->sf
            val keys = listOf("level_${result.level}_champion", "level_${result.level}_final", targetKey)
            val dataValue = "First ${MatchConstants.MATCH_LEVEL[result.level]} Semi-Final"
            checkMajorMap(keys, targetKey, dataValue, result)
        }
    }

    private fun checkQf(result: RecordLevelResult) {
        if (result.round == MatchConstants.ROUND_ID_QF) {
            val targetKey = "level_${result.level}_qf"
            // champion,runner-up,sf比qf轮次高，因此检查是否已添加过同级别champion->runner-up->sf->qf
            val keys = listOf("level_${result.level}_champion", "level_${result.level}_final", "level_${result.level}_sf", targetKey)
            val dataValue = "First ${MatchConstants.MATCH_LEVEL[result.level]} Quarter-Final"
            checkMajorMap(keys, targetKey, dataValue, result)
        }
    }

    private fun checkGroup(result: RecordLevelResult) {
        if (result.round == MatchConstants.ROUND_ID_GROUP) {
            val targetKey = "level_${result.level}_group"
            // champion,runner-up,sf比group轮次高，因此检查是否已添加过同级别champion->runner-up->sf->group
            val keys = listOf("level_${result.level}_champion", "level_${result.level}_final", "level_${result.level}_sf", targetKey)
            val dataValue = "Qualified in Master Cup for the first time"
            checkMajorMap(keys, targetKey, dataValue, result)
        }
    }

    private fun checkMajorMap(keys: List<String>, targetKey: String, major: String, result: RecordLevelResult) {
        for (key in keys) {
            val value = majorMap[key]
            if (value == null) {
                if (key == targetKey) {
                    val dataValue = MajorRound(
                        result.matchId,
                        "P${result.period}-W${result.orderInPeriod}",
                        major,
                        result.name?:""
                    )
                    val data = MajorItem(VIEWTYPE_ROUND, result.period, result.orderInPeriod, "", dataValue)
                    list.add(data)
                    majorMap[key] = data
                    break
                }
            }
            else {
                break
            }
        }
    }

    private fun rankPart() {
        var lastHigh = Int.MAX_VALUE
        getDatabase().getMatchDao().getRecordRanks(recordId).forEach {
            var value = ""
            when {
                it.rank == 1 -> {
                    if (majorMap[FIRST_TOP_1] == null) {
                        majorMap[FIRST_TOP_1] = it.rank
                        value = "Top 1 for the first time"
                    }
                }
                it.rank <= 10 -> {
                    if (majorMap[FIRST_TOP_10] == null) {
                        majorMap[FIRST_TOP_10] = it.rank
                        value = "Top 10 for the first time"
                    }
                }
                it.rank <= 20 -> {
                    if (majorMap[FIRST_TOP_20] == null) {
                        majorMap[FIRST_TOP_20] = it.rank
                        value = "Top 20 for the first time"
                    }
                }
                it.rank <= 50 -> {
                    if (majorMap[FIRST_TOP_50] == null) {
                        majorMap[FIRST_TOP_50] = it.rank
                        value = "Top 50 for the first time"
                    }
                }
                it.rank <= 100 -> {
                    if (majorMap[FIRST_TOP_100] == null) {
                        majorMap[FIRST_TOP_100] = it.rank
                        value = "Top 100 for the first time"
                    }
                }
            }
            if (it.rank < lastHigh) {
                lastHigh = it.rank
                val newHigh = "New highest rank"
                value = if (value.isEmpty()) newHigh else "$value \n $newHigh"
            }
            if (value.isNotEmpty()) {
                val mr = MajorRank(
                    it.period,
                    it.orderInPeriod,
                    "P${it.period}-W${it.orderInPeriod}",
                    value,
                    "No.${it.rank}",
                    "Score(${it.score}), ${it.matchCount} Matches"
                )
                list.add(MajorItem(VIEWTYPE_RANK, it.period, it.orderInPeriod, "", mr))
            }
        }
    }
}