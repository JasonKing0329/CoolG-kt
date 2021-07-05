package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag")
data class Tag (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "NAME")
    var name: String? = null,
    @ColumnInfo(name = "TYPE")
    var type:Int = 0,
    var nameForSort: String
)