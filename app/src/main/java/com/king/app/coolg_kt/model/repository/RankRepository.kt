package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.match.MatchRankRecord
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
            it.onNext(getRecordScoreList(getRankPeriodPack()))
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
            var result = getDatabase().getMatchDao().getMatchRankRecordsBy(period, orderInPeriod)
            it.onNext(result)
            it.onComplete()
        }
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
            val pack = getRankPeriodPack()
            var result = listOf<MatchRankRecordWrap>()
            pack.matchPeriod?.let { matchPeriod ->
                result = getDatabase().getMatchDao().getMatchRankRecordsBy(matchPeriod.period, matchPeriod.orderInPeriod)
            }
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
        return if (pack.startPeriod == pack.endPeriod) {
            getDatabase().getMatchDao().countRecordScoreInPeriod(pack.endPeriod, pack.startPIO, pack.endPIO)
        }
        else {
            getDatabase().getMatchDao().countRecordScoreInPeriod(pack.startPeriod, pack.startPIO, pack.endPeriod, pack.endPIO)
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

    fun getRecordRankPeriodScores(recordId: Long): List<MatchScoreRecordWrap> {
        var pack = getRankPeriodPack()
        return getDatabase().getMatchDao().getRecordScoresInPeriod(recordId, pack.startPeriod, pack.startPIO, pack.endPeriod, pack.endPIO)
    }

    fun getRecordPeriodScores(recordId: Long, period: Int): List<MatchScoreRecordWrap> {
        return getDatabase().getMatchDao().getRecordScoresInPeriod(recordId, period, 0, period, MatchConstants.MAX_ORDER_IN_PERIOD)
    }

    fun getRecordCurrentRank(recordId: Long): Int {
        var pack = getRankPeriodPack()
        pack.matchPeriod?.let {
            val rankItem = getDatabase().getMatchDao().getRecordRank(recordId, it.period, it.orderInPeriod)
            rankItem?.let { item -> return item.rank }
        }
        return -1
    }

}