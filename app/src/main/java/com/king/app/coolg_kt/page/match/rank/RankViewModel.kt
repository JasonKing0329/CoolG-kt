package com.king.app.coolg_kt.page.match.rank

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RankItem
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

    private var rankRepository = RankRepository()

    fun isLastRecordRankCreated(): Boolean {
        return rankRepository.isRecordRankCreated()
    }

    fun isLastStarRankCreated(): Boolean {
        return rankRepository.isStarRankCreated()
    }

    fun loadRecordRankPeriod() {
        loadingObserver.value = true
        recordRankPeriodRx()
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

    private fun recordRankPeriodRx(): Observable<List<MatchRankRecordWrap>> {
        // 从match_rank_record表中查询
        return if (rankRepository.isRecordRankCreated()) {
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

    /**
     * race to final肯定实时统计，不做数据表存储
     */
    fun loadRecordRaceToFinal() {
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

    fun loadStarRankPeriod() {
        loadingObserver.value = true
        starRankPeriodRx()
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

    private fun starRankPeriodRx(): Observable<List<MatchRankStarWrap>> {
        // 从match_rank_record表中查询
        return if (rankRepository.isStarRankCreated()) {
            DebugLog.e("record rank from table")
            rankRepository.getRankPeriodStarRanks()
        }
        // 实时统计积分排名
        else {
            DebugLog.e("record rank from score")
            rankRepository.getRankPeriodStarScores()
                .flatMap { toMatchRankStars(it) }
        }
    }

    /**
     * race to final肯定实时统计，不做数据表存储
     */
    fun loadStarRaceToFinal() {
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
}