package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "star_category")
data class TopStarCategory (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    var name: String? = null,
    var index:Int = 0,
    var type:Int = 0,
    var number:Int = 0
)