package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/15 9:57
 */
@Entity(tableName = "play_order")
data class PlayOrder (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "NAME")
    var name: String? = null,
    @ColumnInfo(name = "COVER_URL")
    var coverUrl: String? = null
)