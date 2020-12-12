package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/15 10:40
 */
@Entity(tableName = "play_duration")
data class PlayDuration (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    @ColumnInfo(name = "record_id")
    var recordId: Long = 0,
    var duration:Int = 0,
    var total:Int = 0

)