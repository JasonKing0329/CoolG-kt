package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/1/20 16:47
 */
@Entity(tableName = "match_rank_detail")
data class MatchRankDetail (
    @PrimaryKey(autoGenerate = true)
    var recordId: Long,
    var studioId: Long,
    var studioName: String?,
    var gsCount: Int,
    var gm1000Count: Int,
    var gm500Count: Int,
    var gm250Count: Int,
    var lowCount: Int,
    var microCount: Int
)