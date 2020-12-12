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
@Entity(tableName = "count_record")
data class CountRecord (
    @PrimaryKey
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "RANK")
    var rank: Int = 0
)