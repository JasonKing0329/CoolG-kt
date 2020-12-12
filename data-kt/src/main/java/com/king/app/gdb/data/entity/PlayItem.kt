package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/15 9:51
 */
@Entity(tableName = "play_item")
data class PlayItem (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    @ColumnInfo(name = "order_id")
    var orderId: Long = 0,
    @ColumnInfo(name = "record_id")
    var recordId: Long = 0,
    var url: String? = null,
    var index:Int = 0
)