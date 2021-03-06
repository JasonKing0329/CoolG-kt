package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "tag_star")
data class TagStar (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,

    @ColumnInfo(name = "TAG_ID")
    var tagId: Long = 0,

    @ColumnInfo(name = "STAR_ID")
    var starId: Long = 0
)