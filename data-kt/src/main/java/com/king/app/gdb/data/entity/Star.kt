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
 * 创建时间: 2018/2/9 11:38
 */
@Entity(tableName = "stars")
data class Star (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    var name: String? = null,
    var records: Int = 0,
    var betop: Int = 0,
    var bebottom: Int = 0,
    var average:Float = 0f,
    var max: Int = 0,
    var min: Int = 0,
    var caverage:Float = 0f,
    var cmax: Int = 0,
    var cmin: Int = 0,
    var favor: Int = 0
)