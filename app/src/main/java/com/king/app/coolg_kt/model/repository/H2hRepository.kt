package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.H2HRoadRound
import com.king.app.coolg_kt.page.match.H2hItem
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.match.MatchRecord
import com.king.app.gdb.data.relation.MatchItemWrap
import kotlin.math.max

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/17 11:43
 */
class H2hRepository: BaseRepository() {

    fun getH2hItems(record1Id: Long, record2Id: Long): List<H2hItem> {
        val result = mutableListOf<H2hItem>()
        val items = getDatabase().getMatchDao().getH2hItems(record1Id, record2Id)
        items.forEach { wrap ->
            val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(wrap.bean.matchId)
            val index = "P${matchPeriod.bean.period}-W${matchPeriod.bean.orderInPeriod}"
            val round = MatchConstants.roundResultShort(wrap.bean.round, false)
            val level = MatchConstants.MATCH_LEVEL[matchPeriod.match.level]
            val winner = wrap.recordList.first { bean -> bean.recordId == wrap.bean.winnerId }
            val loser = wrap.recordList.first { bean -> bean.recordId != wrap.bean.winnerId }
            val winnerName = getDatabase().getRecordDao().getRecordBasic(winner.recordId)?.name
            val loserName = getDatabase().getRecordDao().getRecordBasic(loser.recordId)?.name
            val win = if (winner.recordSeed != 0) "[${winner.recordSeed}]/(${winner.recordRank}) $winnerName"
            else "(${winner.recordRank}) $winnerName"
            val lose = if (loser.recordSeed != 0) "[${loser.recordSeed}]/(${loser.recordRank}) $loserName"
            else "(${loser.recordRank}) $loserName"
            val item = H2hItem(0, wrap, index, level, matchPeriod.match.name, round, win, lose)
            item.levelId = matchPeriod.match.level
            item.winnerId = wrap.bean.winnerId?:0
            result.add(item)
        }
        return result
    }

    fun getH2hRoadRounds(record1Id: Long, record2Id: Long, matchPeriodId: Long, mfaceRoundId: Int): List<H2HRoadRound> {
        // 小组赛显示当前已完成的其他小组赛
        val faceRoundId = if (mfaceRoundId == MatchConstants.ROUND_ID_GROUP) {
            MatchConstants.ROUND_ID_SF
        }
        else {
            mfaceRoundId
        }
        val faceRoundSortValue = MatchConstants.getRoundSortValue(faceRoundId)
        val items1 = getDatabase().getMatchDao().getMatchItems(matchPeriodId, record1Id)
            .filter { MatchConstants.getRoundSortValue(it.bean.round) < faceRoundSortValue }
            .sortedByDescending { MatchConstants.getRoundSortValue(it.bean.round) }
        val items2 = getDatabase().getMatchDao().getMatchItems(matchPeriodId, record2Id)
            .filter { MatchConstants.getRoundSortValue(it.bean.round) < faceRoundSortValue }
            .sortedByDescending { MatchConstants.getRoundSortValue(it.bean.round) }
        val array = listOf(MatchConstants.ROUND_ID_F, MatchConstants.ROUND_ID_SF, MatchConstants.ROUND_ID_GROUP,
            MatchConstants.ROUND_ID_QF, MatchConstants.ROUND_ID_16, MatchConstants.ROUND_ID_32, MatchConstants.ROUND_ID_64,
            MatchConstants.ROUND_ID_128, MatchConstants.ROUND_ID_Q3, MatchConstants.ROUND_ID_Q2, MatchConstants.ROUND_ID_Q1)
        val result = mutableListOf<H2HRoadRound>()
        val index = array.indexOf(faceRoundId)
        val rounds = array.subList(index + 1, array.size)
        rounds.forEach {
            val list1 = findCompetitors(record1Id, items1, it)
            val list2 = findCompetitors(record2Id, items2, it)
            val maxSize = max(list1.size, list2.size)
            for (i in 0 until maxSize) {
                fillRound(it, list1.getOrNull(i), list2.getOrNull(i))?.let { roundItem -> result.add(roundItem) }
            }
        }
        return result
    }

    private fun fillRound(round: Int, matchRecord1: MatchRecordWithResult?, matchRecord2: MatchRecordWithResult?): H2HRoadRound? {
        if (matchRecord1 != null || matchRecord2 != null) {
            var seed1 = if (matchRecord1?.bean?.recordSeed?:0 > 0) "[${matchRecord1?.bean?.recordSeed}]/" else ""
            var seed2 = if (matchRecord2?.bean?.recordSeed?:0 > 0) "[${matchRecord2?.bean?.recordSeed}]/" else ""
            var isLoseTo1 = if (matchRecord1?.isLoseTo == true) "Lose   " else ""
            var isLoseTo2 = if (matchRecord2?.isLoseTo == true) "Lose   " else ""
            var record1: Record? = null
            var record2: Record? = null
            matchRecord1?.let { record1 = getDatabase().getRecordDao().getRecordBasic(it.bean?.recordId) }
            matchRecord2?.let { record2 = getDatabase().getRecordDao().getRecordBasic(it.bean?.recordId) }
            return H2HRoadRound(
                MatchConstants.roundResultShort(round, false),
                matchRecord1?.bean?.recordId,
                matchRecord2?.bean?.recordId,
                "$isLoseTo1$seed1${matchRecord1?.bean?.recordRank}",
                "$isLoseTo2$seed2${matchRecord2?.bean?.recordRank}",
                ImageProvider.getRecordRandomPath(record1?.name, null),
                ImageProvider.getRecordRandomPath(record2?.name, null)
            )
        }
        return null
    }

    private fun findCompetitors(recordId: Long, list: List<MatchItemWrap>, round: Int): List<MatchRecordWithResult> {
        val result = mutableListOf<MatchRecordWithResult>()
        list.filter { it.bean.round == round }.forEach {
            it.recordList?.firstOrNull { it.recordId != recordId }?.let { record -> result.add(MatchRecordWithResult(
                record,
                it.bean.winnerId == record.recordId
            )) }
        }
        return result
    }

    data class MatchRecordWithResult(
        var bean: MatchRecord,
        var isLoseTo: Boolean
    )
}