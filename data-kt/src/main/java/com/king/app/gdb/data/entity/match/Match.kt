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

    var level: Int,

    var draws: Int,

    var byeDraws: Int,

    var qualifyDraws: Int,

    var wildcardDraws: Int,

    var orderInPeriod: Int,

    var name: String,

    var imgUrl: String
)