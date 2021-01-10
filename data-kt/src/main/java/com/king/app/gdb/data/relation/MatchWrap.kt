package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchPeriod

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