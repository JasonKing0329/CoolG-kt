package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 10:45
 */
@Entity(tableName = "match_score_record")
data class MatchScoreRecord (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var matchId: Long,

    var matchItemId: Long,

    var recordId: Long,

    var score: Int
)