package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/1/20 16:47
 */
@Entity(tableName = "match_rank_star")
data class MatchRankStar (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var period: Int,
    var orderInPeriod: Int,
    var starId: Long,
    var rank: Int,
    var score: Int,
    var matchCount: Int
)