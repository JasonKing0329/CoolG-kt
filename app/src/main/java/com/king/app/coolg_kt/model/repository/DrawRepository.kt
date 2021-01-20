package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.*
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

    fun createFinalDraw(bean: MatchPeriodWrap):Observable<FinalDrawData> {
        return Observable.create {
            var rankRecords = createRankSystem()
            it.onNext(createMasterFinalDraw(bean, rankRecords))
            it.onComplete()
        }
    }

    private fun createMasterFinalDraw(match: MatchPeriodWrap, rankRecords: List<RankRecord>): FinalDrawData {
        return FinalDrawPlan(rankRecords, match).arrangeMainDraw()
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
            createNormalMainDraw(bean, rankRecords, drawData)
            it.onNext(drawData)
            it.onComplete()
        }
    }

    private fun createRankByRecord(): List<RankRecord> {
        return getDatabase().getRecordDao().getRankRecords(MatchConstants.RANK_LIMIT_MAX)
    }

    private fun createRankSystem(): List<RankRecord> {
        val matchPeriod = getRankPeriodToDraw()
        matchPeriod?.let {
            return getDatabase().getMatchDao().getRankRecords(MatchConstants.RANK_LIMIT_MAX, it.period, it.orderInPeriod)
        }
        return listOf()
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

    fun saveFinalDraw(data: FinalDrawData): Observable<FinalDrawData> {
        return Observable.create {
            // 先清除matchPeriodId相关
            getDatabase().getMatchDao().deleteMatchItemsByMatchPeriod(data.matchPeriod.id)
            getDatabase().getMatchDao().deleteMatchRecordsByMatchPeriod(data.matchPeriod.id)

            // 先插入MatchItem获取id
            val insertMatchItemList = mutableListOf<MatchItem>()
            val firstRound = data.roundMap[MatchConstants.roundFull(MatchConstants.ROUND_ID_GROUP)]
            firstRound?.forEach { drawItem ->
                insertMatchItemList.add(drawItem.matchItem)
            }
            val ids = getDatabase().getMatchDao().insertMatchItems(insertMatchItemList)
            insertMatchItemList.forEachIndexed { index, matchItem ->
                matchItem.id = ids[index]
            }
            // 再插入MatchRecord
            val insertMatchRecordList = mutableListOf<MatchRecord>()
            firstRound?.forEach { drawItem ->
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

    private fun nextRoundItem(matchPeriodId: Long, roundId: Int, order: Int, isQualify: Boolean, winner1Record: MatchRecord, winner2Record: MatchRecord): MatchItem {
        var nextMatchItem = getDatabase().getMatchDao().getMatchItem(matchPeriodId, roundId, 1)
        if (nextMatchItem == null) {
            nextMatchItem =
                MatchItem(0, matchPeriodId, roundId, null, isQualify, false, order, null)
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
        } else {
            var list = mutableListOf<MatchRecord>()
            var record1 = getDatabase().getMatchDao()
                .getMatchRecord(nextMatchItem.id, MatchConstants.MATCH_RECORD_ORDER1)
            record1?.let {
                it.bean.recordId = winner1Record.recordId
                it.bean.recordRank = winner1Record.recordRank
                it.bean.recordSeed = winner1Record.recordSeed
                it.bean.order = 1
                list.add(it.bean)
            }
            var record2 = getDatabase().getMatchDao()
                .getMatchRecord(nextMatchItem.id, MatchConstants.MATCH_RECORD_ORDER1)
            record2?.let {
                it.bean.recordId = winner2Record.recordId
                it.bean.recordRank = winner2Record.recordRank
                it.bean.recordSeed = winner2Record.recordSeed
                it.bean.order = 2
                list.add(it.bean)
            }
            getDatabase().getMatchDao().updateMatchRecords(list)
        }
        return nextMatchItem
    }

    fun checkFinalSf(matchPeriodId: Long, winner1Record: MatchRecord, winner2Record: MatchRecord) {
        nextRoundItem(matchPeriodId, MatchConstants.ROUND_ID_F, 1, false, winner1Record, winner2Record)
    }

    /**
     * 一对签位都产生了胜者，才更新下一轮matchItem
     */
    fun checkNextRound(winner1Item: MatchItem, winner1Record: MatchRecord, winner2Item: MatchItem, winner2Record: MatchRecord) {
        var nextRoundOrder = winner1Item.order / 2
        when(winner1Item.round) {
            MatchConstants.ROUND_ID_Q1, MatchConstants.ROUND_ID_Q2,
            MatchConstants.ROUND_ID_128, MatchConstants.ROUND_ID_64, MatchConstants.ROUND_ID_32,
            MatchConstants.ROUND_ID_16, MatchConstants.ROUND_ID_QF, MatchConstants.ROUND_ID_SF -> {
                nextRoundItem(winner1Item.matchId, winner1Item.round + 1, nextRoundOrder, winner1Item.isQualify, winner1Record, winner2Record)
            }
        }
    }

    fun checkFinalGroup(matchPeriodId: Long, firstRound: MutableList<DrawItem>, scoreAList: MutableList<FinalScore>, scoreBList: MutableList<FinalScore>) {
        countScore(firstRound, scoreAList)
        countScore(firstRound, scoreBList)
        // 先删除已有的sf, f数据
        getDatabase().getMatchDao().deleteMatchItemsBy(matchPeriodId, MatchConstants.ROUND_ID_SF)
        getDatabase().getMatchDao().deleteMatchItemsBy(matchPeriodId, MatchConstants.ROUND_ID_F)
        // 各组取top2晋级，且A1-B2，A2-B1
        val a1 = MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, matchPeriodId, 0,
            scoreAList[0].record.bean.id!!, scoreAList[0].recordRank, scoreAList[0].recordRank, 1)
        val b2 = MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, matchPeriodId, 0,
            scoreBList[1].record.bean.id!!, scoreBList[1].recordRank, scoreBList[1].recordRank, 2)
        nextRoundItem(matchPeriodId, MatchConstants.ROUND_ID_SF, 1, false, a1, b2)
        val a2 = MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, matchPeriodId, 0,
            scoreAList[1].record.bean.id!!, scoreAList[1].recordRank, scoreAList[1].recordRank, 1)
        val b1 = MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, matchPeriodId, 0,
            scoreBList[0].record.bean.id!!, scoreBList[0].recordRank, scoreBList[0].recordRank, 2)
        nextRoundItem(matchPeriodId, MatchConstants.ROUND_ID_SF, 2, false, a2, b1)
    }

    private fun getSamePeriodMatches(period: Int, orderInPeriod: Int): List<MatchPeriod> {
        return getDatabase().getMatchDao().getMatchPeriods(period, orderInPeriod);
    }

    fun createScore(match: MatchPeriodWrap): Observable<Boolean> {
        return Observable.create {
            getDatabase().getMatchDao().deleteMatchScoreStarsByMatch(match.bean.id)
            getDatabase().getMatchDao().deleteMatchScoreRecordsByMatch(match.bean.id)

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
            val updateStarScores = mutableListOf<MatchScoreStar>()
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
                                // star可能在一站中有多个record，取最高分。还可能在同期赛事中有其他record，还要从数据库里查
                                var matchScoreStar = starScoreMap[star.bean.starId]
                                if (matchScoreStar == null) {
                                    // 先从数据库里查是否已有记录
                                    matchScoreStar = getDatabase().getMatchDao().getMatchScoreStarBy(match.bean.period, match.bean.orderInPeriod, star.bean.starId)
                                    // 没有则创建新纪录
                                    if (matchScoreStar == null) {
                                        matchScoreStar = MatchScoreStar(0, match.bean.id, item.bean.id, matchRecord.recordId, star.bean.starId, score)
                                        starScoreMap[star.bean.starId] = matchScoreStar
                                        starScores.add(matchScoreStar)
                                    }
                                    // 有则判断是否修改记录
                                    else {
                                        starScoreMap[star.bean.starId] = matchScoreStar
                                        updateStarScores.add(matchScoreStar)
                                        if (score > matchScoreStar.score) {
                                            matchScoreStar.matchItemId = item.bean.id
                                            matchScoreStar.recordId = matchRecord.recordId
                                            matchScoreStar.score = score
                                        }
                                    }
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
            getDatabase().getMatchDao().updateMatchScoreStars(starScores)

            // 更新match_period表
            match.bean.isScoreCreated = true
            getDatabase().getMatchDao().updateMatchPeriod(match.bean)

            it.onNext(true)
            it.onComplete()
        }
    }

    fun getFinalDrawData(matchPeriod: MatchPeriodWrap): Observable<FinalDrawData> {
        return Observable.create {
            var groupAList = mutableListOf<RecordWithRank>()
            var groupBList = mutableListOf<RecordWithRank>()
            var head = FinalHead(groupAList, groupBList)
            var scoreAList = mutableListOf<FinalScore>()
            var scoreBList = mutableListOf<FinalScore>()

            var roundMap = mutableMapOf<String, MutableList<DrawItem>?>()
            // round, order已有序
            var matchItems = getDatabase().getMatchDao().getMatchItemsSorted(matchPeriod.bean.id)
            matchItems.forEach { wrap ->
                var round = MatchConstants.roundFull(wrap.bean.round)
                var roundItems = roundMap[round]
                if (roundItems == null) {
                    roundItems = mutableListOf()
                    roundMap[round] = roundItems
                }
                var m1 = getDatabase().getMatchDao().getMatchRecord(wrap.bean.id, 1)
                var m2 = getDatabase().getMatchDao().getMatchRecord(wrap.bean.id, 2)
                var winner: MatchRecordWrap? = null
                if (wrap.bean.winnerId == m1!!.bean.recordId) {
                    winner = m1
                }
                else if (wrap.bean.winnerId == m2!!.bean.recordId) {
                    winner = m2
                }
                roundItems.add(DrawItem(wrap.bean, m1, m2, winner))
            }
            // define group for all records
            val firstRound = roundMap[MatchConstants.roundFull(MatchConstants.ROUND_ID_GROUP)]
            firstRound?.forEach { item ->
                if (item.matchItem.groupFlag == 0) {
                    addToFinalGroup(item, groupAList, scoreAList)
                }
                else {
                    addToFinalGroup(item, groupBList, scoreBList)
                }
            }
            // 统计score
            firstRound?.let { items ->
                countScore(items, scoreAList)
                countScore(items, scoreBList)
            }
            var finalDrawData = FinalDrawData(matchPeriod.bean, head, scoreAList, scoreBList, roundMap)
            it.onNext(finalDrawData)
            it.onComplete()
        }
    }

    private fun countScore(firstRound: MutableList<DrawItem>, scoreList: MutableList<FinalScore>) {
        firstRound?.forEach {
            val fs1 = findFinalScore(it.matchRecord1?.bean?.recordId, scoreList)
            val fs2 = findFinalScore(it.matchRecord2?.bean?.recordId, scoreList)
            if (it.matchRecord1?.bean?.recordId == it.matchItem.winnerId) {
                fs1?.let { finalScore -> finalScore.win++ }
                fs2?.let { finalScore -> finalScore.lose++ }
            }
            else if (it.matchRecord2?.bean?.recordId == it.matchItem.winnerId) {
                fs1?.let { finalScore -> finalScore.lose++ }
                fs2?.let { finalScore -> finalScore.win++ }
            }
        }
        // 设置extraValue
        // TODO 规则还没想好，大体上以record关联的star排名进行参考

        // 第一关键字为win
        scoreList.sortByDescending { it.win }
        // 第二关键字为lose
        scoreList.sortBy { it.lose }
        // 第三关键字为extraValue
        scoreList.sortByDescending { it.extraValue }
        // 第四关键字为rank
        scoreList.sortBy { it.recordRank }

        scoreList.forEachIndexed { index, finalScore -> finalScore.rank = (index + 1).toString() }
    }

    private fun findFinalScore(recordId: Long?, scoreList: MutableList<FinalScore>): FinalScore? {
        for (fs in scoreList) {
            if (fs.record.bean.id == recordId) {
                return fs
            }
        }
        return null
    }

    /**
     * 分组、生成scoreList
     */
    private fun addToFinalGroup(item: DrawItem, groupList: MutableList<RecordWithRank>, scoreList: MutableList<FinalScore>) {
        val record1 = getDatabase().getRecordDao().getRecord(item.matchRecord1!!.bean.recordId)!!
        record1.imageUrl = ImageProvider.getRecordRandomPath(record1.bean.name, null)
        val record2 = getDatabase().getRecordDao().getRecord(item.matchRecord2!!.bean.recordId)!!
        record2.imageUrl = ImageProvider.getRecordRandomPath(record2.bean.name, null)
        var r1 = groupList.firstOrNull { it.record.bean.id == record1.bean.id }
        if (r1 == null) {
            groupList.add(RecordWithRank(record1, item.matchRecord1!!.bean.recordRank!!))
        }
        var r2 = groupList.firstOrNull { it.record.bean.id == record2.bean.id }
        if (r2 == null) {
            groupList.add(RecordWithRank(record2, item.matchRecord2!!.bean.recordRank!!))
        }
        var s1 = scoreList.firstOrNull { it.record.bean.id == record1.bean.id }
        if (s1 == null) {
            scoreList.add(FinalScore("0", record1, item.matchRecord1?.bean?.recordRank?:0))
        }
        var s2 = scoreList.firstOrNull { it.record.bean.id == record2.bean.id }
        if (s2 == null) {
            scoreList.add(FinalScore("0", record2, item.matchRecord2?.bean?.recordRank?:0))
        }
    }

}