package com.king.app.coolg_kt.page.match

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.king.app.coolg_kt.BR
import com.king.app.coolg_kt.view.widget.chart.adapter.LineData
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.match.*
import com.king.app.gdb.data.relation.MatchItemWrap
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap
import com.king.app.gdb.data.relation.RecordWrap

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
    var unavailableScore: Int? = null,
    var studioName: String? = "",
    var canSelect: Boolean = true
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
    var matchPeriod: MatchPeriod,
    var matchItem: MatchItem,
    var match: Match
)
data class ScoreTitle (
    var name: String,
    var color: Int
)
class DetailHead: BaseObservable() {

    @get:Bindable
    var score: String = ""
        set(score) {
            field = score
            notifyPropertyChanged(BR.score)
        }

    @get:Bindable
    var scoreNoCount: String = ""
        set(scoreNoCount) {
            field = scoreNoCount
            notifyPropertyChanged(BR.scoreNoCount)
        }

    var rank: String = ""
    var imageUrl: String? = null
}

class DetailBasic: BaseObservable() {

    var name: String = ""

    @get:Bindable
    var titles: String = ""
        set(titles) {
            field = titles
            notifyPropertyChanged(BR.titles)
        }

    @get:Bindable
    var matchCount: String = ""
        set(matchCount) {
            field = matchCount
            notifyPropertyChanged(BR.matchCount)
        }

    @get:Bindable
    var periodMatches: String = ""
        set(periodMatches) {
            field = periodMatches
            notifyPropertyChanged(BR.periodMatches)
        }

    @get:Bindable
    var best: String = ""
        set(best) {
            field = best
            notifyPropertyChanged(BR.best)
        }

    @get:Bindable
    var bestSub: String = ""
        set(bestSub) {
            field = bestSub
            notifyPropertyChanged(BR.bestSub)
        }

    var rankHigh: String = ""
    var rankHighFirst: String = ""
    var rankHighWeeks: String = ""
    var rankLow: String = ""
    var rankLowFirst: String = ""
    var rankLowWeeks: String = ""
}
class ScoreHead: BaseObservable() {

    @get:Bindable
    var selectedType = 0
        set(selectedType) {
            field = selectedType
            notifyPropertyChanged(BR.selectedType)
        }

    @get:Bindable
    var periodSpecificText: String = ""
        set(periodSpecificText) {
            field = periodSpecificText
            notifyPropertyChanged(BR.periodSpecificText)
        }

    @get:Bindable
    var scoreText: String = ""
        set(scoreText) {
            field = scoreText
            notifyPropertyChanged(BR.scoreText)
        }
}

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

data class ChampionItem(
    var recordId: Long,
    var matchPeriodId: Long,
    var index: String = "",
    var level: String = "",
    var levelId: Int = 0,
    var date: String = "",
    var name: String = "",
    var opponent: String = ""
)

data class ChampionLevel(
    var level: String = "",
    var levelId: Int = 0,
    var count: Int = 0
)

data class RoundItem(
    var isTitle: Boolean = false,
    var isPeriod: Boolean = false,
    var text: String = "",
    var matchPeriodId: Long = 0
)

data class ScorePack(
    var countBean: ScoreCount,
    var countList: List<MatchScoreRecord>? = null,
    var replaceList: List<MatchScoreRecord>? = null
)

data class MatchSemiPack(
    var matchPeriodId: Long,
    var period: String,
    var date: String,
    var items: List<MatchSemiItem>
)

data class MatchSemiItem(
    var recordId: Long,
    var rank: String,
    var imageUrl: String?
)