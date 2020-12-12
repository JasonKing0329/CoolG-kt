package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/2/9 11:43
 */
@Entity(tableName = "properties")
data class GProperties (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    var key: String? = null,
    var value: String? = null,
    var other: String? = null
)