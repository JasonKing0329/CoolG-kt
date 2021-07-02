package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
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
    var id: Long?,
    @ColumnInfo(name = "NAME")
    var name: String? = null,
    @ColumnInfo(name = "RECORDS")
    var records: Int = 0,
    @ColumnInfo(name = "BETOP")
    var betop: Int = 0,
    @ColumnInfo(name = "BEBOTTOM")
    var bebottom: Int = 0,
    @ColumnInfo(name = "AVERAGE")
    var average:Float = 0f,
    @ColumnInfo(name = "MAX")
    var max: Int = 0,
    @ColumnInfo(name = "MIN")
    var min: Int = 0,
    @ColumnInfo(name = "CAVERAGE")
    var caverage:Float = 0f,
    @ColumnInfo(name = "CMAX")
    var cmax: Int = 0,
    @ColumnInfo(name = "CMIN")
    var cmin: Int = 0,
    @ColumnInfo(name = "FAVOR")
    var favor: Int = 0// 已废弃
)