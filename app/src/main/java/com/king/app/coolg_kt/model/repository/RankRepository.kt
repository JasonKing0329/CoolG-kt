package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.coolg_kt.page.match.rank.ScoreModel
import com.king.app.coolg_kt.utils.TimeCostUtil
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.entity.match.MatchRankRecord
import com.king.app.gdb.data.entity.match.MatchScoreRecord
import com.king.app.gdb.data.relation.MatchRankRecordWrap
import com.king.app.gdb.data.relation.MatchRankStarWrap
import com.king.app.gdb.data.relation.MatchScoreRecordWrap
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

    fun isStarRankCreated(): Boolean {
        var pack = getRankPeriodPack()
        pack.matchPeriod?.let {
            return getDatabase().getMatchDao().countStarRankItems(it.period, it.orderInPeriod) > 0
        }
        return false
    }

    fun getRankPeriodRecordScores(): Observable<List<ScoreCount>> {
        return Observable.create {
            TimeCostUtil.start()
            val list = getRecordScoreList(getRankPeriodPack())
            TimeCostUtil.end("getRankPeriodRecordScores")
            it.onNext(list)
            it.onComplete()
        }
    }

    fun getRTFRecordScores(): Observable<List<ScoreCount>> {
        return Observable.create {
            it.onNext(getRecordScoreList(getRTFPeriodPack()))
            it.onComplete()
        }
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
    fun getSpecificPeriodRecordRanks(period: Int, orderInPeriod: Int): Observable<List<MatchRankRecordWrap>> {
        return Observable.create {
            it.onNext(specificPeriodRecordRanks(period, orderInPeriod))
            it.onComplete()
        }
    }

    fun specificPeriodRecordRanks(period: Int, orderInPeriod: Int): List<MatchRankRecordWrap> {
        return getDatabase().getMatchDao().getMatchRankRecordsBy(period, orderInPeriod)
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
    fun getRankPeriodRecordRanks(): Observable<List<MatchRankRecordWrap>> {
        return Observable.create {
            TimeCostUtil.start()
            val pack = getRankPeriodPack()
            var result = listOf<MatchRankRecordWrap>()
            pack.matchPeriod?.let { matchPeriod ->
                result = getDatabase().getMatchDao().getMatchRankRecordsBy(matchPeriod.period, matchPeriod.orderInPeriod)
            }
            TimeCostUtil.end("getRankPeriodRecordRanks")
            it.onNext(result)
            it.onComplete()
        }
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
        // 重新按score降序排序
        return list.sortedByDescending { it.score }
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

    fun getRecordCurrentRank(recordId: Long): Int {
        var pack = getCompletedPeriodPack()
        pack.matchPeriod?.let {
            val rankItem = getDatabase().getMatchDao().getRecordRank(recordId, it.period, it.orderInPeriod)
            rankItem?.let { item -> return item.rank }
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

    fun getRankPeriodRecordMatchItems(recordId: Long): List<MatchItem> {
        return getRecordMatchItems(recordId, getRankPeriodPack())
    }

    fun getRTFRecordMatchItems(recordId: Long): List<MatchItem> {
        return getRecordMatchItems(recordId, getRTFPeriodPack())
    }

    fun getRecordMatchItems(recordId: Long, pack: PeriodPack): List<MatchItem> {
        return getDatabase().getMatchDao().getRecordMatchItems(recordId, pack.startPeriod, pack.startPIO, pack.endPeriod, pack.endPIO)
    }

    fun getRecordMatchItemsRange(recordId: Long, pack: PeriodPack): List<MatchItem> {
        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        return getDatabase().getMatchDao().getRecordMatchItemsRange(recordId, rangeStart, rangeEnd, circleTotal)
    }

    fun countRecordTitlesIn(recordId: Long, pack: PeriodPack): Int {
        val circleTotal = MatchConstants.MAX_ORDER_IN_PERIOD
        val rangeStart = pack.startPeriod * circleTotal + pack.startPIO
        val rangeEnd = pack.endPeriod * circleTotal + pack.endPIO
        return getDatabase().getMatchDao().countRecordWinIn(recordId, MatchConstants.ROUND_ID_F, rangeStart, rangeEnd, circleTotal)
    }

}