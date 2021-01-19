package com.king.app.coolg_kt.page.match.draw

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.page.match.*
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/19 16:11
 */
class FinalDrawViewModel(application: Application): BaseViewModel(application) {

    val drawRepository = DrawRepository()

    lateinit var matchPeriod: MatchPeriodWrap

    var dataObserver = MutableLiveData<List<Any>>()

    var createdDrawData: FinalDrawData? = null

    fun loadMatch(matchPeriodId: Long) {
        loadingObserver.value = true
        matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        loadPageData()
            .flatMap { toViewList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    loadingObserver.value = false
                    dataObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    private fun loadPageData(): Observable<FinalDrawData> {
        return Observable.create {
            var groupAList = mutableListOf<RecordWithRank>()
            var groupBList = mutableListOf<RecordWithRank>()
            var head = FinalHead(groupAList, groupBList)
            var scoreAList = mutableListOf<FinalScore>()
            var scoreBList = mutableListOf<FinalScore>()

            var roundMap = mutableMapOf<String, MutableList<DrawItem>?>()
            // round, order已有序
            var matchItems = getDatabase().getMatchDao().getMatchItemsSorted(matchPeriod.bean.id)
            matchItems.forEach { wrap ->
                var round = MatchConstants.roundFull(wrap.bean.round)
                var roundItems = roundMap[round]
                if (roundItems == null) {
                    roundItems = mutableListOf()
                    roundMap[round] = roundItems
                }
                var m1 = getDatabase().getMatchDao().getMatchRecord(wrap.bean.id, 1)
                var m2 = getDatabase().getMatchDao().getMatchRecord(wrap.bean.id, 2)
                var winner: MatchRecordWrap? = null
                if (wrap.bean.winnerId == m1!!.bean.recordId) {
                    winner = m1
                }
                else if (wrap.bean.winnerId == m2!!.bean.recordId) {
                    winner = m2
                }
                roundItems.add(DrawItem(wrap.bean, m1, m2, winner))
            }
            // define group for all records
            val firstRound = roundMap[MatchConstants.roundFull(MatchConstants.ROUND_ID_GROUP)]
            firstRound?.forEach { item ->
                if (item.matchItem.groupFlag == 0) {
                    addToGroup(item, groupAList, scoreAList)
                }
                else {
                    addToGroup(item, groupBList, scoreBList)
                }
            }
            var finalDrawData = FinalDrawData(matchPeriod.bean, head, scoreAList, scoreBList, roundMap)
            it.onNext(finalDrawData)
            it.onComplete()
        }
    }

    private fun toViewList(finalDrawData: FinalDrawData): ObservableSource<List<Any>> {
        return ObservableSource {
            var list = mutableListOf<Any>()
            list.add(finalDrawData.head)
            for (i in finalDrawData.scoreAList.indices) {
                list.add(finalDrawData.scoreAList[i])
                list.add(finalDrawData.scoreBList[i])
            }
            val roundRobinText = MatchConstants.roundFull(MatchConstants.ROUND_ID_GROUP)
            val roundSf = MatchConstants.roundFull(MatchConstants.ROUND_ID_SF)
            val roundF = MatchConstants.roundFull(MatchConstants.ROUND_ID_F)
            finalDrawData.roundMap[roundRobinText]?.let { items ->
                list.add(FinalRound(roundRobinText))
                list.addAll(items)
            }
            finalDrawData.roundMap[roundSf]?.let { items ->
                list.add(FinalRound(roundRobinText))
                list.addAll(items)
            }
            finalDrawData.roundMap[roundF]?.let { items ->
                list.add(FinalRound(roundRobinText))
                list.addAll(items)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun addToGroup(item: DrawItem, groupList: MutableList<RecordWithRank>, scoreList: MutableList<FinalScore>) {
        val record1 = getDatabase().getRecordDao().getRecord(item.matchRecord1!!.bean.recordId)!!
        record1.imageUrl = ImageProvider.getRecordRandomPath(record1.bean.name, null)
        val record2 = getDatabase().getRecordDao().getRecord(item.matchRecord2!!.bean.recordId)!!
        record2.imageUrl = ImageProvider.getRecordRandomPath(record2.bean.name, null)
        var r1 = groupList.firstOrNull { it.record.bean.id == record1.bean.id }
        if (r1 == null) {
            groupList.add(RecordWithRank(record1, item.matchRecord1!!.bean.recordRank!!))
        }
        var r2 = groupList.firstOrNull { it.record.bean.id == record2.bean.id }
        if (r1 == null) {
            groupList.add(RecordWithRank(record2, item.matchRecord2!!.bean.recordRank!!))
        }
        var s1 = scoreList.firstOrNull { it.record.bean.id == record1.bean.id }
        if (s1 == null) {
            scoreList.add(FinalScore("0", record1, ""))
        }
        else {

        }
    }

    fun createDraw() {
        loadingObserver.value = true
        drawRepository.createFinalDraw(matchPeriod)
            .flatMap {
                createdDrawData = it
                toViewList(it)
            }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    loadingObserver.value = false
                    dataObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }
}