package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.match.MatchRankRecord
import com.king.app.gdb.data.relation.MatchScoreRecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 14:08
 */
class RankRepository: BaseRepository() {

    fun createRank() {

    }

    fun isRecordRankCreated(): Boolean {
        var pack = getRankPeriodPack()
        pack.matchPeriod?.let {
            return getDatabase().getMatchDao().countRecordRankItems(it.id) > 0
        }
        return false
    }

    fun isStarRankCreated(): Boolean {
        var pack = getRankPeriodPack()
        pack.matchPeriod?.let {
            return getDatabase().getMatchDao().countStarRankItems(it.id) > 0
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
     * 确认当前排名的积分周期
     */
    fun getRankPeriodPack(): PeriodPack {
        var bean = PeriodPack()
        var last = getDatabase().getMatchDao().getLastMatchPeriod()
        last?.let { period ->
            bean.matchPeriod = period
            bean.endPeriod = period.period
            bean.endPIO = period.orderInPeriod
            // 确认起始站有3种情况
            // 当前结束的orderInPeriod等于45或46（46为Final）,计分周期为 1 to orderInPeriod
            // 当前结束的orderInPeriod小于45，计分周期为 last(orderInPeriod + 1) to orderInPeriod
            bean.startPeriod = 0
            bean.startPIO = 0
            if (period.orderInPeriod == 45 || period.orderInPeriod == 46) {
                bean.startPeriod = period.period
                bean.startPIO = 1
            } else {
                bean.startPeriod = period.period - 1
                bean.startPIO = period.orderInPeriod + 1
            }
        }
        return bean
    }

    /**
     * 确认RaceToFinal的积分周期
     */
    fun getRTFPeriodPack(): PeriodPack {
        var bean = PeriodPack()
        var last = getDatabase().getMatchDao().getLastMatchPeriod()
        last?.let { period ->
            bean.matchPeriod = period
            bean.endPeriod = period.period
            bean.endPIO = period.orderInPeriod
            bean.startPeriod = period.period
            bean.startPIO = 1
        }
        return bean
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
                rankList.add(MatchRankRecord(0, it.id, scoreCount.id, index + 1, scoreCount.score, scoreCount.matchCount))
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