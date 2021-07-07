package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.module.BasicAndTimeWaste
import com.king.app.coolg_kt.model.module.TimeWasteTask
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.CareerCategoryMatch
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.gdb.data.entity.match.Match
import com.king.app.gdb.data.entity.match.MatchItem
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/7 16:11
 */
class CareerMatchViewModel(application: Application): BaseViewModel(application) {

    var matchesObserver = MutableLiveData<List<Any>>()
    var rangeChangedObserver = MutableLiveData<TimeWasteRange>()
    var originList = listOf<Match>()

    var mRecordId: Long = 0
    var rankRepository = RankRepository()

    fun loadMatches() {
        BasicAndTimeWaste<Any>()
            .basic(basicMatches())
            .timeWaste(timeWaste(), 10)
            .composite(getComposite())
            .subscribe(
                object : SimpleObserver<List<Any>>(getComposite()) {
                    override fun onNext(t: List<Any>) {
                        matchesObserver.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                },
                object : SimpleObserver<TimeWasteRange>(getComposite()) {
                    override fun onNext(t: TimeWasteRange) {
                        rangeChangedObserver.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                }
            )
    }

    private fun basicMatches(): Observable<List<Any>> {
        return Observable.create {
            val result = mutableListOf<Any>()
            var list = getDatabase().getMatchDao().getAllMatchesByOrder()
            list.forEach { match ->
                match.imgUrl = ImageProvider.parseCoverUrl(match.imgUrl)?:""
                // 耗时操作都交给timewaste
                result.add(CareerCategoryMatch(match, 0, "", ""))
            }
            originList = list
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun timeWaste(): TimeWasteTask<Any> {
        return object : TimeWasteTask<Any> {
            override fun handle(data: Any) {
                if (data is CareerCategoryMatch) {
                    val items = getDatabase().getMatchDao().getRecordMatchItems(mRecordId, data.match.id)
                    var win = 0
                    var lose = 0
                    val periodMap = mutableMapOf<Long, MutableList<MatchItem>?>()
                    items.forEach { item ->
                        if (item.winnerId == mRecordId) {
                            win ++
                        }
                        else if (item.winnerId?:0L != 0L) {
                            lose ++
                        }
                        var list = periodMap[item.matchId]
                        if (list == null) {
                            list = mutableListOf()
                            periodMap[item.matchId] = list
                        }
                        list.add(item)
                    }
                    // win lose
                    data.winLose = "${win}胜${lose}负"
                    // times
                    data.times = periodMap.keys.size
                    // best，只取最好的3个轮次
                    val countlist = mutableListOf<MatchCount>()
                    periodMap.keys.forEach { matchPeriodId ->
                        val count = toMatchCount(matchPeriodId, periodMap[matchPeriodId]!!)
                    }
                }
            }
        }
    }

    private fun toMatchCount(matchPeriodId: Long, list: List<MatchItem>): MatchCount {
        val matchPeriod = getDatabase().getMatchDao().getMatchPeriod(matchPeriodId)
        val bestRound = MatchConstants.ROUND_ID_Q3
        list.forEach {
            if (matchPeriod.match.level == MatchConstants.MATCH_LEVEL_FINAL) {

            }
            else {

            }
            val value = MatchConstants.getRoundSortValue(it.round)
        }
        return MatchCount(matchPeriodId, matchPeriod.bean.period, "", 0)
    }

    data class MatchCount(
        var matchPeriodId: Long,
        var period: Int,
        var roundShort: String,
        var roundWeight: Int
    )
}