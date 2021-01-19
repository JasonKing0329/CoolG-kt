package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 10:45
 */
@Entity(tableName = "match_item")
data class MatchItem (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var matchId: Long,// matchPeriodId

    var round: Int,

    var winnerId: Long?,

    var isQualify: Boolean,

    var isBye: Boolean,

    var order: Int,

    var groupFlag: Int?// 0:groupA, 1:groupB
)