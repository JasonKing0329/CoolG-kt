package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.match.*

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 23:14
 */
data class MatchPeriodWrap (

    @Embedded
    var bean: MatchPeriod,

    @Relation(parentColumn = "matchId",
        entityColumn = "id")
    var match: Match

)
data class MatchRecordWrap (

    @Embedded
    var bean: MatchRecord,

    @Relation(parentColumn = "recordId",
        entityColumn = "_id")
    var record: Record?

) {
    var imageUrl: String? = null
}
data class MatchRecordDetailWrap (

    @Embedded
    var bean: MatchRecord,

    var round: Int,

    var winnerId: Long,

    var period: Int,

    var orderInPeriod: Int
)
data class MatchItemWrap (

    @Embedded
    var bean: MatchItem,

    @Relation(parentColumn = "id",
        entityColumn = "matchItemId")
    var recordList: List<MatchRecord>

)
data class MatchItemPeriodWrap (

    @Embedded
    var bean: MatchItem,

    var period: Int,

    var orderInPeriod: Int

)
data class MatchScoreRecordWrap (

    @Embedded
    var bean: MatchScoreRecord,

    var matchRealId: Long,

    @Relation(parentColumn = "matchItemId",
        entityColumn = "id")
    var matchItem: MatchItem
)
data class RankItemWrap (

    @Embedded
    var bean: MatchRankRecord,

    var studioId: Long? = 0,

    var studioName: String? = "",

    @Relation(parentColumn = "recordId",
        entityColumn = "_id")
    var record: Record?,

    @Relation(parentColumn = "recordId",
        entityColumn = "recordId")
    var details: MatchRankDetail?

) {
    var unAvailableScore: Int? = null
}
data class MatchRankRecordWrap (

    @Embedded
    var bean: MatchRankRecord,

    @Relation(parentColumn = "recordId",
        entityColumn = "_id")
    var record: Record?,

    @Relation(parentColumn = "recordId",
        entityColumn = "recordId")
    var details: MatchRankDetail?

) {
    var unAvailableScore: Int? = null
}
data class MatchRankStarWrap (

    @Embedded
    var bean: MatchRankStar,

    @Relation(parentColumn = "starId",
        entityColumn = "_id")
    var star: Star?

)