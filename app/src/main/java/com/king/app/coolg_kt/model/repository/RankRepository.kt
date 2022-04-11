package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.extension.printCostTime
import com.king.app.coolg_kt.page.match.HighRankRecord
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.coolg_kt.page.match.rank.ScoreModel
import com.king.app.gdb.data.bean.RankLevelCount
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.match.*
import com.king.app.gdb.data.relation.MatchRankStarWrap
import com.king.app.gdb.data.relation.MatchScoreRecordWrap
import com.king.app.gdb.data.relation.RankItemWrap
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 14:08
 */
class RankRepository: BaseRepository() {

    fun isRecordRankCreated(): Boolean {
        var pack = getRankPeriodPack()
        pack.matchPeriod?.let {
            return getDatabase().getMatchDao().countRecordRankItems(it.period, it.orderInPeriod) > 0
        }
        return false
    }

    fun isLastCompletedRankCreated(): Boolean {
        var pack = getCompletedPeriodPack()
        pack.matchPeriod?.let {
            return getDatabase().getMatchDao().countRecordRankItems(it.period, it.orderInPeriod) > 0
        }
        return false
    }

    fun isStarRankCreated(): Boolean {
        var pack = getRankPeriodPack()
        pack.matchPeriod?.let {
            return getDatabase().getMatchDao().countStarRankItems(it.period, it.orderInPeriod) > 0
        }
        return false
    }

    fun getRankPeriodRecordScores(): List<ScoreCount> {
        var list = listOf<ScoreCount>()
        printCostTime("getRankPeriodRecordScores") {
            list = getRecordScoreList(getRankPeriodPack())
        }
        return list
    }

    fun getRTFRecordScores(): List<ScoreCount> {
        return getRecordScoreList(getRTFPeriodPack())
    }

    fun getRankPeriodStarScores(): Observable<List<ScoreCount>> {
        return Observable.create {
            it.onNext(getStarScoreList(getRankPeriodPack()))
            it.onComplete()
        }
    }

    fun getRTFStarScores(): Observable<List<ScoreCount>> {
        return Observable.create {
            it.onNext(getStarScoreList(getRTFPeriodPack()))
            it.onComplete()
        }
    }

    /**
     * 从match_rank_record表中获取排名、积分、数量
     */
    fun getSpecificPeriodRecordRanks(period: Int, orderInPeriod: Int): List<RankItemWrap> {
        return getDatabase().getMatchDao().getRankItems(period, orderInPeriod)
    }

    /**
     * 从match_rank_star表中获取排名、积分、数量
     */
    fun getSpecificPeriodStarRanks(period: Int, orderInPeriod: Int): Observable<List<MatchRankStarWrap>> {
        return Observable.create {
            var result = getDatabase().getMatchDao().getMatchRankStarsBy(period, orderInPeriod)
            it.onNext(result)
            it.onComplete()
        }
    }

    /**
     * 从match_rank_record表中获取排名、积分、数量
     */
    fun getRankPeriodRecordRanks(): List<RankItemWrap> {
        var result = listOf<RankItemWrap>()
        printCostTime("getRankPeriodRecordRanks") {
            val pack = getRankPeriodPack()
            pack.matchPeriod?.let { matchPeriod ->
                result = getDatabase().getMatchDao().getRankItems(matchPeriod.period, matchPeriod.orderInPeriod)
            }
        }
        return result
    }

    fun getStudioRankPeriodRecordRanks(studioId: Long):List<RankItemWrap> {
        val pack = getRankPeriodPack()
        var result = listOf<RankItemWrap>()
        pack.matchPeriod?.let { matchPeriod ->
            result = getDatabase().getMatchDao().getStudioRankItems(matchPeriod.period, matchPeriod.orderInPeriod, studioId)
        }
        return result
    }

    /**
     * 从match_rank_star表中获取排名、积分、数量
     */
    fun getRankPeriodStarRanks(): Observable<List<MatchRankStarWrap>> {
        return Observable.create {
            val pack = getRankPeriodPack()
            var result = listOf<MatchRankStarWrap>()
            pack.matchPeriod?.let { matchPeriod ->
                result = getDatabase().getMatchDao().getMatchRankStarsBy(matchPeriod.period, matchPeriod.orderInPeriod)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun getRecordScoreList(pack: PeriodPack): List<ScoreCount> {
        val list =  if (pack.startPeriod == pack.endPeriod) {
            getDatabase().getMatchDao().countRecordScoreInPeriod(pack.endPeriod, pack.startPIO, pack.endPIO)
        }
        else {
            getDatabase().getMatchDao().countRecordScoreInPeriod(pack.startPeriod, pack.startPIO, pack.endPeriod, pack.endPIO)
        }
        return defineRecordScore(list, pack)
    }

    /**
     * 根据排名情况重新确定积分
     */
    private fun defineRecordScore(list: List<ScoreCount>, pack: PeriodPack): List<ScoreCount> {
        val scoreModel = ScoreModel()
        list.forEach {
            // topN的需要重新计算积分
            if (scoreModel.isTopOfLastPeriod(it.id)) {
                val bean = scoreModel.countTopScore(it.id, pack)
                it.score = bean.score
                it.unavailableScore = bean.unavailableScore
            }
            // 为提高效率，其他情况只有当matchCount大于MatchConstants.MATCH_COUNT_SCORE才需要重新计算
            else {
                if (it.matchCount > MatchConstants.MATCH_COUNT_SCORE) {
                    val bean = scoreModel.countNormalScore(it.id, pack)
                    it.score = bean.score
                    it.unavailableScore = bean.unavailableScore
                }
            }
        }
        // 重新按score降序排序，score相等的情况按matchCount升序
        return list.sortedWith { o1, o2 ->
            when (val result = compareInt(o2.score, o1.score)) {
                0 -> compareInt(o1.matchCount, o2.matchCount)
                else -> result
            }
        }
    }

    private fun compareInt(left: Int, right: Int): Int {
        val result = left - right
        return when {
            result > 0 -> 1
            result < 0 -> -1
            else -> 0
        }
    }

    private fun getStarScoreList(pack: PeriodPack): List<ScoreCount> {
        return if (pack.startPeriod == pack.endPeriod) {
            getDatabase().getMatchDao().countStarScoreInPeriod(pack.endPeriod, pack.startPIO, pack.endPIO)
        }
        else {
            getDatabase().getMatchDao().countStarScoreInPeriod(pack.startPeriod, pack.startPIO, pack.endPeriod, pack.endPIO)
        }
    }

    fun countRecordRankPeriod() {
        var pack = getRankPeriodPack()
        pack.matchPeriod?.let {
            var countList = getRecordScoreList(pack)

            var rankList = mutableListOf<MatchRankRecord>()
            countList.forEachIndexed { index, scoreCount ->
//                rankList.add(MatchRankRecord(0, it.id, scoreCount.id, index + 1, scoreCount.score, scoreCount.matchCount))
            }
            getDatabase().getMatchDao().insertMatchRankRecords(rankList)
        }
    }

    /**
     * @return 按score降序排列
     */
    fun getPeriodScores(recordId: Long, pack: PeriodPack): List<MatchScoreRecordWrap> {
        return getDatabase().getMatchDao().getRecordScoresInPeriod(recordId, pack.startPeriod, pack.startPIO, pack.endPeriod, pack.endPIO)
    }

    /**
     * @return 按score降序排列
     */
    fun getRecordRankPeriodScores(recordId: Long): List<MatchScoreRecordWrap> {
        var pack = getCompletedPeriodPack()
        return getDatabase().getMatchDao().getRecordScoresInPeriod(recordId, pack.startPeriod, pack.startPIO, pack.endPeriod, pack.endPIO)
    }

    /**
     * @return 按score降序排列
     */
    fun getRecordPeriodScores(recordId: Long, period: Int): List<MatchScoreRecordWrap> {
        return getDatabase().getMatchDao().getRecordScoresInPeriod(recordId, period, 0, period, MatchConstants.MAX_ORDER_IN_PERIOD)
    }

    /**
     * @return 按score降序排列
     */
    fun getRecordPeriodScoresRange(recordId: Long, pack: PeriodPack): List<MatchScoreRecord> {
        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        return getDatabase().getMatchDao().getRecordScoresInPeriodRange(recordId, rangeStart, rangeEnd, circleTotal)
    }

    /**
     * current rank
     */
    fun getRecordCurrentRank(recordId: Long): Int {
        // 查询当前orderInPeriod或上一个（即最近一站）的排名
        getDatabase().getMatchDao().getRecordLastRank(recordId)?.let {
            return it.rank
        }
        return -1
    }

    /**
     * return [0]score [1]scoreNotCount
     */
    fun getRecordCurrentScore(recordId: Long): Array<Int> {
        val list = getRecordRankPeriodScores(recordId)
        var score = 0
        var scoreNotCount = 0
        list.forEachIndexed { index, wrap ->
            val isNotCount = index >= MatchConstants.MATCH_COUNT_SCORE
            if (isNotCount) {
                scoreNotCount += wrap.bean.score
            }
            else {
                score += wrap.bean.score
            }
        }
        return arrayOf(score, scoreNotCount)
    }

    fun getRecordRankToDraw(recordId: Long): Int {
        var rankMatchPeriod = getRankPeriodToDraw()
        rankMatchPeriod?.let { mp ->
            val rankBean = getDatabase().getMatchDao().getRecordRank(recordId, mp.period, mp.orderInPeriod)
            rankBean?.let {
                return it.rank
            }
        }
        return MatchConstants.RANK_OUT_OF_SYSTEM
    }

    fun getRecordMatchItemsRange(recordId: Long, pack: PeriodPack): List<MatchItem> {
        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        return getDatabase().getMatchDao().getRecordMatchItemsRange(recordId, rangeStart, rangeEnd, circleTotal)
    }

    fun getRecordCurRankRangeMatches(recordId: Long): List<Long> {
        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val pack = getRTFPeriodPack()
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        return getDatabase().getMatchDao().getRecordMatchesRange(recordId, rangeStart, rangeEnd, circleTotal)
    }

    /**
     * race to final period下record所参加的level对应次数
     */
    fun getRankLevelCount(): List<RankLevelCount> {
        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val pack = getRTFPeriodPack()
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        return getDatabase().getMatchDao().getRankLevelCount(rangeStart, rangeEnd, circleTotal)
    }

    /**
     * micro不计入title
     */
    fun countRecordTitlesIn(recordId: Long, pack: PeriodPack): Int {
        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        return getDatabase().getMatchDao().countRecordWinIn(recordId, MatchConstants.ROUND_ID_F, rangeStart, rangeEnd, circleTotal)
    }

    fun getMatchSemiItems(matchPeriod: MatchPeriod): List<MatchRecord> {
        var list = mutableListOf<MatchRecord>()
        // champion and runner-up
        var items = getDatabase().getMatchDao().getMatchItemsByRound(matchPeriod.id, MatchConstants.ROUND_ID_F)
        items.firstOrNull()?.let { item ->
            val winnerId = item.bean.winnerId
            if (item.recordList.isNotEmpty()) {
                list.addAll(item.recordList)
                // winner排在第一个
                if (list[0].recordId != winnerId) {
                    list.reverse()
                }
            }
        }
        // semi-final, 找输掉的一方
        items = getDatabase().getMatchDao().getMatchItemsByRound(matchPeriod.id, MatchConstants.ROUND_ID_SF)
        items.forEach { item ->
            val winnerId = item.bean.winnerId
            item.recordList.firstOrNull { it.recordId != winnerId }?.let { list.add(it) }
        }
        return list
    }

    fun getRecordFinalRanks(recordId: Long): List<MatchRankRecord> {
        val list = mutableListOf<MatchRankRecord>()
        var cp = getCompletedPeriodPack()
        val endPeriod = if (cp.endPIO == MatchConstants.MAX_ORDER_IN_PERIOD) {
            cp.endPeriod
        }
        else {
            cp.endPeriod - 1
        }
        val startPeriod = getDatabase().getMatchDao().getRecordStartPeriod(recordId)?:endPeriod
        for (i in startPeriod..endPeriod) {
            val rank:MatchRankRecord = getDatabase().getMatchDao().getRecordRank(recordId, i, MatchConstants.MAX_ORDER_IN_PERIOD)
                ?: MatchRankRecord(0, i, MatchConstants.MAX_ORDER_IN_PERIOD, recordId, 9999, 0, 0)
            list.add(rank)

        }
        return list
    }

    fun getHighestRankGroup(): List<HighRankRecord> {
        val list = mutableListOf<HighRankRecord>()
        // 只取最高排名100以内的，已按最高排名排序
        val groups = getDatabase().getMatchDao().groupRecordsRank(100)
        groups.forEach { item ->
            getDatabase().getRecordDao().getRecordBasic(item.recordId)?.let { record ->
                list.add(
                    // week, score, first, last属于耗时操作，后续加载
                    HighRankRecord(
                        record, item.high, 0, 0, "", "", "", 0, 0
                    )
                )
            }
        }
        return list
    }

    fun getHighRankDetail(item: HighRankRecord) {
        val rankList = getDatabase().getMatchDao().getRecordRanks(item.record.id!!, item.rank)
        var maxScore = 0
        var maxBean: MatchRankRecord? = null
        rankList.forEach { bean ->
            if (bean.score > maxScore) {
                maxScore = bean.score
                maxBean = bean
            }
        }
        item.weeks = rankList.size
        item.highestScore = maxScore
        item.highestScoreTime = "P${maxBean!!.period}-W${maxBean!!.orderInPeriod}"
        item.firstPeriod = rankList.first().period
        item.firstPIO = rankList.first().orderInPeriod
        item.firstTime = "P${item.firstPeriod}-W${item.firstPIO}"
        item.lastTime = "P${rankList.last().period}-W${rankList.last().orderInPeriod}"
    }

    fun getRecordRankInMatch(recordId: Long, matchPeriodId: Long): MatchRecord? {
        return getDatabase().getMatchDao().getMatchRecords(matchPeriodId, recordId).firstOrNull()
    }

    fun deleteMatchPeriod(bean: MatchPeriod) {
        // delete related tables
        getDatabase().runInTransaction {
            // match_item
            getDatabase().getMatchDao().deleteMatchItemsByMatchPeriod(bean.id)
            // match_record
            getDatabase().getMatchDao().deleteMatchRecordsByMatchPeriod(bean.id)
            // match_period
            getDatabase().getMatchDao().deleteMatchPeriod(bean)
        }
    }
}