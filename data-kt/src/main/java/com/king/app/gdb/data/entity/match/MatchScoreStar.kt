package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 10:45
 */
@Entity(tableName = "match_score_star")
data class MatchScoreStar (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var matchId: Long,

    var matchItemId: Long,

    var recordId: Long,

    var starId: Long,

    var score: Int
)