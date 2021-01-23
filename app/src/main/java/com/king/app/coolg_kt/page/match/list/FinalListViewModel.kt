package com.king.app.coolg_kt.page.match.list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.FinalListItem
import com.king.app.gdb.data.relation.MatchRecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/22 13:09
 */
class FinalListViewModel(application: Application): BaseViewModel(application) {

    var dataObserver = MutableLiveData<List<FinalListItem>>()

    private var mFilterLevel = MatchConstants.MATCH_LEVEL_ALL

    fun loadData() {
        getFinals(mFilterLevel)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<FinalListItem>>(getComposite()){
                override fun onNext(t: List<FinalListItem>) {
                    dataObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun getFinals(level: Int): Observable<List<FinalListItem>> {
        return Observable.create {
            val list = mutableListOf<FinalListItem>()
            val matchItems = if (level == MatchConstants.MATCH_LEVEL_ALL) {
                getDatabase().getMatchDao().getMatchItemsByRound(MatchConstants.ROUND_ID_F)
            }
            else {
                getDatabase().getMatchDao().getMatchItemsByRoundLevel(MatchConstants.ROUND_ID_F, level)
            }
            matchItems.forEach { wrap ->
                val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(wrap.bean.matchId)
                val winner = wrap.recordList.first { it.recordId == wrap.bean.winnerId }
                val loser = wrap.recordList.first { it.recordId != wrap.bean.winnerId }
                val winnerRecord = getDatabase().getRecordDao().getRecordBasic(winner.recordId)
                val loserRecord = getDatabase().getRecordDao().getRecordBasic(loser.recordId)
                val r = MatchRecordWrap(winner, winnerRecord)
                r.imageUrl = ImageProvider.getRecordRandomPath(winnerRecord?.name, null)
                val l = MatchRecordWrap(loser, loserRecord)
                l.imageUrl = ImageProvider.getRecordRandomPath(loserRecord?.name, null)
                list.add(FinalListItem(matchPeriod, r, l))
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    fun filterByLevel(levelIndex: Int) {
        mFilterLevel = levelIndex
        loadData()
    }
}