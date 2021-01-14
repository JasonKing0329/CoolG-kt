package com.king.app.coolg_kt.page.match.rank

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.gdb.data.bean.ScoreCount
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star
import io.reactivex.rxjava3.core.ObservableSource

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 16:28
 */
class RankViewModel(application: Application): BaseViewModel(application) {

    var recordRanksObserver = MutableLiveData<List<RankItem<Record?>>>()
    var starRanksObserver = MutableLiveData<List<RankItem<Star?>>>()

    var rankRepository = RankRepository()

//    fun recordRankPeriodRx(): Observable<List<RankItem<Record?>>> {
//        if (rankRepository.isRecordRankCreated()) {
//
//        }
//        else {
//            return rankRepository.getRankPeriodRecordScores()
//                .flatMap { toRecordList(it) }
//        }
//    }

    fun loadRecordRankPeriod() {
        loadingObserver.value = true
        rankRepository.getRankPeriodRecordScores()
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

    fun loadRecordRaceToFinal() {
        loadingObserver.value = true
        rankRepository.getRTFRecordScores()
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
        rankRepository.getRankPeriodStarScores()
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

    fun loadStarRaceToFinal() {
        loadingObserver.value = true
        rankRepository.getRTFStarScores()
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

//    private fun toRankRecordList(list: List<MatchRankRecordWrap>): ObservableSource<List<RankItem<Record?>>> {
//        return ObservableSource {
//            var result = mutableListOf<RankItem<Record?>>()
//            list.forEachIndexed { index, bean ->
//                var url = ImageProvider.getRecordRandomPath(record?.name, null)
//                var item = RankItem(bean.re, scoreCount.id, (index + 1).toString(), ""
//                    , url, record?.name, scoreCount.score.toString(), scoreCount.matchCount.toString())
//                result.add(item)
//            }
//            it.onNext(result)
//            it.onComplete()
//        }
//    }

    private fun toRecordList(list: List<ScoreCount>): ObservableSource<List<RankItem<Record?>>> {
        return ObservableSource {
            var result = mutableListOf<RankItem<Record?>>()
            list.forEachIndexed { index, scoreCount ->
                var record = getDatabase().getRecordDao().getRecordBasic(scoreCount.id)
                var url = ImageProvider.getRecordRandomPath(record?.name, null)
                var item = RankItem(record, scoreCount.id, (index + 1).toString(), ""
                    , url, record?.name, scoreCount.score.toString(), scoreCount.matchCount.toString())
                result.add(item)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun toStarList(list: List<ScoreCount>): ObservableSource<List<RankItem<Star?>>> {
        return ObservableSource {
            var result = mutableListOf<RankItem<Star?>>()
            list.forEachIndexed { index, scoreCount ->
                var star = getDatabase().getStarDao().getStar(scoreCount.id)
                var url = ImageProvider.getStarRandomPath(star?.name, null)
                var item = RankItem(star, scoreCount.id, (index + 1).toString(), ""
                    , url, star?.name, scoreCount.score.toString(), scoreCount.matchCount.toString())
                result.add(item)
            }
            it.onNext(result)
            it.onComplete()
        }
    }
}