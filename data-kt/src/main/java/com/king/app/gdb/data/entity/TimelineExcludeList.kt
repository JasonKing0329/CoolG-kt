package com.king.app.gdb.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/24 14:06
 */
@Entity(tableName = "timeline_star_exclude")
data class TimelineExcludeList(
    @PrimaryKey
    var starId: Long
)