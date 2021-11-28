package com.king.app.gdb.data.entity.match

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/24 14:06
 */
@Entity(tableName = "match_black_list")
data class MatchBlackList(
    @PrimaryKey
    var recordId: Long
)