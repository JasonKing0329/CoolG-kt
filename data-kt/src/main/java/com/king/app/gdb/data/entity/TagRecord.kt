package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "tag_record")
data class TagRecord (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,

    @ColumnInfo(name = "tag_id")
    var tagId: Long = 0,

    @ColumnInfo(name = "record_id")
    var recordId: Long = 0
)