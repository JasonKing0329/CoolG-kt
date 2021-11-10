package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.gdb.data.entity.match.MatchRankRecord
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/10 9:06
 */
class BasicViewModel(application: Application): BaseViewModel(application) {

    private var rankRepository = RankRepository()

    var rankChartList = MutableLiveData<List<MatchRankRecord>>()

    fun loadFinalRanks(recordId: Long) {
        Observable.create<List<MatchRankRecord>> {
            val result = rankRepository.getRecordFinalRanks(recordId)
            it.onNext(result)
            it.onComplete()
        }.compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<MatchRankRecord>>(getComposite()) {
                override fun onNext(t: List<MatchRankRecord>?) {
                    rankChartList.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }
}