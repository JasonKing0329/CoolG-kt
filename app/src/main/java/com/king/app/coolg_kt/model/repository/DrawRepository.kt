package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.draw.GM1000Plan
import com.king.app.coolg_kt.page.match.draw.GM250Plan
import com.king.app.coolg_kt.page.match.draw.GM500Plan
import com.king.app.coolg_kt.page.match.draw.GrandSlamPlan
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchRecord
import com.king.app.gdb.data.relation.MatchPeriodWrap
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 16:35
 */
class DrawRepository: BaseRepository() {

    fun createDraw(bean: MatchPeriodWrap):Observable<List<DrawItem>> {
        return Observable.create {
            // 第一个赛季，种子排位参考CountRecord
            var rankRecords = if (bean.bean.period == 1) {
                createRankByRecord()
            }
            // 参考rank体系
            else {
                createRankSystem()
            }
            createMainDrawByMatch(bean.match, rankRecords)
        }
    }

    private fun createRankByRecord(): List<RankRecord> {
        return getDatabase().getRecordDao().getRankRecords()
    }

    fun createRankSystem(): List<RankRecord> {
        var list = mutableListOf<RankRecord>()

        return list
    }

    private fun createMainDrawByMatch(match: Match, rankRecords: List<RankRecord>) {
        // master final
        if (match.level == 1) {
            createMasterFinalDraw(match, rankRecords)
        }
        else {
            createNormalMainDraw(match, rankRecords)
        }
    }

    private fun createNormalMainDraw(match: Match, rankRecords: List<RankRecord>) {
        var plan = when(match.level) {
            0 -> GrandSlamPlan(rankRecords, match)
            2 -> GM1000Plan(rankRecords, match)
            3 -> GM500Plan(rankRecords, match)
            else -> GM250Plan(rankRecords, match)
        }
        val draws = mutableListOf<MatchRecord?>()
        for (i in 0 until match.draws) {
            draws.add(null)
        }
        plan.arrangeDraw(draws)
    }

    private fun createMasterFinalDraw(match: Match, rankRecords: List<RankRecord>) {

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
                var players = getDatabase().getMatchDao().getMatchRecords(item.id)
                if (players.isNotEmpty()) {
                    drawItem.matchRecord1 = players[0]
                }
                else if (players.size > 1) {
                    drawItem.matchRecord2 = players[1]
                }
                result.add(drawItem)
            }
            it.onNext(result)
            it.onComplete()
        }
    }
}