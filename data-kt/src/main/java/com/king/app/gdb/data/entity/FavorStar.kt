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
 * 创建时间: 2018/3/13 17:06
 */
@Entity(tableName = "favor_star")
data class FavorStar (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "ORDER_ID")
    var orderId: Long = 0,

    @ColumnInfo(name = "STAR_ID")
    var starId: Long = 0,

    @ColumnInfo(name = "CREATE_TIME")
    var createTime: Long?,
    @ColumnInfo(name = "UPDATE_TIME")
    var updateTime: Long?
)