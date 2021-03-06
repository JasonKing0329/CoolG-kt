package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "star_category")
data class TopStarCategory (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "NAME")
    var name: String?,
    @ColumnInfo(name = "INDEX")
    var index:Int = 0,
    @ColumnInfo(name = "TYPE")
    var type:Int = 0,
    @ColumnInfo(name = "NUMBER")
    var number:Int = 0
)