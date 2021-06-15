package com.king.app.coolg_kt.page.match.record

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RecordMatchPageItem
import com.king.app.coolg_kt.page.match.RecordMatchPageTitle
import io.reactivex.rxjava3.core.Observable

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

    var itemsObserver = MutableLiveData<List<Any>>()

    var mRecordId: Long = 0
    var mMatchId: Long = 0

    var rankRepository = RankRepository()

    fun loadData() {
        getDatabase().getRecordDao().getRecord(mRecordId)?.let {
            recordImageUrl.set(ImageProvider.getRecordRandomPath(it.bean.name, null))
            var rank = rankRepository.getRecordCurrentRank(mRecordId)
            recordRankText.set("Rank $rank")

            var match = getDatabase().getMatchDao().getMatch(mMatchId)
            matchLevelText.set(MatchConstants.MATCH_LEVEL[match.level])
            matchImageUrl.set(ImageProvider.parseCoverUrl(match.imgUrl))
            matchWeekText.set("W${match.orderInPeriod}")
            matchNameText.set(match.name)

            loadItems()
        }
    }

    private fun loadItems() {
        getItems()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>?) {
                    itemsObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message?:""
                }

            })
    }

    private fun getItems(): Observable<List<Any>> {
        return Observable.create {
            var result = mutableListOf<Any>()
            var win = 0
            var lose = 0
            var lastPeriod = 0
            // 先按period分组，再在period中按round排序
            val titleList = mutableListOf<RecordMatchPageTitle>()
            var itemMap = mutableMapOf<Int, MutableList<RecordMatchPageItem>>()
            var titleBean: RecordMatchPageTitle? = null
            getDatabase().getMatchDao().getRecordCompetitorsInMatch(mRecordId, mMatchId)
                .forEach { mr ->
                    getDatabase().getMatchDao().getMatchItem(mr.matchItemId)?.let { matchItem ->
                        val mp = getDatabase().getMatchDao().getMatchPeriod(matchItem.matchId)
                        val isWinner = matchItem.winnerId == mRecordId
                        // 要考虑未完赛的情况
                        if (matchItem.winnerId == mRecordId) {
                            win ++
                        }
                        else if (matchItem.winnerId == mr.recordId) {
                            lose ++
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
                        val url = ImageProvider.getRecordRandomPath(record?.name, null)
                        val sortValue = MatchConstants.getRoundSortValue(matchItem.round)
                        val isChampion = isWinner && MatchConstants.ROUND_ID_F == matchItem.round
                        itemMap[mp.bean.period]!!.add(RecordMatchPageItem(round, record, rankSeed, url, sortValue, isChampion))
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
            winLoseText.set("${win}胜${lose}负")
            it.onNext(result)
            it.onComplete()
        }
    }
}