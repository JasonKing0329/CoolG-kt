package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/24 14:06
 */
@Entity(tableName = "temp_high_rank")
data class TempHighRank(
    @PrimaryKey
    var recordId: Long,
    var high: Int
)