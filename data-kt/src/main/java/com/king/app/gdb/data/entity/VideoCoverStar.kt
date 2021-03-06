package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/22 17:31
 */
@Entity(tableName = "video_cover_star")
data class VideoCoverStar (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "STAR_ID")
    var starId: Long = 0
)