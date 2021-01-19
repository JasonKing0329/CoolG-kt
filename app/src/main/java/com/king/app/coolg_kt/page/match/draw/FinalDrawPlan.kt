package com.king.app.coolg_kt.page.match.draw

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.*
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.entity.match.MatchRecord
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap
import java.util.*
import kotlin.math.abs

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/19 14:22
 */
/**
 * Final取top10, 5个一组, 前二晋级
 * 可能的连环套局面：22222 42220 33310
 * 由于没有比分的概念，出现连环套时以match_score_player的score与matchCount作为辅助判断依据
 */
class FinalDrawPlan(var list: List<RankRecord>, var match: MatchPeriodWrap) {

    val database = CoolApplication.instance.database!!

    fun arrangeMainDraw(): FinalDrawData {
        val total = 5 * 4 / 2 * 2; // C52 * 2
        val draws = mutableListOf<DrawCell>()
        for (i in 0 until total) {
            draws.add(DrawCell(null))
        }
        val random = Random()
        val top10 = mutableListOf<RecordWithRank>()
        list.take(10).forEach {
            val record = database.getRecordDao().getRecord(it.recordId)
            top10.add(RecordWithRank(record!!, it.rank))
        }
        // 12, 34, 56, 78, 910分别不同组
        val groupA = mutableListOf<RecordWithRank>()
        val groupB = mutableListOf<RecordWithRank>()
        val scoreA = mutableListOf<FinalScore>()
        val scoreB = mutableListOf<FinalScore>()
        groupA.add(top10[0])
        scoreA.add(FinalScore("1", top10[0].record, ""))
        groupB.add(top10[1])
        scoreB.add(FinalScore("1", top10[1].record, ""))
        if (abs(random.nextInt()) % 2 == 0) {
            groupA.add(top10[2])
            scoreA.add(FinalScore("2", top10[2].record, ""))
            groupB.add(top10[3])
            scoreB.add(FinalScore("2", top10[3].record, ""))
        }
        else {
            groupA.add(top10[3])
            scoreA.add(FinalScore("2", top10[3].record, ""))
            groupB.add(top10[2])
            scoreB.add(FinalScore("2", top10[2].record, ""))
        }
        if (abs(random.nextInt()) % 2 == 0) {
            groupA.add(top10[4])
            scoreA.add(FinalScore("3", top10[4].record, ""))
            groupB.add(top10[5])
            scoreB.add(FinalScore("3", top10[5].record, ""))
        }
        else {
            groupA.add(top10[5])
            scoreA.add(FinalScore("3", top10[5].record, ""))
            groupB.add(top10[4])
            scoreB.add(FinalScore("3", top10[4].record, ""))
        }
        if (abs(random.nextInt()) % 2 == 0) {
            groupA.add(top10[6])
            scoreA.add(FinalScore("4", top10[6].record, ""))
            groupB.add(top10[7])
            scoreB.add(FinalScore("4", top10[7].record, ""))
        }
        else {
            groupA.add(top10[7])
            scoreA.add(FinalScore("4", top10[7].record, ""))
            groupB.add(top10[6])
            scoreB.add(FinalScore("4", top10[6].record, ""))
        }
        if (abs(random.nextInt()) % 2 == 0) {
            groupA.add(top10[8])
            scoreA.add(FinalScore("5", top10[8].record, ""))
            groupB.add(top10[9])
            scoreB.add(FinalScore("5", top10[9].record, ""))
        }
        else {
            groupA.add(top10[9])
            scoreA.add(FinalScore("5", top10[9].record, ""))
            groupB.add(top10[8])
            scoreB.add(FinalScore("5", top10[8].record, ""))
        }

        val pairsA = mutableListOf<RecordPair>()
        for (i in 0 until groupA.size - 1) {
            for (j in i + 1 until groupA.size) {
                pairsA.add(RecordPair(groupA[i], groupA[j]))
            }
        }
        val pairsB = mutableListOf<RecordPair>()
        for (i in 0 until groupB.size - 1) {
            for (j in i + 1 until groupB.size) {
                pairsB.add(RecordPair(groupB[i], groupB[j]))
            }
        }
        var head = FinalHead(groupA, groupB)
        var roundMap = mutableMapOf<String, MutableList<DrawItem>?>()
        val firstRound = mutableListOf<DrawItem>()
        roundMap[MatchConstants.roundFull(MatchConstants.ROUND_ID_GROUP)] = firstRound
        addToFirstRound(0, pairsA, firstRound)
        addToFirstRound(0, pairsB, firstRound)
        return FinalDrawData(match.bean, head, scoreA, scoreB, roundMap)
    }

    private fun addToFirstRound(groupFlg: Int, pairs: MutableList<RecordPair>, firstRound: MutableList<DrawItem>) {
        pairs.forEachIndexed { index, recordPair ->
            firstRound.add(
                DrawItem(
                    MatchItem(0, match.bean.id, MatchConstants.ROUND_ID_GROUP, 0, false, false, index + 1, groupFlg),
                    MatchRecordWrap(
                        MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, match.bean.id, 0, recordPair.record1.record.bean.id!!, recordPair.record1.rank, recordPair.record1.rank, 1)
                        , recordPair.record1.record.bean
                    ),
                    MatchRecordWrap(
                        MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, match.bean.id, 0, recordPair.record2.record.bean.id!!, recordPair.record2.rank, recordPair.record2.rank, 2)
                        , recordPair.record2.record.bean
                    ),
                    null, true
                )
            )
        }
    }

    data class RecordPair (
        var record1: RecordWithRank,
        var record2: RecordWithRank
    )
}