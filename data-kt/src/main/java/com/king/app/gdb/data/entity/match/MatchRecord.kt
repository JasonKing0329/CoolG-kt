package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 10:45
 */
@Entity(tableName = "match_record")
data class MatchRecord (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var type: Int,

    var matchId: Long,

    var matchItemId: Long,

    var recordId: Long,

    var recordRank: Int?,

    var recordSeed: Int?,

    var order: Int?
)