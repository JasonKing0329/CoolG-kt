package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.ChampionItem
import com.king.app.coolg_kt.page.match.DetailBasic
import com.king.app.coolg_kt.page.match.DetailHead
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.coolg_kt.page.match.rank.ScoreModel
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchRankRecord
import com.king.app.gdb.data.relation.RecordWrap
import java.text.DecimalFormat

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 9:29
 */
class DetailViewModel(application: Application): BaseViewModel(application) {

    private var rankRepository = RankRepository()

    private var scoreModel = ScoreModel()

    var toolbarText = ObservableField<String>()

    var headData = ObservableField<DetailHead>()

    var mRecordId = 0L

    var recordWrap: RecordWrap? = null

    var detailBasic = DetailBasic()
    var detailHead = DetailHead()

    var showRankDialog = MutableLiveData<Boolean>()
    var showH2hPage = MutableLiveData<Boolean>()

    var basicPeriodType = 0 // 0:rank period, 1: all time, 2:specific period

    var championPerTotalText = ""
    var championRateText = ""

    fun loadRecord(recordId: Long) {
        mRecordId = recordId
        recordWrap = getDatabase().getRecordDao().getRecord(recordId)
        recordWrap?.let { wrap ->
            detailHead.imageUrl = ImageProvider.getRecordRandomPath(wrap.bean.name, null)
            detailBasic.name = wrap.bean.name?:""
            var rankTxt: String? = null
            wrap.countRecord?.let { countRecord ->
                val curRank = rankRepository.getRecordRankToDraw(wrap.bean.id!!)
                rankTxt = if (MatchConstants.RANK_OUT_OF_SYSTEM == curRank) {
                    "R-${countRecord.rank}"
                }
                else {
                    "$curRank (R-${countRecord.rank})"
                }
            }
            rankTxt?.let { text -> detailHead.rank = text }
            // score
            val scoreCount = scoreModel.countScore(mRecordId, rankRepository.getRankPeriodPack())
            detailHead.score = scoreCount.countBean.score.toString()
            scoreCount.countBean.unavailableScore?.let {
                if (it > 0) {
                    detailHead.scoreNoCount = it.toString()
                }
            }
        }

        headData.set(detailHead)
    }

    fun loadBasic(type: Int, specificPeriod: Int) {
        basicPeriodType = type
        countRank()
        val pack = getPeriodPack(type, specificPeriod)
        countWinLose(pack)
        countPeriodRelated(pack)
    }

    private fun getPeriodPack(type: Int, specificPeriod: Int): PeriodPack {
        return when (type) {
            0 -> {
                rankRepository.getCompletedPeriodPack()
            }
            1 -> {
                rankRepository.getAllTimePeriodPack()
            }
            else -> {
                rankRepository.getSpecificPeriodPack(specificPeriod)
            }
        }
    }

    private fun countRank() {
        val high = getDatabase().getMatchDao().getRecordHighestRank(mRecordId)
        val low = getDatabase().getMatchDao().getRecordLowestRank(mRecordId)
        detailBasic.rankHigh = high.toString()
        detailBasic.rankLow = low.toString()
        getDatabase().getMatchDao().getRecordRankFirstTime(mRecordId, high)?.let {
            detailBasic.rankHighFirst = "P${it.period}-W${it.orderInPeriod}"
        }
        getDatabase().getMatchDao().getRecordRankFirstTime(mRecordId, low)?.let {
            detailBasic.rankLowFirst = "P${it.period}-W${it.orderInPeriod}"
        }
        val highWeeks = getDatabase().getMatchDao().getRecordRankWeeks(mRecordId, high)
        val lowWeeks = getDatabase().getMatchDao().getRecordRankWeeks(mRecordId, low)
        detailBasic.rankHighWeeks = "$highWeeks weeks"
        detailBasic.rankLowWeeks = "$lowWeeks weeks"
    }

    private fun countWinLose(periodPack: PeriodPack) {
        var win = 0
        var lose = 0
        val items = rankRepository.getRecordMatchItemsRange(mRecordId, periodPack)
        items.forEach { item ->
            if (item.winnerId == mRecordId) {
                win ++
            }
            else {
                lose ++
            }
        }
        detailBasic.periodMatches = "${win}胜${lose}负"
    }

    /**
     * 统计period范围内相关信息
     */
    private fun countPeriodRelated(pack: PeriodPack) {
        val scoreList = rankRepository.getRecordPeriodScoresRange(mRecordId, pack)
        detailBasic.matchCount = scoreList.size.toString()

        // 统计best, score not count
        var best = 0
        var bestTimes = 0
        var bestMatches = mutableListOf<Match>()

        scoreList.forEachIndexed { index, wrap ->
            var match = getDatabase().getMatchDao().getMatchPeriod(wrap.matchId).match
            if (wrap.score > best) {
                best = wrap.score
                bestTimes = 1
                bestMatches.clear()
                bestMatches.add(match)
            }
            else if (wrap.score == best) {
                bestTimes ++
                bestMatches.add(match)
            }
        }
        // best information
        if (bestTimes > 1) {
            detailBasic.best = "$best($bestTimes times)"
            val buffer = StringBuffer()
            // 最多显示3个
            bestMatches.take(3).forEachIndexed { index, match ->
                if (index == 0) {
                    buffer.append(match.name)
                }
                else {
                    buffer.append(", ").append(match.name)
                }
            }
            detailBasic.bestSub = if (bestMatches.size > 3) {
                "${buffer}..."
            }
            else {
                buffer.toString()
            }
        }
        else {
            detailBasic.best = best.toString()
            if (bestMatches.size == 1) {
                detailBasic.bestSub = "${bestMatches[0].name}(${MatchConstants.MATCH_LEVEL[bestMatches[0].level]})"
            }
        }
        // titles
        detailBasic.titles = rankRepository.countRecordTitlesIn(mRecordId, pack).toString()
    }

    fun getPeriodsToSelect(): Array<String> {
        val periods = rankRepository.getAllPeriods()
        val list = mutableListOf<String>()
        periods.forEach { list.add(it.toString()) }
        return list.toTypedArray()
    }

    fun getChampionItems(): List<ChampionItem> {
        return getFinalItems(true)
    }

    fun getRunnerUpItems(): List<ChampionItem> {
        return getFinalItems(false)
    }

    private fun getFinalItems(isWin: Boolean): List<ChampionItem> {
        val list = mutableListOf<ChampionItem>()
        val finalList = getDatabase().getMatchDao().getRecordMatchItemsByRound(mRecordId, MatchConstants.ROUND_ID_F)
        var passCount = 0
        finalList.forEach {
            val isPass = if (isWin) it.bean.winnerId == mRecordId
                else it.bean.winnerId != mRecordId
            if (isPass) {
                passCount ++
                val match = getDatabase().getMatchDao().getMatchPeriod(it.bean.matchId)
                val item = ChampionItem(mRecordId, match.bean.id!!)
                item.date = "P${match.bean.period}-W${match.bean.orderInPeriod}"
                item.levelId = match.match.level
                item.level = MatchConstants.MATCH_LEVEL[match.match.level]
                item.name = match.match.name
                it.recordList.forEach { record ->
                    if (record.recordId != mRecordId) {
                        var seed = ""
                        record.recordSeed?.let { s ->
                            if (s > 0) {
                                seed = "[$s]/"
                            }
                        }
                        val recordBean = getDatabase().getRecordDao().getRecordBasic(record.recordId)
                        val head = if (isWin) "d. " else "lose. "
                        item.opponent = "$head$seed${record.recordRank} ${recordBean?.name}"
                    }
                }
                list.add(item)
            }
        }
        // index是倒序，开始设置
        list.forEachIndexed { index, championItem ->
            championItem.index = (list.size - index).toString()
        }
        championPerTotalText = "$passCount/${finalList.size}"
        val rate = passCount.toFloat() / finalList.size
        val format = DecimalFormat("#.#%")
        championRateText = format.format(rate)
        return list
    }

    fun loadFinalRanks(): List<MatchRankRecord> {
        return rankRepository.getRecordFinalRanks(mRecordId)
    }
}