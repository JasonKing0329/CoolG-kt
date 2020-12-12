package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/8/10 8:54
 */
@Entity(tableName = "count_star")
data class CountStar (
    @PrimaryKey
    @ColumnInfo(name = "star_id")
    var starId: Long,
    var rank: Int = 0
)