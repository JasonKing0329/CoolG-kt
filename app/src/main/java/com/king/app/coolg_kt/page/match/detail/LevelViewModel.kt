package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.page.match.RoundItem
import com.king.app.gdb.data.bean.RecordLevelResult
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
        getLevelDataNew(level)
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

    /**
     * 一次性sql查出全部结果，再逐个从结果中挑出来，大大缩短加载时间
     */
    private fun getLevelDataNew(level: Int): Observable<List<RoundItem>> {
        return Observable.create {
            val list = mutableListOf<RoundItem>()
            val matches = getDatabase().getMatchDao().getMatchByLevel(level)
            matchCount = matches.size
            // title
            list.add(RoundItem(true, true, "Period"))
            matches.forEach { match ->
                list.add(RoundItem(true, false, match.name))
            }

            val results = getDatabase().getMatchDao().getRecordResultOfLevel(mRecordId, level)
            val first = results.firstOrNull()?.period?:0
            val last = results.lastOrNull()?.period?:0
            for (period in first..last) {
                var pList = mutableListOf<RoundItem>();
                matches.forEach { match ->
                    val result = results.firstOrNull { result -> result.period==period && result.matchId==match.id }
                    if (result == null) {
                        pList.add(RoundItem(false, false, "--", 0))
                    }
                    else {
                        val round = result.round
                        val text = MatchConstants.roundResultShort(round, result.winnerId == mRecordId)
                        pList.add(RoundItem(false, false, text, result.matchPeriodId))
                    }
                }
                list.add(RoundItem(false, true, "P$period"))
                list.addAll(pList)
            }

            it.onNext(list)
            it.onComplete()
        }
    }

    @Deprecated("逐个统计结果太耗时", replaceWith = ReplaceWith("getLevelDataNew"))
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
            var first = getDatabase().getMatchDao().getRecordFirstPeriod(mRecordId)
            if (first == 0) {
                first = 1
            }
            var last = getDatabase().getMatchDao().getLastMatchPeriod()
            last?.let { lp ->
                for (i in first..lp.period) {
                    var pList = mutableListOf<RoundItem>();
                    matches.forEach { match ->
                        val result = getDatabase().getMatchDao().getResultMatchItem(mRecordId, match.id, i)
                        if (result == null) {
                            pList.add(RoundItem(false, false, "--", 0))
                        }
                        else {
                            val round = result.round
                            val text = MatchConstants.roundResultShort(round, result.winnerId == mRecordId)
                            pList.add(RoundItem(false, false, text, result.matchId))
                        }
                    }
                    list.add(RoundItem(false, true, "P$i"))
                    list.addAll(pList)
                }
            }
            it.onNext(list)
            it.onComplete()
        }
    }
}