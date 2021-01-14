package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.DrawCell
import com.king.app.coolg_kt.page.match.DrawData
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.draw.*
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.entity.match.*
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
            var drawData = DrawData(bean.bean)
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
            MatchConstants.MATCH_LEVEL_GS -> GrandSlamPlan(rankRecords, match)
            MatchConstants.MATCH_LEVEL_GM1000 -> GM1000Plan(rankRecords, match)
            MatchConstants.MATCH_LEVEL_GM500 -> GM500Plan(rankRecords, match)
            MatchConstants.MATCH_LEVEL_GM250 -> GM250Plan(rankRecords, match)
            else -> LowPlan(rankRecords, match)
        }
        plan.prepare()
        val mainCells = plan.arrangeMainDraw()
        var roundId = getMatchRound(match.match, MatchConstants.DRAW_MAIN)[0].id
        drawData.mainItems = convertDraws(mainCells, match, roundId, false)

        val qualifyCells = plan.arrangeQualifyDraw()
        roundId = getMatchRound(match.match, MatchConstants.DRAW_QUALIFY)[0].id
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
            var matchItem = MatchItem(0, match.bean.id, roundId, null, isQualify,
                isBye = false, order = i / 2, groupFlag = null)
            if (cell1.matchRecord!!.type == MatchConstants.MATCH_RECORD_BYE || cell2.matchRecord!!.type == MatchConstants.MATCH_RECORD_BYE) {
                matchItem.isBye = true
            }
            var drawItem = DrawItem(matchItem)
            cell1.matchRecord?.let {
                it.order = MatchConstants.MATCH_RECORD_ORDER1
                val record = getDatabase().getRecordDao().getRecordBasic(it.recordId)
                drawItem.matchRecord1 = MatchRecordWrap(it, record)
                drawItem.matchRecord1?.imageUrl = ImageProvider.getRecordRandomPath(record?.name, null)
            }
            cell2.matchRecord?.let {
                it.order = MatchConstants.MATCH_RECORD_ORDER2
                val record = getDatabase().getRecordDao().getRecordBasic(it.recordId)
                drawItem.matchRecord2 = MatchRecordWrap(it, record)
                drawItem.matchRecord2?.imageUrl = ImageProvider.getRecordRandomPath(record?.name, null)
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
                        winner.imageUrl = ImageProvider.getRecordRandomPath(winner.record?.name, null)
                    }
                }
                drawItem.matchRecord1 = getDatabase().getMatchDao().getMatchRecord(item.id, 1)
                drawItem.matchRecord1?.let { bean ->
                    bean.imageUrl = ImageProvider.getRecordRandomPath(bean.record?.name, null)
                }
                drawItem.matchRecord2 = getDatabase().getMatchDao().getMatchRecord(item.id, 2)
                drawItem.matchRecord2?.let { bean ->
                    bean.imageUrl = ImageProvider.getRecordRandomPath(bean.record?.name, null)
                }
                result.add(drawItem)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    fun isDrawExist(matchPeriodId: Long): Boolean {
        return getDatabase().getMatchDao().countMatchItemsByMatchPeriod(matchPeriodId) > 0
    }

    fun saveDraw(data: DrawData):Observable<DrawData> {
        return Observable.create {
            // 先清除matchPeriodId相关
            getDatabase().getMatchDao().deleteMatchItemsByMatchPeriod(data.matchPeriod.id)
            getDatabase().getMatchDao().deleteMatchRecordsByMatchPeriod(data.matchPeriod.id)

            // 先插入MatchItem获取id
            val insertMatchItemList = mutableListOf<MatchItem>()
            data.qualifyItems.forEach { drawItem ->
                insertMatchItemList.add(drawItem.matchItem)
            }
            data.mainItems.forEach { drawItem ->
                insertMatchItemList.add(drawItem.matchItem)
            }
            val ids = getDatabase().getMatchDao().insertMatchItems(insertMatchItemList)
            insertMatchItemList.forEachIndexed { index, matchItem ->
                matchItem.id = ids[index]
            }

            // 再插入MatchRecord
            val insertMatchRecordList = mutableListOf<MatchRecord>()
            data.qualifyItems.forEach { drawItem ->
                drawItem.matchRecord1?.bean?.let { matchRecord ->
                    matchRecord.matchItemId = drawItem.matchItem.id
                    insertMatchRecordList.add(matchRecord)
                }
                drawItem.matchRecord2?.bean?.let { matchRecord ->
                    matchRecord.matchItemId = drawItem.matchItem.id
                    insertMatchRecordList.add(matchRecord)
                }
            }
            data.mainItems.forEach { drawItem ->
                drawItem.matchRecord1?.bean?.let { matchRecord ->
                    matchRecord.matchItemId = drawItem.matchItem.id
                    insertMatchRecordList.add(matchRecord)
                }
                drawItem.matchRecord2?.bean?.let { matchRecord ->
                    matchRecord.matchItemId = drawItem.matchItem.id
                    insertMatchRecordList.add(matchRecord)
                }
            }
            getDatabase().getMatchDao().insertMatchRecords(insertMatchRecordList)
            it.onNext(data)
            it.onComplete()
        }
    }

    /**
     * 一对签位都产生了胜者，才更新下一轮matchItem
     */
    fun checkNextRound(winner1Item: MatchItem, winner1Record: MatchRecord, winner2Item: MatchItem, winner2Record: MatchRecord) {
        if (winner1Record == null || winner2Record == null) {
            return
        }
        var nextRoundOrder = winner1Item.order / 2
        when(winner1Item.round) {
            MatchConstants.ROUND_ID_Q1, MatchConstants.ROUND_ID_Q2,
            MatchConstants.ROUND_ID_128, MatchConstants.ROUND_ID_64, MatchConstants.ROUND_ID_32,
            MatchConstants.ROUND_ID_16, MatchConstants.ROUND_ID_QF, MatchConstants.ROUND_ID_SF -> {
                var nextMatchItem = getDatabase().getMatchDao().getMatchItem(winner1Item.matchId, winner1Item.round + 1, nextRoundOrder)
                if (nextMatchItem == null) {
                    nextMatchItem = MatchItem(0, winner1Item.matchId, winner1Item.round + 1, null, winner1Item.isQualify, false, nextRoundOrder, null)
                    var id = getDatabase().getMatchDao().insertMatchItem(nextMatchItem)

                    var list = mutableListOf<MatchRecord>()
                    var record1 = winner1Record.copy()
                    record1.id = 0
                    record1.matchItemId = id
                    record1.order = 1
                    list.add(record1)
                    var record2 = winner2Record.copy()
                    record2.id = 0
                    record2.matchItemId = id
                    record2.order = 2
                    list.add(record2)
                    getDatabase().getMatchDao().insertMatchRecords(list)
                }
                else {
                    var list = mutableListOf<MatchRecord>()
                    var record1 = getDatabase().getMatchDao().getMatchRecord(nextMatchItem.id, MatchConstants.MATCH_RECORD_ORDER1)
                    record1?.let {
                        it.bean.recordId = winner1Record.recordId
                        it.bean.recordRank = winner1Record.recordRank
                        it.bean.recordSeed = winner1Record.recordSeed
                        it.bean.order = 1
                        list.add(it.bean)
                    }
                    var record2 = getDatabase().getMatchDao().getMatchRecord(nextMatchItem.id, MatchConstants.MATCH_RECORD_ORDER1)
                    record2?.let {
                        it.bean.recordId = winner2Record.recordId
                        it.bean.recordRank = winner2Record.recordRank
                        it.bean.recordSeed = winner2Record.recordSeed
                        it.bean.order = 2
                        list.add(it.bean)
                    }
                    getDatabase().getMatchDao().updateMatchRecords(list)
                }
            }
        }
    }

    fun createScore(match: MatchPeriodWrap): Observable<Boolean> {
        return Observable.create {
            var plan = when(match.match.level) {
                MatchConstants.MATCH_LEVEL_GS -> GrandSlamScorePlan(match)
                MatchConstants.MATCH_LEVEL_FINAL -> FinalScorePlan(match)
                MatchConstants.MATCH_LEVEL_GM1000 -> GM1000ScorePlan(match)
                MatchConstants.MATCH_LEVEL_GM500 -> GM500ScorePlan(match)
                MatchConstants.MATCH_LEVEL_GM250 -> GM250ScorePlan(match)
                else -> LowScorePlan(match)
            }

            val items = getDatabase().getMatchDao().getMatchItems(match.bean.id)
            val recordScores = mutableListOf<MatchScoreRecord>()
            val starScores = mutableListOf<MatchScoreStar>()
            val starScoreMap = mutableMapOf<Long, MatchScoreStar?>()
            items.forEach { item ->
                item.recordList.forEach { matchRecord ->
                    if (matchRecord.type != MatchConstants.MATCH_RECORD_BYE) {
                        var singleScore: Int? = null

                        // win，只有F和RR计分
                        if (matchRecord.recordId == item.bean.winnerId) {
                            if (item.bean.round == MatchConstants.ROUND_ID_F) {
                                singleScore = plan.getRoundScore(item.bean.round, isWinner = true, isQualify = false)
                            }
                            else if (item.bean.round == MatchConstants.ROUND_ID_GROUP) {
                                // TODO 待设计
                            }
                        }
                        // lose，其他所有轮次都计分
                        else {
                            singleScore = plan.getRoundScore(
                                item.bean.round,
                                isWinner = false,
                                isQualify = matchRecord.type == MatchConstants.MATCH_RECORD_QUALIFY
                            )
                        }

                        singleScore?.let { score ->
                            val matchScoreRecord = MatchScoreRecord(0, match.bean.id, item.bean.id, matchRecord.recordId, score)
                            recordScores.add(matchScoreRecord)
                            val stars = getDatabase().getRecordDao().getRecordStars(matchRecord.recordId)
                            stars.forEach { star ->
                                // star可能在一站中有多个record，取最高分
                                var matchScoreStar = starScoreMap[star.bean.starId]
                                if (matchScoreStar == null) {
                                    matchScoreStar = MatchScoreStar(0, match.bean.id, item.bean.id, matchRecord.recordId, star.bean.starId, score)
                                    starScoreMap[star.bean.starId] = matchScoreStar
                                    starScores.add(matchScoreStar)
                                }
                                else {
                                    if (score > matchScoreStar.score) {
                                        matchScoreStar.matchItemId = item.bean.id
                                        matchScoreStar.recordId = matchRecord.recordId
                                        matchScoreStar.score = score
                                    }
                                }
                            }
                        }
                    }
                }
            }
            getDatabase().getMatchDao().insertMatchScoreRecords(recordScores)
            getDatabase().getMatchDao().insertMatchScoreStars(starScores)
            it.onNext(true)
            it.onComplete()
        }
    }
}