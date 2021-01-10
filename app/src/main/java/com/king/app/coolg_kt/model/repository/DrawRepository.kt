package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.match.Match
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
        var directIn = match.draws - match.byeDraws - match.qualifyDraws - match.wildcardDraws
        var seeds = match.byeDraws
        // gs设32种子，无轮空
        if (match.level == 0) {
            seeds = 32
        }
        // gm1000至少设16种子（也有32种子），但不一定种子全轮空（可能只轮空8个或四个）
        else if (match.level == 2 && match.byeDraws < 16) {
            seeds = 16
        }
        // gm500至少设8种子（也有16种子），但不一定种子全轮空（可能只轮空4个或0个）
        else if (match.level == 3 && match.byeDraws < 8) {
            seeds = 8
        }
        // gm250固定设8种子，但不一定种子全轮空
        else if (match.level == 4 && match.byeDraws < 8) {
            seeds = 8
        }
        var seedRecords = rankRecords.take(seeds)
        var unSeedRecords = rankRecords.takeLast(rankRecords.size - seeds)
        // 非种子直接shuffle
        unSeedRecords = unSeedRecords.shuffled()
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