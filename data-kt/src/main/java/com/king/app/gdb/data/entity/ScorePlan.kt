package com.king.app.gdb.data.entity

import androidx.room.Entity

/**
 * Desc: score plan
 * @author：Jing Yang
 * @date: 2021/10/20 11:42
 */
@Entity(tableName = "score_plan", primaryKeys = ["matchId", "period"])
data class ScorePlan(
    var matchId: Long,
    var period: Int,
    var plan: String
)