package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/1/20 16:47
 */
@Entity(tableName = "match_rank_record")
data class MatchRankRecord (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var matchId: Long,// matchPeriodId
    var recordId: Long,
    var rank: Int,
    var score: Int,
    var matchCount: Int
)