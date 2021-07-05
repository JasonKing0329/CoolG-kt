package com.king.app.gdb.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_class_item")
data class TagClassItem (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var classId: Long,
    var tagId: Long
)