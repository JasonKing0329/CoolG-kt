package com.king.app.coolg_kt.page.match.rank

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.coolg_kt.page.match.ShowPeriod
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.match.MatchRankRecord
import com.king.app.gdb.data.entity.match.MatchRankStar
import com.king.app.gdb.data.relation.MatchRankRecordWrap
import com.king.app.gdb.data.relation.MatchRankStarWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 16:28
 */
class RankViewModel(application: Application): BaseViewModel(application) {

    var recordRanksObserver = MutableLiveData<List<RankItem<Record?>>>()
    var starRanksObserver = MutableLiveData<List<RankItem<Star?>>>()

    var periodGroupVisibility = ObservableInt(View.GONE)
    var periodLastVisibility = ObservableInt(View.GONE)
    var periodNextVisibility = ObservableInt(View.GONE)
    var periodText = ObservableField<String>()

    private var rankRepository = RankRepository()

    // 由record/star spinner来触发初始化，recordOrStar设为-1来标识变化
    var periodOrRtf = 0 //0 period, 1 RTF
    var recordOrStar = -1 // 0 record, 1 star
    var showPeriod : ShowPeriod
    var currentPeriod: ShowPeriod

    init {
        val rankPack = rankRepository.getRankPeriodPack()
        showPeriod = if (rankPack.matchPeriod == null) {
            ShowPeriod(0, 0)
        } else {
            ShowPeriod(rankPack.matchPeriod!!.period, rankPack.matchPeriod!!.orderInPeriod)
        }
        currentPeriod = showPeriod.copy()
    }

    fun onPeriodOrRtfChanged(periodOrRtf: Int) {
        if (this.periodOrRtf != periodOrRtf) {
            this.periodOrRtf = periodOrRtf
            if (periodOrRtf == 0) {
                periodGroupVisibility.set(View.VISIBLE)
            }
            else {
                periodGroupVisibility.set(View.GONE)
            }
            loadData()
        }
    }

    fun onRecordOrStarChanged(recordOrStar: Int) {
        if (this.recordOrStar != recordOrStar) {
            this.recordOrStar = recordOrStar
            loadData()
        }
    }

    private fun loadData() {
        if (recordOrStar == 0) {
            if (periodOrRtf == 0) {
                periodGroupVisibility.set(View.VISIBLE)
                loadRecordRankPeriod()
            }
            else {
                periodGroupVisibility.set(View.GONE)
                loadRecordRaceToFinal()
            }
        }
        else {
            if (periodOrRtf == 0) {
                periodGroupVisibility.set(View.VISIBLE)
                loadStarRankPeriod()
            }
            else {
                periodGroupVisibility.set(View.GONE)
                loadStarRaceToFinal()
            }
        }
    }

    fun isLastRecordRankCreated(): Boolean {
        return rankRepository.isRecordRankCreated()
    }

    fun isLastStarRankCreated(): Boolean {
        return rankRepository.isStarRankCreated()
    }

    private fun loadRecordRankPeriod() {
        loadingObserver.value = true
        recordRankPeriodRx()
            .flatMap { toRecordList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Record?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Record?>>?) {
                    checkRecordLastNext()
                    loadingObserver.value = false
                    recordRanksObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun recordRankPeriodRx(): Observable<List<MatchRankRecordWrap>> {
        periodText.set("P${showPeriod.period}-W${showPeriod.orderInPeriod}")
        // 只有当前week的排名要判断是否统计当前积分，其他情况都从rank表里取
        return if (currentPeriod.period == showPeriod.period && currentPeriod.orderInPeriod == showPeriod.orderInPeriod) {
            // 从match_rank_record表中查询
            if (rankRepository.isRecordRankCreated()) {
                DebugLog.e("record rank from table")
                rankRepository.getRankPeriodRecordRanks()
            }
            // 实时统计积分排名
            else {
                DebugLog.e("record rank from score")
                rankRepository.getRankPeriodRecordScores()
                    .flatMap { toMatchRankRecords(it) }
            }
        }
        else {
            // 从match_rank_record表中查询
            DebugLog.e("record rank from table")
            rankRepository.getSpecificPeriodRecordRanks(showPeriod.period, showPeriod.orderInPeriod)
        }

    }

    /**
     * race to final肯定实时统计，不做数据表存储
     */
    private fun loadRecordRaceToFinal() {
        loadingObserver.value = true
        rankRepository.getRTFRecordScores()
            .flatMap { toMatchRankRecords(it) }
            .flatMap { toRecordList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Record?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Record?>>?) {
                    loadingObserver.value = false
                    recordRanksObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun loadStarRankPeriod() {
        loadingObserver.value = true
        starRankPeriodRx()
            .flatMap { toStarList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Star?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Star?>>?) {
                    checkStarLastNext()
                    loadingObserver.value = false
                    starRanksObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun starRankPeriodRx(): Observable<List<MatchRankStarWrap>> {
        periodText.set("P${showPeriod.period}-W${showPeriod.orderInPeriod}")
        // 只有当前week的排名要判断是否统计当前积分，其他情况都从rank表里取
        return if (currentPeriod.period == showPeriod.period && currentPeriod.orderInPeriod == showPeriod.orderInPeriod) {
            // 从match_rank_record表中查询
            return if (rankRepository.isStarRankCreated()) {
                DebugLog.e("star rank from table")
                rankRepository.getRankPeriodStarRanks()
            }
            // 实时统计积分排名
            else {
                DebugLog.e("star rank from score")
                rankRepository.getRankPeriodStarScores()
                    .flatMap { toMatchRankStars(it) }
            }
        }
        else {
            // 从match_rank_record表中查询
            DebugLog.e("star rank from table")
            rankRepository.getSpecificPeriodStarRanks(showPeriod.period, showPeriod.orderInPeriod)
        }
    }

    /**
     * race to final肯定实时统计，不做数据表存储
     */
    private fun loadStarRaceToFinal() {
        loadingObserver.value = true
        rankRepository.getRTFStarScores()
            .flatMap { toMatchRankStars(it) }
            .flatMap { toStarList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RankItem<Star?>>>(getComposite()) {
                override fun onNext(t: List<RankItem<Star?>>?) {
                    loadingObserver.value = false
                    starRanksObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toMatchRankRecords(list: List<ScoreCount>): ObservableSource<List<MatchRankRecordWrap>> {
        return ObservableSource {
            var result = mutableListOf<MatchRankRecordWrap>()
            list.forEachIndexed { index, scoreCount ->
                var record = getDatabase().getRecordDao().getRecordBasic(scoreCount.id)
                result.add(MatchRankRecordWrap(
                    MatchRankRecord(0, 0, 0,
                    scoreCount.id, index + 1, scoreCount.score, scoreCount.matchCount), record
                ))
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun toRecordList(list: List<MatchRankRecordWrap>): ObservableSource<List<RankItem<Record?>>> {
        return ObservableSource {
            var result = mutableListOf<RankItem<Record?>>()
            list.forEach { bean ->
                var url = ImageProvider.getRecordRandomPath(bean.record?.name, null)
                var item = RankItem(bean.record, bean.bean.recordId, bean.bean.rank, ""
                    , url, bean.record?.name, bean.bean.score, bean.bean.matchCount)
                result.add(item)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun toMatchRankStars(list: List<ScoreCount>): ObservableSource<List<MatchRankStarWrap>> {
        return ObservableSource {
            var result = mutableListOf<MatchRankStarWrap>()
            list.forEachIndexed { index, scoreCount ->
                var star = getDatabase().getStarDao().getStar(scoreCount.id)
                result.add(MatchRankStarWrap(
                    MatchRankStar(0, 0, 0,
                        scoreCount.id, index + 1, scoreCount.score, scoreCount.matchCount), star
                ))
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun toStarList(list: List<MatchRankStarWrap>): ObservableSource<List<RankItem<Star?>>> {
        return ObservableSource {
            var result = mutableListOf<RankItem<Star?>>()
            list.forEach { bean ->
                var url = ImageProvider.getStarRandomPath(bean.star?.name, null)
                var item = RankItem(bean.star, bean.bean.starId, bean.bean.rank, ""
                    , url, bean.star?.name, bean.bean.score, bean.bean.matchCount)
                result.add(item)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun checkRecordLastNext() {
        // last
        val last = rankRepository.getLastPeriod(showPeriod)
        if (last.period > 0 && getDatabase().getMatchDao().countRecordRankItems(last.period, last.orderInPeriod) > 0) {
            periodLastVisibility.set(View.VISIBLE)
        }
        else {
            periodLastVisibility.set(View.INVISIBLE)
        }
        // next
        val next = rankRepository.getNextPeriod(showPeriod)
        if (next.period == currentPeriod.period && currentPeriod.orderInPeriod == currentPeriod.orderInPeriod ||
            getDatabase().getMatchDao().countRecordRankItems(next.period, next.orderInPeriod) > 0) {
            periodNextVisibility.set(View.VISIBLE)
        }
        else {
            periodNextVisibility.set(View.INVISIBLE)
        }
    }

    private fun checkStarLastNext() {
        // last
        var period = showPeriod.period
        var orderInPeriod = showPeriod.orderInPeriod - 1
        if (orderInPeriod == 0) {
            period -= 1
            orderInPeriod = MatchConstants.MAX_ORDER_IN_PERIOD
        }
        if (period > 0 && getDatabase().getMatchDao().countStarRankItems(period, orderInPeriod) > 0) {
            periodLastVisibility.set(View.VISIBLE)
        }
        else {
            periodLastVisibility.set(View.INVISIBLE)
        }
        // next
        period = showPeriod.period
        orderInPeriod = showPeriod.orderInPeriod + 1
        if (orderInPeriod > MatchConstants.MAX_ORDER_IN_PERIOD) {
            period += 1
            orderInPeriod = 1
        }
        if (getDatabase().getMatchDao().countStarRankItems(period, orderInPeriod) > 0) {
            periodNextVisibility.set(View.VISIBLE)
        }
        else {
            periodNextVisibility.set(View.INVISIBLE)
        }
    }

    fun createRankRecord() {
        insertRankRecordList()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    messageObserver.value = "success"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun createRankStar() {
        insertRankStarList()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    messageObserver.value = "success"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun insertRankRecordList(): Observable<Boolean> {
        return Observable.create {
            rankRepository.getRankPeriodPack().matchPeriod?.let { matchPeriod ->
                getDatabase().getMatchDao().deleteMatchRankRecords(matchPeriod.period, matchPeriod.orderInPeriod)
                var insertList = mutableListOf<MatchRankRecord>()
                recordRanksObserver.value?.forEachIndexed { index, rankItem ->
                    val bean = MatchRankRecord(0, matchPeriod.period, matchPeriod.orderInPeriod,
                        rankItem.id, index + 1, rankItem.score, rankItem.matchCount)
                    insertList.add(bean)
                }
                getDatabase().getMatchDao().insertMatchRankRecords(insertList)
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    private fun insertRankStarList(): Observable<Boolean> {
        return Observable.create {
            rankRepository.getRankPeriodPack().matchPeriod?.let { matchPeriod ->
                getDatabase().getMatchDao().deleteMatchRankStars(matchPeriod.period, matchPeriod.orderInPeriod)
                var insertList = mutableListOf<MatchRankStar>()
                starRanksObserver.value?.forEachIndexed { index, rankItem ->
                    val bean = MatchRankStar(0, matchPeriod.period, matchPeriod.orderInPeriod,
                        rankItem.id, index + 1, rankItem.score, rankItem.matchCount)
                    insertList.add(bean)
                }
                getDatabase().getMatchDao().insertMatchRankStars(insertList)
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    fun nextPeriod() {
        showPeriod = rankRepository.getNextPeriod(showPeriod)
        loadData()
    }

    fun lastPeriod() {
        showPeriod = rankRepository.getLastPeriod(showPeriod)
        loadData()
    }
}