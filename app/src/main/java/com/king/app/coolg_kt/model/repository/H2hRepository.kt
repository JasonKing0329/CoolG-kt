package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.H2hItem
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/17 11:43
 */
class H2hRepository: BaseRepository() {

    fun getH2hItems(record1Id: Long, record2Id: Long): Observable<List<H2hItem>> {
        return Observable.create {
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
            it.onNext(result)
            it.onComplete()
        }
    }
}