package com.king.app.coolg_kt.page.match.h2h

import android.app.Application
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.H2hRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.H2hItem
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/17 11:35
 */
class H2hViewModel(application: Application): BaseViewModel(application) {

    var player1Name = ObservableField<String>()
    var player2Name = ObservableField<String>()
    var player1Rank = ObservableField<String>()
    var player2Rank = ObservableField<String>()
    var player1Win = ObservableField<String>()
    var player2Win = ObservableField<String>()
    var player1FilterWin = ObservableField<String>()
    var player2FilterWin = ObservableField<String>()
    var player1WinColor = ObservableInt()
    var player2WinColor = ObservableInt()
    var player1ImageUrl = ObservableField<String>()
    var player2ImageUrl = ObservableField<String>()
    var ytdTitles1Text = ObservableField<String>()
    var ytdTitles2Text = ObservableField<String>()
    var ytdWinLose1Text = ObservableField<String>()
    var ytdWinLose2Text = ObservableField<String>()
    var ytdMatches1Text = ObservableField<String>()
    var ytdMatches2Text = ObservableField<String>()
    var careerTitles1Text = ObservableField<String>()
    var careerTitles2Text = ObservableField<String>()
    var careerWinLose1Text = ObservableField<String>()
    var careerWinLose2Text = ObservableField<String>()
    var debut1Text = ObservableField<String>()
    var debut2Text = ObservableField<String>()
    var highRank1Text = ObservableField<String>()
    var highRank2Text = ObservableField<String>()
    var scoreRank1Text = ObservableField<String>()
    var scoreRank2Text = ObservableField<String>()

    var h2hObserver = MutableLiveData<List<H2hItem>>()
    private var h2hList = listOf<H2hItem>()
    private var mLevelId = -1

    val h2hRepository = H2hRepository()
    val rankRepository = RankRepository()

    var player1: RecordWrap? = null

    var player2: RecordWrap? = null

    var player1Color: Int = 0
    var player2Color: Int = 0

    var indexToReceivePlayer = -1

    init {
        val colors = listOf(
            getResource().getColor(R.color.h2h_bg_1),
            getResource().getColor(R.color.h2h_bg_2),
            getResource().getColor(R.color.h2h_bg_3),
            getResource().getColor(R.color.h2h_bg_4),
            getResource().getColor(R.color.h2h_bg_5)
        ).shuffled().take(2)

        player1WinColor.set(colors[0])
        player2WinColor.set(colors[1])
    }

    fun loadH2h(id1: Long, id2: Long) {
        player1 = getDatabase().getRecordDao().getRecord(id1)
        onPlayer1Changed()
        player2 = getDatabase().getRecordDao().getRecord(id2)
        onPlayer2Changed()
    }

    private fun onPlayer1Changed() {
        player1?.let {
            player1Name.set(it.bean.name)
            player1ImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
            onH2hChanged()
            loadPlayerInfo(it,
                InfoPart(
                    player1Rank, ytdTitles1Text, ytdWinLose1Text, ytdMatches1Text,
                    careerTitles1Text, careerWinLose1Text, debut1Text,
                    highRank1Text, scoreRank1Text
                )
            )
        }
    }

    private fun onPlayer2Changed() {
        player2?.let {
            player2Name.set(it.bean.name)
            player2ImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
            onH2hChanged()
            loadPlayerInfo(it,
                InfoPart(
                    player2Rank, ytdTitles2Text, ytdWinLose2Text, ytdMatches2Text,
                    careerTitles2Text, careerWinLose2Text, debut2Text,
                    highRank2Text, scoreRank2Text
                )
            )
        }
    }

    private fun onH2hChanged() {
        if (player1 != null && player2 != null) {
            h2hRepository.getH2hItems(player1!!.bean.id!!, player2!!.bean.id!!)
                .flatMap { calculateWin(it, player1!!.bean.id!!, player2!!.bean.id!!) }
                .flatMap { filterByLevel(it) }
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<List<H2hItem>>(getComposite()){
                    override fun onNext(t: List<H2hItem>) {
                        h2hList = t
                        h2hObserver.value = t
                    }

                    override fun onError(e: Throwable) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    private fun calculateWin(list: List<H2hItem>, player1Id: Long, player2Id: Long): ObservableSource<List<H2hItem>> {
        return ObservableSource {

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
            player1Win.set(win1.toString())
            player2Win.set(win2.toString())
            list.forEach { item ->
                item.bgColor = if (item.matchItem.bean.winnerId == player1Id) {
                    player1WinColor.get()
                }
                else {
                    player2WinColor.get()
                }
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun filterByLevel(list: List<H2hItem>): ObservableSource<List<H2hItem>> {
        return ObservableSource {
            if (mLevelId == -1) {
                player1FilterWin.set(player1Win.get())
                player2FilterWin.set(player2Win.get())
                it.onNext(list)
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
                player1FilterWin.set("$win1")
                player2FilterWin.set("$win2")
                it.onNext(result)
            }
            it.onComplete()
        }
    }

    fun loadReceivePlayer(playerId: Long) {
        var player = getDatabase().getRecordDao().getRecord(playerId)
        if (indexToReceivePlayer == 1) {
            player1 = player
            onPlayer1Changed()
        }
        else {
            player2 = player
            onPlayer2Changed()
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
        var rankScore: ObservableField<String>
    )

    private fun loadPlayerInfo(record: RecordWrap, infoPart: InfoPart) {
        playerInfo(record, infoPart)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {

                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message?:""
                }
            })
    }

    private fun playerInfo(record: RecordWrap, infoPart: InfoPart): Observable<Boolean> {
        return Observable.create {
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
            it.onNext(true)
            it.toString()
        }
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
        onH2hChanged()
    }

}