package com.king.app.coolg_kt.page.match.rank

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.coolg_kt.page.match.ScorePack
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.match.MatchScoreRecord
import com.king.app.gdb.data.relation.MatchScoreRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/27 10:14
 */
class ScoreModel {

    val TOP = 30
    val database = CoolApplication.instance.database!!
    var topList = listOf<Long>()

    init {
        val last = database.getMatchDao().getLastMatchPeriod()
        last?.let {
            val lastPeriod = it.period - 1
            topList = database.getMatchDao().getTopRecordRanks(lastPeriod, MatchConstants.MAX_ORDER_IN_PERIOD, TOP)
        }
    }

    fun countScore(recordId: Long, pack: PeriodPack):ScorePack {

        return countScore(recordId, pack, isTopOfLastPeriod(recordId), false)
    }

    fun countScoreWithClassifiedResult(recordId: Long, pack: PeriodPack):ScorePack {

        return countScore(recordId, pack, isTopOfLastPeriod(recordId), true)
    }

    fun countTopScore(recordId: Long, pack: PeriodPack):ScoreCount {

        return countScore(recordId, pack, true, false).countBean
    }

    fun countNormalScore(recordId: Long, pack: PeriodPack):ScoreCount {

        return countScore(recordId, pack, false, false).countBean
    }

    private fun countScore(recordId: Long, pack: PeriodPack, isTopRecord: Boolean, classifyResult: Boolean):ScorePack {

        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        val list = database.getMatchDao().getRecordScoresInPeriodRange(recordId, rangeStart, rangeEnd, circleTotal)
        return if (isTopRecord) {
            defineTopScore(recordId, list, classifyResult)
        }
        else {
            defineNormalScore(recordId, list, classifyResult)
        }
    }

    /**
     * gs, gm1000, master final强制计分，其他取最好6or5站（gm500至少2站）
     */
    private fun defineTopScore(recordId: Long, list: List<MatchScoreRecord>, classifyResult: Boolean): ScorePack {
        val scoreCount = ScoreCount(recordId, 0, 0)
        val replaceList = mutableListOf<MatchScoreRecord>()
        val countList = mutableListOf<MatchScoreRecord>()
        val tempList = mutableListOf<MatchScoreRecord>()
        val temp500List = mutableListOf<MatchScoreRecord>()
        var hasFinal = false
        // 先确认强制计分
        list.forEach {
            val match = database.getMatchDao().getMatchPeriod(it.matchId).match
            when (match.level) {
                MatchConstants.MATCH_LEVEL_GS, MatchConstants.MATCH_LEVEL_GM1000 -> countList.add(it)
                MatchConstants.MATCH_LEVEL_FINAL -> {
                    countList.add(it)
                    hasFinal = true
                }
                MatchConstants.MATCH_LEVEL_GM500 -> temp500List.add(it)
                else -> tempList.add(it)
            }
        }
        // 再确认剩下6or5站最好
        // hasFinal=true则取5站（2站500强制，3站其他），hasFinal=false则取6站（2站500强制，4站其他）
        temp500List.sortByDescending { it.score }
        temp500List.forEachIndexed { index, it ->
            if (index < 2) {
                countList.add(it)
            }
            else {
                tempList.add(it)
            }
        }
        val left = if (hasFinal) 3 else 4
        tempList.sortByDescending { it.score }
        tempList.forEachIndexed { index, it ->
            if (index < left) {
                countList.add(it)
            }
            else {
                replaceList.add(it)
            }
        }
        var score = 0
        var scoreNotCount = 0
        countList.forEach {
            score += it.score
            scoreCount.matchCount ++
        }
        replaceList.forEach { scoreNotCount += it.score }
        scoreCount.score = score
        scoreCount.unavailableScore = scoreNotCount

        return if (classifyResult) {
            ScorePack(scoreCount, countList, replaceList)
        } else {
            ScorePack(scoreCount)
        }
    }

    /**
     * 取MatchConstants.MATCH_COUNT_SCORE站最好
     */
    private fun defineNormalScore(recordId: Long, list: List<MatchScoreRecord>, classifyResult: Boolean):ScorePack {
        val scoreCount = ScoreCount(recordId, 0, 0)
        val replaceList = mutableListOf<MatchScoreRecord>()
        var countList = mutableListOf<MatchScoreRecord>()
        var score = 0
        var scoreNotCount = 0
        if (list.size > MatchConstants.MATCH_COUNT_SCORE) {
            list.sortedByDescending { it.score }
                .forEachIndexed { index, matchScoreRecord ->
                    if (index < MatchConstants.MATCH_COUNT_SCORE) {
                        score += matchScoreRecord.score
                        scoreCount.matchCount ++
                        countList.add(matchScoreRecord)
                    }
                    else {
                        scoreNotCount += matchScoreRecord.score
                        replaceList.add(matchScoreRecord)
                    }
                }
        }
        else {
            countList = list.toMutableList()
            list.forEach { score += it.score }
        }
        scoreCount.score = score
        scoreCount.unavailableScore =  scoreNotCount

        return if (classifyResult) {
            ScorePack(scoreCount, countList, replaceList)
        } else {
            ScorePack(scoreCount)
        }
    }

    fun isTopOfLastPeriod(recordId: Long): Boolean {
        val id = topList.firstOrNull { it == recordId }
        return id != null
    }
}