package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.MajorRank
import com.king.app.coolg_kt.page.match.MajorRound
import com.king.app.coolg_kt.page.match.TimeWasteRange
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
    val imageChanged = MutableLiveData<TimeWasteRange>()

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
        basicAndTimeWaste(
            blockBasic = {
                rankPart()
                majorPart()
                list.sortWith(compareBy({it.period}, {it.orderInPeriod}))
                toViewList()
            },
            onCompleteBasic = { majorItems.value = it },
            blockWaste = {_, item ->
                if (item is MajorRound) {
                    item.imageUrl = ImageProvider.parseCoverUrl(getDatabase().getMatchDao().getMatch(item.matchId).imgUrl)
                }
            },
            wasteNotifyCount = 20,
            onWasteRangeChanged = {start, count -> imageChanged.value = TimeWasteRange(start, count) }
        )
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
                    checkFinal(it, true)
                    checkSf(it, true)
                    checkQf(it, true)
                }
                MatchConstants.MATCH_LEVEL_FINAL -> {
                    checkFinal(it)
                    checkSf(it)
                    checkGroup(it)
                }
            }
        }
    }

    private fun getKeyStart(result: RecordLevelResult, matchMatters: Boolean): String {
        var keyBuffer = StringBuffer("level_")
        keyBuffer.append(result.level)
        if (matchMatters) {
            keyBuffer.append("_m").append(result.matchId)
        }
        return keyBuffer.toString()
    }

    private fun checkFinal(result: RecordLevelResult, matchMatters: Boolean = false) {
        if (result.round == MatchConstants.ROUND_ID_F) {
            var keyStart = getKeyStart(result, matchMatters)
            // champion，检查是否已添加过同级别champion
            var targetKey = ""
            val keys = if (result.winnerId == recordId) {
                targetKey = "${keyStart}_champion"
                listOf(targetKey)
            }
            // runner-up, champion比final轮次高，因此检查是否已添加过同级别champion->runner-up
            else {
                targetKey = "${keyStart}_final"
                listOf("${keyStart}_champion", targetKey)
            }
            val winOrLose = if (result.winnerId == recordId) "Champion"
            else "Final"
            checkMajorMap(keys, targetKey, winOrLose, result)
        }
    }

    private fun checkSf(result: RecordLevelResult, matchMatters: Boolean = false) {
        if (result.round == MatchConstants.ROUND_ID_SF) {
            var keyStart = getKeyStart(result, matchMatters)
            var targetKey = "${keyStart}_sf"
            // champion,runner-up比sf轮次高，因此检查是否已添加过同级别champion->runner-up->sf
            val keys = listOf("${keyStart}_champion", "${keyStart}_final", targetKey)
            checkMajorMap(keys, targetKey, "Semi-Final", result)
        }
    }

    private fun checkQf(result: RecordLevelResult, matchMatters: Boolean = false) {
        if (result.round == MatchConstants.ROUND_ID_QF) {
            var keyStart = getKeyStart(result, matchMatters)
            var targetKey = "${keyStart}_qf"
            // champion,runner-up,sf比qf轮次高，因此检查是否已添加过同级别champion->runner-up->sf->qf
            val keys = listOf("${keyStart}_champion", "${keyStart}_final", "${keyStart}_sf", targetKey)
            checkMajorMap(keys, targetKey, "Quarter-Final", result)
        }
    }

    private fun checkGroup(result: RecordLevelResult) {
        if (result.round == MatchConstants.ROUND_ID_GROUP) {
            var keyStart = getKeyStart(result, false)
            var targetKey = "${keyStart}_group"
            // champion,runner-up,sf比group轮次高，因此检查是否已添加过同级别champion->runner-up->sf->group
            val keys = listOf("${keyStart}_champion", "${keyStart}_final", "${keyStart}_sf", targetKey)
            checkMajorMap(keys, targetKey, "Qualified", result)
        }
    }

    private fun checkMajorMap(keys: List<String>, targetKey: String, round: String, result: RecordLevelResult) {
        for (key in keys) {
            val value = majorMap[key]
            if (value == null) {
                if (key == targetKey) {
                    val dataValue = MajorRound(
                        result.matchId,
                        "P${result.period}-W${result.orderInPeriod}",
                        MatchConstants.MATCH_LEVEL[result.level],
                        getRoundColor(result.level),
                        round,
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

    private fun getRoundColor(level: Int): Int {
        return when(level) {
            MatchConstants.MATCH_LEVEL_GS -> getResource().getColor(R.color.match_level_gs)
            MatchConstants.MATCH_LEVEL_FINAL -> getResource().getColor(R.color.match_level_final)
            MatchConstants.MATCH_LEVEL_GM1000 -> getResource().getColor(R.color.match_level_gm1000)
            MatchConstants.MATCH_LEVEL_GM500 -> getResource().getColor(R.color.match_level_gm500)
            MatchConstants.MATCH_LEVEL_GM250 -> getResource().getColor(R.color.match_level_gm250)
            else -> getResource().getColor(R.color.match_level_low)
        }
    }

    private fun rankPart() {
        var lastHigh = Int.MAX_VALUE
        getDatabase().getMatchDao().getRecordRanks(recordId).forEach {
            var major = ""
            var finalKey = ""
            // 这里必须都用if，不能用when，原因：
            // 例如item从100开外直接进入top20，那么它其实同时完成了top100/50/20，需要将map中的top100/50/20都设为已占用，
            // 否则，下一次排名掉到20开外50以内，在when条件下又会判断为第一次进入top50
            if (it.rank <= 100) {
                if (majorMap[FIRST_TOP_100] == null) {
                    finalKey = FIRST_TOP_100
                    majorMap[FIRST_TOP_100] = it.rank
                    major = "Top 100"
                }
            }
            if (it.rank <= 50) {
                if (majorMap[FIRST_TOP_50] == null) {
                    finalKey = FIRST_TOP_50
                    majorMap[FIRST_TOP_50] = it.rank
                    major = "Top 50"
                }
            }
            if (it.rank <= 20) {
                if (majorMap[FIRST_TOP_20] == null) {
                    finalKey = FIRST_TOP_20
                    majorMap[FIRST_TOP_20] = it.rank
                    major = "Top 20"
                }
            }
            if (it.rank <= 10) {
                if (majorMap[FIRST_TOP_10] == null) {
                    finalKey = FIRST_TOP_10
                    majorMap[FIRST_TOP_10] = it.rank
                    major = "Top 10"
                }
            }
            if (it.rank == 1) {
                if (majorMap[FIRST_TOP_1] == null) {
                    finalKey = FIRST_TOP_1
                    majorMap[FIRST_TOP_1] = it.rank
                    major = "Top 1"
                }
            }
            var majorHigh = ""
            if (it.rank < lastHigh) {
                lastHigh = it.rank
                majorHigh = "New Record"
            }
            if (major.isNotEmpty() || majorHigh.isNotEmpty()) {
                val mr = MajorRank(
                    it.period,
                    it.orderInPeriod,
                    "P${it.period}-W${it.orderInPeriod}",
                    major,
                    getRankColor(finalKey),
                    majorHigh,
                    "No.${it.rank}",
                    "Score(${it.score}), ${it.matchCount} Matches"
                )
                list.add(MajorItem(VIEWTYPE_RANK, it.period, it.orderInPeriod, "", mr))
            }
        }
    }

    private fun getRankColor(key: String): Int {
        return when(key) {
            FIRST_TOP_1 -> getResource().getColor(R.color.bg_top1)
            FIRST_TOP_10 -> getResource().getColor(R.color.bg_top10)
            FIRST_TOP_20 -> getResource().getColor(R.color.bg_top20)
            FIRST_TOP_50 -> getResource().getColor(R.color.bg_top50)
            else -> getResource().getColor(R.color.bg_top100)
        }
    }

}