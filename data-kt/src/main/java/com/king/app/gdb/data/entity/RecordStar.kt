package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/2/9 11:43
 */
@Entity(tableName = "record_star")
data class RecordStar (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,

    @ColumnInfo(name = "record_id")
    var recordId: Long = 0,

    @ColumnInfo(name = "star_id")
    var starId: Long = 0,
    var type:Int = 0,
    var score:Int = 0,

    @ColumnInfo(name = "score_c")
    var scoreC:Int = 0
)