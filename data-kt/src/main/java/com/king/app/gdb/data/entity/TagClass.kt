package com.king.app.gdb.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_class")
data class TagClass (
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var type: Int,
    var name: String,
    var nameForSort: String
)