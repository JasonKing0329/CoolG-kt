package com.king.app.coolg_kt.model.bean

import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:21
 */
data class StandardMatchDraw (
    var draw: Int,
    var byeDraw: Int,
    var qualifyDraw: Int
)
data class MatchPeriodTitle (
    var period: Int,
    var startDate: String = "",
    var endDate: String = ""
)
data class MatchListItem (
    var match: Match,
    var studioCount: Int,
)