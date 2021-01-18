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
data class MatchItemWrap (

    @Embedded
    var bean: MatchItem,

    @Relation(parentColumn = "id",
        entityColumn = "matchItemId")
    var recordList: List<MatchRecord>

)
data class MatchScoreRecordWrap (

    @Embedded
    var bean: MatchScoreRecord,

    var matchRealId: Long,

    @Relation(parentColumn = "matchItemId",
        entityColumn = "id")
    var matchItem: MatchItem
)
data class MatchRankRecordWrap (

    @Embedded
    var bean: MatchRankRecord,

    @Relation(parentColumn = "recordId",
        entityColumn = "_id")
    var record: Record?

)
data class MatchRankStarWrap (

    @Embedded
    var bean: MatchRankStar,

    @Relation(parentColumn = "starId",
        entityColumn = "_id")
    var star: Star?

)