package com.king.app.coolg_kt.page.match

import com.king.app.coolg_kt.view.widget.chart.adapter.LineData
import com.king.app.gdb.data.entity.match.*
import com.king.app.gdb.data.relation.MatchItemWrap
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap
import com.king.app.gdb.data.relation.RecordWrap
import java.util.*

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
data class FinalDrawData (
    var matchPeriod: MatchPeriod,
    var head: FinalHead,
    var scoreAList: MutableList<FinalScore>,
    var scoreBList: MutableList<FinalScore>,
    var roundMap: MutableMap<String, MutableList<DrawItem>?>
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
    var rank: Int,
    var change: String,
    var imageUrl: String?,
    var name: String?,
    var score: Int,
    var matchCount: Int,
    var unavailableScore: Int? = null
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
    var isNotCount: Boolean,
    var matchPeriod: MatchPeriod,
    var matchItem: MatchItem,
    var match: Match
)
data class ScoreTitle (
    var name: String,
    var color: Int
)
data class ScoreHead (
    var recordId: Long,
    var score: String = "",
    var scoreNoCount: String = "",
    var rank: String = "",
    var name: String = "",
    var matchCount: String = "",
    var periodMatches: String = "",
    var best: String = "",
    var bestSub: String = "",
    var rankHigh: String = "",
    var rankHighFirst: String = "",
    var rankHighWeeks: String = "",
    var rankLow: String = "",
    var rankLowFirst: String = "",
    var rankLowWeeks: String = "",
    var imageUrl: String? = null
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
    var winner: String,
    var loser: String
)

data class ShowPeriod(
    var period: Int,
    var orderInPeriod: Int
)
data class HomeUrls(
    var matchUrl: String? = null,
    var seasonUrl: String? = null,
    var rankUrl: String? = null,
    var h2hUrl: String? = null,
    var finalUrl: String? = null
)

data class RecordWithRank(
    var record: RecordWrap,
    var rank: Int
)
data class FinalHead(
    var groupAList: List<RecordWithRank>,
    var groupBList: List<RecordWithRank>
)
data class FinalScore(
    var rank: String,
    var record: RecordWrap,
    var recordRank: Int,
    var win: Int = 0,
    var lose: Int = 0,
    var extraValue: Int = 0
)
data class FinalRound(
    var round: String
)

data class FinalListItem(
    var match: MatchPeriodWrap,
    var recordWin: MatchRecordWrap,
    var recordLose: MatchRecordWrap
)
class AxisDegree<T> {
    var text: String? = null
    var isNotDraw = false
    var weight = 0
    var data: T? = null
}
class LineChartData {
    var axisYCount = 0
    var axisYTotalWeight = 0
    var axisYDegreeList: MutableList<AxisDegree<Int>> = mutableListOf()
    var axisXCount = 0
    var axisXTotalWeight = 0
    var axisXDegreeList: MutableList<AxisDegree<MatchRankRecord>> = mutableListOf()
    var lineList: MutableList<LineData> = mutableListOf()

}