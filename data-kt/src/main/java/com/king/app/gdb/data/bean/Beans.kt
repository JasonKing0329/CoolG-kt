package com.king.app.gdb.data.bean

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/6 21:22
 */
data class RecordScene(
    var name: String?,
    var number: Int
)
data class RankRecord (
    var recordId: Long,
    var rank: Int
) {
    var seed: Int = 0
}
data class ScoreCount(
    var id: Long,
    var score: Int,
    var matchCount: Int
) {
    var unavailableScore: Int? = null
}
data class TitlesCount(
    var winnerId: Long,
    var num: Int
)
data class RecordHighestRank(
    var recordId: Long,
    var high: Int
)
data class LevelChampion(
    var winnerId: Long,
    var period: Int,
    var levelMatchId: Long,
    var matchPeriodId: Long
)
data class DataForRoundCount(
    var recordId: Long,
    var winnerId: Long?,
    var period: Int
)
data class RankLevelCount(
    var recordId: Long,
    var level: Int,
    var count: Int
)
data class MatchSemiRecord(
    var recordId: Long,
    var recordSeed: Int,
    var recordRank: Int,
    var round: Int,
    var winnerId: Long?,
    var period: Int,
    var matchPeriodId: Long,
    var matchPeriodDate: Long
)
data class RecordLevelResult(
    var recordId: Long,
    var round: Int,
    var winnerId: Long?,
    var matchPeriodId: Long,
    var period: Int,
    var orderInPeriod: Int,
    var matchId: Long,
    var name: String?,
    var level: Int
)