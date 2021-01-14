package com.king.app.coolg_kt.page.match

import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.entity.match.MatchPeriod
import com.king.app.gdb.data.entity.match.MatchRecord
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 11:30
 */
data class DrawData (
    var matchPeriod: MatchPeriod,
    var mainItems: List<DrawItem> = listOf(),
    var qualifyItems: List<DrawItem> = listOf()
)
data class DrawItem (
    var matchItem: MatchItem,
    var matchRecord1: MatchRecordWrap? = null,
    var matchRecord2: MatchRecordWrap? = null,
    var winner: MatchRecordWrap? = null,
    var isChanged: Boolean = false
)
data class DrawCell (
    var matchRecord: MatchRecord?
)
data class RankItem<T> (
    var bean: T,
    var id: Long,
    var rank: String,
    var change: String,
    var imageUrl: String?,
    var name: String?,
    var score: String,
    var matchCount: String
)

data class PeriodPack (
    var matchPeriod: MatchPeriod? = null,
    var startPeriod: Int = 0,
    var startPIO: Int = 0,
    var endPeriod: Int = 0,
    var endPIO: Int = 0
)