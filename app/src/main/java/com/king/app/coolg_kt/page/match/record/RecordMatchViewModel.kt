package com.king.app.coolg_kt.page.match.record

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RecordMatchPageItem
import com.king.app.coolg_kt.page.match.RecordMatchPageTitle
import com.king.app.coolg_kt.page.match.TimeWasteRange

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/15 14:00
 */
class RecordMatchViewModel(application: Application): BaseViewModel(application) {

    var recordImageUrl = ObservableField<String>()
    var recordRankText = ObservableField<String>()
    var matchImageUrl = ObservableField<String>()
    var matchNameText = ObservableField<String>()
    var matchLevelText = ObservableField<String>()
    var matchWeekText = ObservableField<String>()
    var winLoseText = ObservableField<String>()
    var winLoseQualifyText = ObservableField<String>()
    var bestText = ObservableField<String>()
    var joinTimesText = ObservableField<String>()

    var itemsObserver = MutableLiveData<List<Any>>()
    var imageChanged = MutableLiveData<TimeWasteRange>()

    var mRecordId: Long = 0
    var mMatchId: Long = 0

    var rankRepository = RankRepository()

    fun loadData() {
        basicAndTimeWaste(
            blockBasic = {
                loadRecord()
                getItems()
            },
            onCompleteBasic = { itemsObserver.value = it },
            blockWaste = { _,item -> handleWasteItem(item) },
            wasteNotifyCount = 20,
            onWasteRangeChanged = { start, count -> imageChanged.value = TimeWasteRange(start, count) }
        )
    }

    private fun loadRecord() {
        getDatabase().getRecordDao().getRecord(mRecordId)?.let {
            recordImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
            var rank = rankRepository.getRecordCurrentRank(mRecordId)
            recordRankText.set("Rank $rank")

            var match = getDatabase().getMatchDao().getMatch(mMatchId)
            matchLevelText.set(MatchConstants.MATCH_LEVEL[match.level])
            matchImageUrl.set(ImageProvider.parseCoverUrl(match.imgUrl))
            matchWeekText.set("W${match.orderInPeriod}")
            matchNameText.set(match.name)
        }
    }

    private fun handleWasteItem(item: Any) {
        if (item is RecordMatchPageItem) {
            item.imageUrl = ImageProvider.getRecordRandomPath(item.record?.name, null)
        }
    }

    private fun getItems(): List<Any> {

        var result = mutableListOf<Any>()
        var win = 0
        var lose = 0
        var winQ = 0
        var loseQ = 0
        var lastPeriod = 0
        // 先按period分组，再在period中按round排序
        val titleList = mutableListOf<RecordMatchPageTitle>()
        var itemMap = mutableMapOf<Int, MutableList<RecordMatchPageItem>>()
        var titleBean: RecordMatchPageTitle? = null
        var bestRound = -9999
        var bestPeriod = mutableListOf<Int>()
        var bestIsWin = false
        getDatabase().getMatchDao().getRecordCompetitorsInMatch(mRecordId, mMatchId)
            .forEach { mr ->
                getDatabase().getMatchDao().getMatchItem(mr.matchItemId)?.let { matchItem ->
                    val mp = getDatabase().getMatchDao().getMatchPeriod(matchItem.matchId)
                    val isWinner = matchItem.winnerId == mRecordId
                    // 要考虑未完赛的情况
                    if (matchItem.winnerId == mRecordId) {
                        if (matchItem.round in MatchConstants.ROUND_ID_Q1..MatchConstants.ROUND_ID_Q3) {
                            winQ ++
                        }
                        else {
                            win ++
                        }
                        // winner的情况下只有F才需要对比best
                        if (matchItem.round == MatchConstants.ROUND_ID_F) {
                            // Winner肯定是best
                            if (matchItem.round != bestRound || !bestIsWin) {
                                bestRound = matchItem.round
                                bestPeriod.clear()
                            }
                            bestPeriod.add(mp.bean.period)
                            bestIsWin = true
                        }
                    }
                    else if (matchItem.winnerId == mr.recordId) {
                        if (matchItem.round in MatchConstants.ROUND_ID_Q1..MatchConstants.ROUND_ID_Q3) {
                            loseQ ++
                        }
                        else {
                            lose ++
                        }
                        // 一般lose的情况才是period下最终轮次，对比best
                        val roundVal = MatchConstants.getRoundSortValue(matchItem.round)
                        val bestVal = MatchConstants.getRoundSortValue(bestRound)
                        if (roundVal == bestVal && !bestIsWin) {
                            bestPeriod.add(mp.bean.period)
                        }
                        else if (roundVal > bestVal) {
                            bestRound = matchItem.round
                            bestPeriod.clear()
                            bestPeriod.add(mp.bean.period)
                        }
                    }

                    if (mp.bean.period != lastPeriod) {
                        lastPeriod = mp.bean.period
                        val period = "P${mp.bean.period}"
                        val rankSeed = getDatabase().getMatchDao().getMatchRecord(matchItem.id, mRecordId)?.bean?.let { mmr ->
                            if (mmr.recordSeed != 0) {
                                "(Rank ${mmr.recordRank} / [${mmr.recordSeed}])"
                            }
                            else {
                                "(Rank ${mmr.recordRank})"
                            }
                        }
                        titleBean = RecordMatchPageTitle(period, rankSeed, mp.bean.period)
                        titleList.add(titleBean!!)
                        itemMap[mp.bean.period] = mutableListOf()
                    }
                    val round = MatchConstants.roundResultShort(matchItem.round, isWinner)
                    val rankSeed = if (mr.recordSeed != 0) {
                        "Rank ${mr.recordRank} / [${mr.recordSeed}]"
                    }
                    else {
                        "Rank ${mr.recordRank}"
                    }
                    val record = getDatabase().getRecordDao().getRecordBasic(mr.recordId)
                    val sortValue = MatchConstants.getRoundSortValue(matchItem.round)
                    val isChampion = isWinner && MatchConstants.ROUND_ID_F == matchItem.round
                    // imageUrl属于耗时操作、延迟加载
                    itemMap[mp.bean.period]!!.add(RecordMatchPageItem(round, record, rankSeed, null, sortValue, isWinner, isChampion))
                    if (isChampion) {
                        titleBean!!.isChampion = true
                    }
                }
            }
        titleList.sortByDescending { title -> title.sortValue }
        titleList.forEach { title ->
            result.add(title)
            itemMap[title.sortValue]
                ?.sortedByDescending { item -> item.sortValue }
                ?.forEach { item -> result.add(item) }
        }

        val bestPeriodText = if (bestPeriod.size in 1..10) {
            var buffer = StringBuffer("(")
            for (i in bestPeriod.indices) {
                if (i == 0) {
                    buffer.append("P").append(bestPeriod[i])
                }
                else {
                    buffer.append(", P").append(bestPeriod[i])
                }
            }
            buffer.append(")")
        }
        else {
            ""
        }
        val joinTimes = titleList.size
        if (joinTimes == 1) {
            joinTimesText.set(" $joinTimes time in")
        }
        else {
            joinTimesText.set(" $joinTimes times in")
        }
        bestText.set("Best: ${MatchConstants.roundResultShort(bestRound, bestIsWin)}$bestPeriodText")
        winLoseText.set("${win}胜${lose}负")
        if (winQ > 0 || loseQ > 0) {
            winLoseQualifyText.set("Q(${winQ}胜${loseQ}负)")
        }
        return result
    }
}