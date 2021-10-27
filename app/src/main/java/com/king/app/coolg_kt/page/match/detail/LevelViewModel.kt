package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.page.match.RoundItem
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/25 11:14
 */
class LevelViewModel(application: Application): BaseViewModel(application) {

    var matchCount = 0
    var mRecordId = 0L

    var listObserver = MutableLiveData<List<RoundItem>>()

    fun loadGsData() {
        loadData(MatchConstants.MATCH_LEVEL_GS)
    }

    fun loadGm1000Data() {
        loadData(MatchConstants.MATCH_LEVEL_GM1000)
    }

    private fun loadData(level: Int) {
        getLevelData(level)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RoundItem>>(getComposite()){
                override fun onNext(t: List<RoundItem>) {
                    listObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun getLevelData(level: Int): Observable<List<RoundItem>> {
        return Observable.create {
            val list = mutableListOf<RoundItem>()
            val matches = getDatabase().getMatchDao().getMatchByLevel(level)
            matchCount = matches.size
            // title
            list.add(RoundItem(true, true, "Period"))
            matches.forEach { match ->
                list.add(RoundItem(true, false, match.name))
            }
            // tables
            var last = getDatabase().getMatchDao().getLastMatchPeriod()
            last?.let { lp ->
                for (i in 1..lp.period) {
                    var pList = mutableListOf<RoundItem>();
                    var isPeriodValid = false
                    matches.forEach { match ->
                        val result = getDatabase().getMatchDao().getResultMatchItem(mRecordId, match.id, i)
                        if (result == null) {
                            pList.add(RoundItem(false, false, "--", 0))
                        }
                        else {
                            isPeriodValid = true;
                            val round = result.round
                            val text = MatchConstants.roundResultShort(round, result.winnerId == mRecordId)
                            pList.add(RoundItem(false, false, text, result.matchId))
                        }
                    }
                    if (isPeriodValid) {
                        list.add(RoundItem(false, true, "P$i"))
                        list.addAll(pList)
                    }
                }
            }
            it.onNext(list)
            it.onComplete()
        }
    }
}