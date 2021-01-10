package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.entity.match.MatchPeriod
import com.king.app.gdb.data.entity.match.MatchRecord

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
    var record: Record

) {
    var imageUrl: String? = null
}