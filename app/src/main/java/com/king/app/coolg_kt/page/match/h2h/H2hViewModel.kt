package com.king.app.coolg_kt.page.match.h2h

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.extension.log
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.H2hRepository
import com.king.app.coolg_kt.model.repository.MatchRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.*
import com.king.app.coolg_kt.page.match.detail.CareerMatchViewModel
import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.relation.RecordWrap
import kotlin.system.measureTimeMillis

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/17 11:35
 */
class H2hViewModel(application: Application): BaseViewModel(application) {

    val GROUP_ROAD = 0
    val GROUP_H2H = 1

    var h2hRoadWrap = H2hRoadWrap()

    var matchPeriodId: Long = 0
    var faceRoundId: Int = 0

    var h2hObserver = MutableLiveData<List<Any>>()
    private var h2hList = listOf<H2hItem>()
    private var matchRoadList = listOf<H2HRoadRound>()
    private var mLevelId = -1

    val h2hRepository = H2hRepository()
    val rankRepository = RankRepository()
    val matchRepository = MatchRepository()

    var player1: RecordWrap? = null

    var player2: RecordWrap? = null

    var player1Color: Int = 0
    var player2Color: Int = 0

    var indexToReceivePlayer = -1

    private var isExpandRoad = true
    private var isExpandH2h = true

    init {
        val colors = listOf(
            getResource().getColor(R.color.h2h_bg_1),
            getResource().getColor(R.color.h2h_bg_2),
            getResource().getColor(R.color.h2h_bg_3),
            getResource().getColor(R.color.h2h_bg_4),
            getResource().getColor(R.color.h2h_bg_5)
        ).shuffled().take(2)

        h2hRoadWrap.player1WinColor.set(colors[0])
        h2hRoadWrap.player2WinColor.set(colors[1])
    }

    fun loadH2h(id1: Long, id2: Long) {
        /**
         * player细节属于耗时操作，因此这里采用先加载其他不耗时数据(basic, round, h2h)
         * 加载完后立即更新UI，使列表可见。然后再更新耗时操作的player info细节
         * 在更新UI后加载player info细节过程中，使用了ObservableField直接设置value
         * 如果在Main线程中启动协程(launchMain)：
         * player info部分会阻塞UI，最后还是要等info都加载完了，列表才可见
         * 只有在子线程中启动协程，player info部分才不会阻塞列表的提前展示
         */
        launchThread {
            val result = mutableListOf<Any>()
            // head
            measureTimeMillis {
                loadPlayer1(id1)
                loadPlayer2(id2)
                result.add(h2hRoadWrap)
            }.log("head")
            // 没有选完两个player时，只加载head部分
            if (id1 == 0L || id2 == 0L) {
                h2hObserver.postValue(result)
                return@launchThread
            }
            // info list
            val infoList = createInfoList()
            result.addAll(infoList)
            // round
            if (matchPeriodId != 0L) {
                measureTimeMillis {
                    matchRoadList = h2hRepository.getH2hRoadRounds(id1, id2, matchPeriodId, faceRoundId)
                    if (matchRoadList.isNotEmpty()) {
                        result.add(H2HRoadGroup(GROUP_ROAD, " Match Road ", isExpandRoad))
                        if (isExpandRoad) {
                            result.addAll(matchRoadList)
                        }
                    }
                }.log("round")
            }
            // h2h
            measureTimeMillis {
                val h2hs = h2hRepository.getH2hItems(id1, id2)
                if (h2hs.isNotEmpty()) {
                    result.add(H2HRoadGroup(GROUP_H2H, " Head To Head ", isExpandH2h, showH2hFilter = isExpandH2h, infoWrap = h2hRoadWrap))
                    h2hList = filterByLevel(calculateWin(h2hs, id1, id2))
                    if (isExpandH2h) {
                        result.addAll(h2hList)
                    }
                }
            }.log("h2h")
            // 先更新UI
            h2hObserver.postValue(result)
            // info details属于耗时操作，延迟更新
            loadInfoDetails(infoList)
        }
    }

    private fun createInfoList(): List<H2hInfo> {
        val list = mutableListOf(
            H2hInfo(MatchConstants.H2H_INFO_TITLES_YTD),
            H2hInfo(MatchConstants.H2H_INFO_WL_YTD),
            H2hInfo(MatchConstants.H2H_INFO_MATCHES_YTD),
            H2hInfo(MatchConstants.H2H_INFO_TITLES_CAREER),
            H2hInfo(MatchConstants.H2H_INFO_WL_CAREER),
            H2hInfo(MatchConstants.H2H_INFO_DEBUT),
            H2hInfo(MatchConstants.H2H_INFO_RANK_HIGH),
            H2hInfo(MatchConstants.H2H_INFO_SCORE_RANK),
        )
        if (matchPeriodId != 0L) {
            list.add(H2hInfo(MatchConstants.H2H_INFO_TITLES_MATCH, bgColor = getResource().getColor(R.color.bg_h2h_match_info)))
            list.add(H2hInfo(MatchConstants.H2H_INFO_BEST_IN_MATCH, bgColor = getResource().getColor(R.color.bg_h2h_match_info)))
            list.add(H2hInfo(MatchConstants.H2H_INFO_WL_MATCH, bgColor = getResource().getColor(R.color.bg_h2h_match_info)))
        }
        return list
    }

    private fun loadPlayer1(playerId: Long) {
        player1 = getDatabase().getRecordDao().getRecord(playerId)
        player1?.let {
            h2hRoadWrap.player1Name.set(it.bean.name)
            h2hRoadWrap.player1ImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
        }
    }

    private fun loadPlayer2(playerId: Long) {
        player2 = getDatabase().getRecordDao().getRecord(playerId)
        player2?.let {
            h2hRoadWrap.player2Name.set(it.bean.name)
            h2hRoadWrap.player2ImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
        }
    }

    private fun calculateWin(list: List<H2hItem>, player1Id: Long, player2Id: Long): List<H2hItem> {
        var win1 = 0
        var win2 = 0
        list.forEach { item ->
            if (item.matchItem.bean.winnerId == player1Id) {
                win1 ++
            }
            else {
                win2 ++
            }
        }
        h2hRoadWrap.player1Win.set(win1.toString())
        h2hRoadWrap.player2Win.set(win2.toString())
        list.forEach { item ->
            item.bgColor = if (item.matchItem.bean.winnerId == player1Id) {
                h2hRoadWrap.player1WinColor.get()
            }
            else {
                h2hRoadWrap.player2WinColor.get()
            }
        }
        return list
    }

    private fun filterByLevel(list: List<H2hItem>): List<H2hItem> {
        return if (mLevelId == -1) {
            h2hRoadWrap.player1FilterWin.set(h2hRoadWrap.player1Win.get())
            h2hRoadWrap.player2FilterWin.set(h2hRoadWrap.player2Win.get())
            list.forEachIndexed { index, h2hItem -> h2hItem.indexInList = (index + 1).toString() }
            list
        }
        else {
            val result = list.filter { item -> item.levelId == mLevelId }
            var win1 = 0
            var win2 = 0
            result.forEach { item ->
                if (item.winnerId == player1?.bean?.id) {
                    win1 ++
                }
                else if (item.winnerId == player2?.bean?.id) {
                    win2 ++
                }
            }
            h2hRoadWrap.player1FilterWin.set("$win1")
            h2hRoadWrap.player2FilterWin.set("$win2")
            result.forEachIndexed { index, h2hItem -> h2hItem.indexInList = (index + 1).toString() }
            result
        }
    }

    data class InfoPart (
        var rank: ObservableField<String>,
        var ytdTitles: ObservableField<String>,
        var ytdWinLose: ObservableField<String>,
        var ytdMatches: ObservableField<String>,
        var careerTitles: ObservableField<String>,
        var careerWinLose: ObservableField<String>,
        var debut: ObservableField<String>,
        var rankHigh: ObservableField<String>,
        var rankScore: ObservableField<String>,
        var matchTitles: ObservableField<String>? = null,
        var bestInMatch: ObservableField<String>? = null,
        var matchWinLose: ObservableField<String>? = null
    )

    private fun List<H2hInfo>.key(key: String): H2hInfo {
        return firstOrNull { it.key == key }!!
    }

    private fun List<H2hInfo>.keyOrNull(key: String): H2hInfo? {
        return firstOrNull { it.key == key }
    }

    private fun loadInfoDetails(infoList: List<H2hInfo>) {
        player1?.apply {
            loadPlayerInfo(
                this,
                InfoPart(
                    h2hRoadWrap.player1Rank,
                    infoList.key(MatchConstants.H2H_INFO_TITLES_YTD).leftValue,
                    infoList.key(MatchConstants.H2H_INFO_WL_YTD).leftValue,
                    infoList.key(MatchConstants.H2H_INFO_MATCHES_YTD).leftValue,
                    infoList.key(MatchConstants.H2H_INFO_TITLES_CAREER).leftValue,
                    infoList.key(MatchConstants.H2H_INFO_WL_CAREER).leftValue,
                    infoList.key(MatchConstants.H2H_INFO_DEBUT).leftValue,
                    infoList.key(MatchConstants.H2H_INFO_RANK_HIGH).leftValue,
                    infoList.key(MatchConstants.H2H_INFO_SCORE_RANK).leftValue,
                    infoList.keyOrNull(MatchConstants.H2H_INFO_TITLES_MATCH)?.leftValue,
                    infoList.keyOrNull(MatchConstants.H2H_INFO_BEST_IN_MATCH)?.leftValue,
                    infoList.keyOrNull(MatchConstants.H2H_INFO_WL_MATCH)?.leftValue
                )
            )
        }
        player2?.apply {
            loadPlayerInfo(
                this,
                InfoPart(
                    h2hRoadWrap.player1Rank,
                    infoList.key(MatchConstants.H2H_INFO_TITLES_YTD).rightValue,
                    infoList.key(MatchConstants.H2H_INFO_WL_YTD).rightValue,
                    infoList.key(MatchConstants.H2H_INFO_MATCHES_YTD).rightValue,
                    infoList.key(MatchConstants.H2H_INFO_TITLES_CAREER).rightValue,
                    infoList.key(MatchConstants.H2H_INFO_WL_CAREER).rightValue,
                    infoList.key(MatchConstants.H2H_INFO_DEBUT).rightValue,
                    infoList.key(MatchConstants.H2H_INFO_RANK_HIGH).rightValue,
                    infoList.key(MatchConstants.H2H_INFO_SCORE_RANK).rightValue,
                    infoList.keyOrNull(MatchConstants.H2H_INFO_TITLES_MATCH)?.rightValue,
                    infoList.keyOrNull(MatchConstants.H2H_INFO_BEST_IN_MATCH)?.rightValue,
                    infoList.keyOrNull(MatchConstants.H2H_INFO_WL_MATCH)?.rightValue
                )
            )
        }
    }

    private fun loadPlayerInfo(record: RecordWrap, infoPart: InfoPart) {
        // rank
        countRank(record, infoPart)
        // titles
        countTitles(record, infoPart)
        // win lose
        countWinLose(record, infoPart.ytdWinLose, rankRepository.getRTFPeriodPack())
        countWinLose(record, infoPart.careerWinLose, rankRepository.getAllTimePeriodPack())
        // matches
        val matches = rankRepository.getRecordPeriodScoresRange(record.bean.id!!, rankRepository.getRTFPeriodPack()).size
        infoPart.ytdMatches.set(matches.toString())
        // debut
        val debutMatch = getDatabase().getMatchDao().getDebutMatch(record.bean.id!!)
        if (debutMatch == null) {
            infoPart.debut.set("--")
        }
        else {
            val mp = debutMatch!!
            infoPart.debut.set("P${mp.bean.period}-W${mp.bean.orderInPeriod} ${mp.match.name}")
        }
        // match period related
        getDatabase().getMatchDao().getMatchByPeriodId(matchPeriodId)?.id?.apply {
            countMatchRelated(record, infoPart, this)
        }
    }

    private fun countMatchRelated(record: RecordWrap, infoPart: InfoPart, matchId: Long) {
        val counter = RecordMatchCounter(record.bean.id!!, matchId).apply {
            isCountBest = true
            isCountTitles = true
            isCountTitles = true
        }
        val result = matchRepository.countRecordMatchItems(counter)
        infoPart.matchWinLose?.set(result.winLose)
        infoPart.matchTitles?.set(result.titles)
        infoPart.bestInMatch?.set(result.bestResults?.firstOrNull())
    }

    private fun countWinLose(record: RecordWrap, rankInfo: ObservableField<String>, periodPack: PeriodPack) {
        val recordId = record.bean.id!!
        var win = 0
        var lose = 0
        val items = rankRepository.getRecordMatchItemsRange(recordId, periodPack)
        items.forEach { item ->
            if (item.winnerId == recordId) {
                win ++
            }
            else {
                lose ++
            }
        }
        rankInfo.set("${win}/${lose}")
    }

    private fun countTitles(record: RecordWrap, infoPart: InfoPart) {
        val recordId = record.bean.id!!
        val careerTitles = rankRepository.countRecordTitlesIn(recordId, rankRepository.getAllTimePeriodPack())
        infoPart.careerTitles.set(careerTitles.toString())
        val ytdTitles = rankRepository.countRecordTitlesIn(recordId, rankRepository.getRTFPeriodPack())
        infoPart.ytdTitles.set(ytdTitles.toString())
    }

    private fun countRank(record: RecordWrap, infoPart: InfoPart) {
        val recordId = record.bean.id!!
        // rank
        var rank = rankRepository.getRecordCurrentRank(recordId)
        if (rank == -1) {
            infoPart.rank.set("r${MatchConstants.RANK_OUT_OF_SYSTEM}")
        }
        else {
            infoPart.rank.set("r$rank")
        }
        val high = getDatabase().getMatchDao().getRecordHighestRank(recordId)
        val highWeeks = getDatabase().getMatchDao().getRecordRankWeeks(recordId, high)
        getDatabase().getMatchDao().getRecordRankFirstTime(recordId, high)?.let {
            infoPart.rankHigh.set("$high(P${it.period}-W${it.orderInPeriod})($highWeeks weeks)")
        }
        val scoreRank = record.countRecord?.rank
        infoPart.rankScore.set("${record.bean.score}/$scoreRank")
    }

    fun filterByLevel(levelId: Int) {
        mLevelId = levelId
        refreshH2hPart()
    }

    /**
     * 局部更新h2h部分
     */
    private fun refreshH2hPart() {
        launchSingle(
            block = {
                h2hObserver.value?.filterIsInstance<H2HRoadGroup>()?.firstOrNull { it.type == GROUP_H2H }?.let {
                    it.isExpand = isExpandH2h
                    it.showH2hFilter = isExpandH2h
                }
                // 先删除所有子项，再重新根据条件加入
                val all = h2hObserver.value?.toMutableList()
                all?.removeAll { it is H2hItem }
                if (isExpandH2h) {
                    // h2h为末尾内容，可以直接add
                    all?.addAll(filterByLevel(h2hList))
                }
                all
            }
        ) {
            it?.apply { h2hObserver.value = this }
        }
    }

    /**
     * 局部更新road部分
     */
    private fun refreshRoadPart() {
        launchSingle(
            block = {
                h2hObserver.value?.filterIsInstance<H2HRoadGroup>()?.firstOrNull { it.type == GROUP_ROAD }?.let {
                    it.isExpand = isExpandRoad
                }
                val groupIndex = h2hObserver.value?.indexOfFirst { it is H2HRoadGroup && it.type == GROUP_ROAD }
                // 先删除所有子项，再重新根据条件加入
                val all = h2hObserver.value?.toMutableList()
                all?.removeAll { it is H2HRoadRound }
                if (isExpandRoad) {
                    groupIndex?.let { index -> all?.addAll(index + 1, matchRoadList) }
                }
                all
            }
        ) {
            it?.apply { h2hObserver.value = this }
        }
    }

    fun loadReceivePlayer(playerId: Long) {
        if (indexToReceivePlayer == 1) {
            loadH2h(playerId, player2?.bean?.id?:0)
        }
        else {
            loadH2h(player1?.bean?.id?:0, playerId)
        }
    }

    fun toggleGroup(group: H2HRoadGroup) {
        when(group.type) {
            GROUP_ROAD -> {
                isExpandRoad = !group.isExpand
                refreshRoadPart()
            }
            GROUP_H2H -> {
                isExpandH2h = !group.isExpand
                refreshH2hPart()
            }
        }
    }
}