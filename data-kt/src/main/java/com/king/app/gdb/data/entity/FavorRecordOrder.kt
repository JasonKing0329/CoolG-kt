package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/3/13 16:59
 */
@Entity(tableName = "favor_order_record")
data class FavorRecordOrder (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    var name: String,

    @ColumnInfo(name = "cover_url")
    var coverUrl: String?,
    var number:Int = 0,

    @ColumnInfo(name = "sort_seq")
    var sortSeq:Int = 0,

    @ColumnInfo(name = "create_time")
    var createTime: Date,

    @ColumnInfo(name = "update_time")
    var updateTime: Date,

    @ColumnInfo(name = "parent_id")
    var parentId: Long = 0
)