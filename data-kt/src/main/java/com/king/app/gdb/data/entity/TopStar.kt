package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "star_category_details")
data class TopStar (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    @ColumnInfo(name = "category_id")
    var categoryId: Long = 0,
    @ColumnInfo(name = "star_id")
    var starId: Long = 0,
    var level:Int = 0,
    @ColumnInfo(name = "level_index")
    var levelIndex:Int = 0
)