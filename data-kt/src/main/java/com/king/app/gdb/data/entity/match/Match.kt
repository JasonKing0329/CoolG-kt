package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 10:45
 */
@Entity(tableName = "match")
data class Match (

    @PrimaryKey(autoGenerate = true)
    var id: Long,

    var period: Int,

    var level: Int,

    var draws: Int,

    var byeDraws: Int,

    var qualifyDraws: Int,

    var date: Long,

    var order: Int,

    var orderInPeriod: Int,

    var name: String,

    var isRankCreated: Boolean,

    var isScoreCreated: Boolean
)