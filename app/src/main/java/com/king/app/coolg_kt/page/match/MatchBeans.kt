package com.king.app.coolg_kt.page.match

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.king.app.coolg_kt.BR
import com.king.app.coolg_kt.view.widget.chart.adapter.LineData
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.Record
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
    var studioId: Long = 0,
    var studioName: String? = "",
    var canSelect: Boolean = true,
    var levelMatchCount: String? = null
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
    var isLose: Boolean,
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
    var loser: String,
    var levelId: Int = 0,// 用于过滤
    var winnerId: Long = 0
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
data class TitleCountItem(
    var record: Record,
    var titles: Int,
    var rank: Int,
    var isOnlyOne: Boolean,
    var imageUrl: String? = null,
    var details: String? = null
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
data class RecordH2hItem (
    var record1: Record,
    var recordImg1: String?,
    var record2: Record,
    var recordImg2: String?,
    var record2Rank: String,
    var win: Int,
    var lose: Int,
    var sortValue: Int
)
data class TimeWasteRange (
    var start: Int,
    var count: Int
)
data class StudioMapItem (
    var studio: String,
    var count: Int,
    var winCount: Int
)

data class RecordMatchPageItem(
    var round: String,
    var record: Record?,
    var rankSeed: String,
    var imageUrl: String?,
    var sortValue: Int,
    var isWinner: Boolean = true,
    var isChampion: Boolean = false
)
data class RecordMatchPageTitle(
    var period: String,
    var rankSeed: String?,
    var sortValue: Int,
    var isChampion: Boolean = false
)
data class HighRankRecord(
    var record: Record,
    var rank: Int,
    var weeks: Int,
    var highestScore: Int,
    var highestScoreTime: String,
    var firstTime: String,
    var lastTime: String,
    var firstPeriod: Int,
    var firstPIO: Int
)
data class HighRankTitle (
    var rank: Int,
    var items: MutableList<HighRankItem>
)
data class HighRankItem (
    var bean: HighRankRecord,
    var parent: HighRankTitle,
    var curRank: String? = null,
    var imageUrl: String? = null,
    var details: String? = null
): Comparable<HighRankItem> {
    override fun compareTo(other: HighRankItem): Int {
        return if (this.bean.rank == other.bean.rank) {
            if (this.bean.weeks == other.bean.weeks) {
                // 第三关键字score，降序
                other.bean.highestScore - this.bean.highestScore
            }
            // 第二关键字week，降序
            else {
                other.bean.weeks - this.bean.weeks
            }
        }
        // 第一关键字rank，升序
        else {
            this.bean.rank - other.bean.rank
        }
    }

}
data class WildcardBean (
    var recordId: Long,
    var rank: Int,
    var imageUrl: String? = null
)

data class CareerPeriod (
    var period: Int,
    var detail: String,
    var titles: Int,
    var matches: MutableList<CareerMatch>
){

    var winCount = 0
    var loseCount = 0
    var isExpand = true

}
data class CareerMatch (
    var matchBean: Match,
    var parent: CareerPeriod,
    var matchPeriodId: Long,
    var name: String,
    var week: Int,
    var levelStr: String,
    var levelColor: Int,
    var draws: String,
    var rankSeed: String,
    var result: String,
    var championVisibility: Int,
    var records: MutableList<CareerRecord>
) {
    var isExpand = true
}
data class CareerRecord (
    var parent: CareerMatch,
    var round: String,
    var record: Record?,
    var rankSeed: String,
    var imageUrl: String?,
    var isWinner: Boolean,
    var sortValue: Int
)
data class MatchItemGroup (
    var text: String,
    var level: Int
)
data class CareerCategoryMatch (
    var match: Match,
    var times: Int,
    var best: String,
    var winLose: String
)
data class MilestoneBean (
    var match: Match,
    var matchItem: MatchItem,
    var winIndex: String,
    var rankSeed: String,
    var period: String,
    var cptName: String,
    var cptRankSeed: String,
    var cptImageUrl: String? = null
)

data class WallItem(
    var isTitle: Boolean = false,
    var text: String? = null,
    var recordId: Long? = null,
    var matchPeriodId: Long? = null,
    var imageUrl: String? = null
)

data class MatchCountTitle(
    var times: String
)

data class MatchCountRecord(
    var recordId: Long,
    var imgUrl: String? = null,
    var rankSeed: String = "",
    var winLose: String = "",
    var best: String = "",
    var second: String = "",
    var count: Int = 0
)

data class MatchRoundRecord(
    var recordId: Long,
    var imgUrl: String? = null,
    var rankSeed: String = "",
    var times: String = "",
    var periods: String = "",
    var periodList: MutableList<Int> = mutableListOf()
)

data class ScoreQualifyItem(
    var scoreLose: Int,
    var scoreWin: Int
)

data class ScoreItem(
    var round: Int,
    var scoreLose: Int,
    var scoreWin: Int,
    var qualifyItem: ScoreQualifyItem? = null
)

data class DrawScore(
    var matchId: Long,
    var items: MutableList<ScoreItem>
)
