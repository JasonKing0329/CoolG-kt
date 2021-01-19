package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 10:45
 */
@Entity(tableName = "match_period")
data class MatchPeriod (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var matchId: Long,

    var date: Long,

    var period: Int,

    var orderInPeriod: Int,

    var isRankCreated: Boolean,

    var isScoreCreated: Boolean,

    var mainWildcard: Int,

    var qualifyWildcard: Int
)