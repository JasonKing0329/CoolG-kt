package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.DrawCell
import com.king.app.coolg_kt.page.match.DrawData
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.draw.GM1000Plan
import com.king.app.coolg_kt.page.match.draw.GM250Plan
import com.king.app.coolg_kt.page.match.draw.GM500Plan
import com.king.app.coolg_kt.page.match.draw.GrandSlamPlan
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 16:35
 */
class DrawRepository: BaseRepository() {

    /**
     * @drayType 0:Main draw, 1:qualify draw
     */
    fun getMatchRound(match: Match, drawType: Int): List<RoundPack> {
        // master final
        return if (match.level == MatchConstants.MATCH_LEVEL_FINAL) {
            MatchConstants.ROUND_ROBIN
        }
        else {
            if (drawType == MatchConstants.DRAW_MAIN) {
                when(match.draws) {
                    128 -> MatchConstants.ROUND_MAIN_DRAW128
                    64 -> MatchConstants.ROUND_MAIN_DRAW64
                    else -> MatchConstants.ROUND_MAIN_DRAW32
                }
            }
            else {
                MatchConstants.ROUND_QUALIFY
            }
        }
    }

    fun createDraw(bean: MatchPeriodWrap):Observable<DrawData> {
        return Observable.create {
            var drawData = DrawData()
            // 第一个赛季，种子排位参考CountRecord
            var rankRecords = if (bean.bean.period == 1) {
                createRankByRecord()
            }
            // 参考rank体系
            else {
                createRankSystem()
            }
            createMainDrawByMatch(bean, rankRecords, drawData)
            it.onNext(drawData)
            it.onComplete()
        }
    }

    private fun createRankByRecord(): List<RankRecord> {
        return getDatabase().getRecordDao().getRankRecords()
    }

    fun createRankSystem(): List<RankRecord> {
        var list = mutableListOf<RankRecord>()

        return list
    }

    private fun createMainDrawByMatch(match: MatchPeriodWrap, rankRecords: List<RankRecord>, drawData: DrawData) {
        // master final
        if (match.match.level == 1) {
            createMasterFinalDraw(match, rankRecords, drawData)
        }
        else {
            createNormalMainDraw(match, rankRecords, drawData)
        }
    }

    private fun createNormalMainDraw(match: MatchPeriodWrap, rankRecords: List<RankRecord>, drawData: DrawData) {
        var plan = when(match.match.level) {
            0 -> GrandSlamPlan(rankRecords, match)
            2 -> GM1000Plan(rankRecords, match)
            3 -> GM500Plan(rankRecords, match)
            else -> GM250Plan(rankRecords, match)
        }
        plan.prepare()
        val mainCells = plan.arrangeMainDraw()
        var roundId = getMatchRound(match.match, 0)[0].id
        drawData.mainItems = convertDraws(mainCells, match, roundId, false)

        val qualifyCells = plan.arrangeQualifyDraw()
        roundId = getMatchRound(match.match, 1)[0].id
        drawData.qualifyItems = convertDraws(qualifyCells, match, roundId, true)
    }

    private fun createMasterFinalDraw(
        match: MatchPeriodWrap,
        rankRecords: List<RankRecord>,
        drawData: DrawData
    ) {

    }

    private fun convertDraws(draws: List<DrawCell>, match: MatchPeriodWrap, roundId: Int, isQualify: Boolean): MutableList<DrawItem> {
        val list = mutableListOf<DrawItem>()
        // 连续两个cell为一个item
        for (i in draws.indices step 2) {
            val cell1 = draws[i]
            val cell2 = draws[i + 1]
            var matchItem = MatchItem(0, match.match.id, roundId, null, isQualify,
                isBye = false, order = i / 2, groupFlag = null)
            if (cell1.type == 1 || cell2.type == 1) {
                matchItem.isBye = true
            }
            var drawItem = DrawItem(matchItem)
            cell1.matchRecord?.let {
                it.order = 1
                val record = getDatabase().getRecordDao().getRecordBasic(it.recordId)
                drawItem.matchRecord1 = MatchRecordWrap(it, record)
                drawItem.matchRecord1?.imageUrl = ImageProvider.getRecordRandomPath(record.name, null)
            }
            cell2.matchRecord?.let {
                it.order = 2
                val record = getDatabase().getRecordDao().getRecordBasic(it.recordId)
                drawItem.matchRecord2 = MatchRecordWrap(it, record)
                drawItem.matchRecord2?.imageUrl = ImageProvider.getRecordRandomPath(record.name, null)
            }
            list.add(drawItem)
        }
        return list
    }

    fun getDrawItems(matchPeriodId: Long, matchId: Long, round: Int): Observable<List<DrawItem>> {
        return Observable.create {
            var result = mutableListOf<DrawItem>()
            var list = getDatabase().getMatchDao().getRoundMatchItems(matchPeriodId, round)
            list.forEach { item ->
                var drawItem = DrawItem(item)
                item.winnerId?.let { winnerId->
                    drawItem.winner = getDatabase().getMatchDao().getMatchRecord(item.id, winnerId)
                    drawItem.winner?.let { winner ->
                        winner.imageUrl = ImageProvider.getRecordRandomPath(winner.record.name, null)
                    }
                }
                drawItem.matchRecord1 = getDatabase().getMatchDao().getMatchRecord(item.id, 1)
                drawItem.matchRecord2 = getDatabase().getMatchDao().getMatchRecord(item.id, 2)
                result.add(drawItem)
            }
            it.onNext(result)
            it.onComplete()
        }
    }
}