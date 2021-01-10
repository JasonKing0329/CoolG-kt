package com.king.app.coolg_kt.model.bean

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