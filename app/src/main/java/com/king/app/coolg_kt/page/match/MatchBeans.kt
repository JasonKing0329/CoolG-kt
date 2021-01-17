package com.king.app.coolg_kt.page.match

import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.entity.match.MatchPeriod
import com.king.app.gdb.data.entity.match.MatchRecord
import com.king.app.gdb.data.relation.MatchItemWrap
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
data class ScoreBean (
    var score: Int,
    var name: String,
    var round: String,
    var isCompleted: Boolean,
    var isChampion: Boolean,
    var period: Int,
    var matchItem: MatchItem,
    var match: Match
)
data class ScoreTitle (
    var name: String,
    var color: Int
)

data class RoadBean (
    var round: String,
    var rank: String,
    var imageUrl: String? = null,
    var seed: String? = null
)
data class H2hItem (
    var bgColor: Int,
    var matchItem: MatchItemWrap,
    var index: String,
    var level: String,
    var matchName: String,
    var round: String,
    var winner: String
)