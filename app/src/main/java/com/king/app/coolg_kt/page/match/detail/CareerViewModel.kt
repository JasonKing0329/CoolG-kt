package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.module.BasicAndTimeWaste
import com.king.app.coolg_kt.model.module.TimeWasteTask
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.CareerMatch
import com.king.app.coolg_kt.page.match.CareerPeriod
import com.king.app.coolg_kt.page.match.CareerRecord
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.gdb.data.entity.match.Match
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/30 14:36
 */
class CareerViewModel(application: Application): BaseViewModel(application) {

    val rankRepository = RankRepository()

    var periodList = MutableLiveData<List<CareerPeriod>>()

    var periodChanged = MutableLiveData<Int>()

    var mRecordId: Long = 0

    fun loadData(recordId: Long) {
        mRecordId = recordId
        BasicAndTimeWaste<CareerPeriod>()
            .basic(getData())
            .timeWaste(timeWaste(), 1)
            .composite(getComposite())
            .subscribe(
                object : SimpleObserver<List<CareerPeriod>>(getComposite()) {
                    override fun onNext(t: List<CareerPeriod>) {
                        periodList.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                },
                object : SimpleObserver<TimeWasteRange>(getComposite()) {
                    override fun onNext(t: TimeWasteRange) {
                        periodChanged.value = t.start
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                }
            )
    }

    private fun getData(): Observable<List<CareerPeriod>> {
        return Observable.create {
            val list = mutableListOf<CareerPeriod>()
            val items = getDatabase().getMatchDao().getAllTimeMatchRecordsCompetitor(mRecordId)
            // 降低数据库查询频率, key:matchPeriodId
            val matchMap = mutableMapOf<Long, Match?>()
            items.forEach { item ->
                var period = list.firstOrNull { period -> period.period == item.period}
                if (period == null) {
                    // detail与titles后续加载
                    period = CareerPeriod(item.period, "", 0, mutableListOf())
                    list.add(period)
                }
                var match = period.matches.firstOrNull { match -> match.matchPeriodId == item.bean.matchId }
                if (match == null) {
                    var matchBean = matchMap[item.bean.matchId]
                    if (matchBean == null) {
                        matchBean = getDatabase().getMatchDao().getMatchPeriod(item.bean.matchId).match
                        matchMap[item.bean.matchId] = matchBean
                    }
                    val color = when(matchBean.level) {
                        MatchConstants.MATCH_LEVEL_GS -> getResource().getColor(R.color.match_level_gs)
                        MatchConstants.MATCH_LEVEL_FINAL -> getResource().getColor(R.color.match_level_final)
                        MatchConstants.MATCH_LEVEL_GM1000 -> getResource().getColor(R.color.match_level_gm1000)
                        MatchConstants.MATCH_LEVEL_GM500 -> getResource().getColor(R.color.match_level_gm500)
                        MatchConstants.MATCH_LEVEL_GM250 -> getResource().getColor(R.color.match_level_gm250)
                        else -> getResource().getColor(R.color.match_level_low)
                    }
                    // rank seed, result后续加载
                    match = CareerMatch(period, item.bean.matchId, matchBean.name, item.orderInPeriod,
                        MatchConstants.MATCH_LEVEL[matchBean.level], color, "${matchBean.draws} Draws", "", "",
                        View.GONE, mutableListOf())
                    period.matches.add(match)
                }
                val isWin = item.winnerId == mRecordId
                if (isWin) period.winCount ++
                else period.loseCount ++
                val record = getDatabase().getRecordDao().getRecord(item.bean.recordId)
                val rankSeed = if (item.bean.recordSeed?:0 > 0) {
                    "(Rank ${item.bean.recordRank} / [${item.bean.recordSeed}])"
                }
                else {
                    "(Rank ${item.bean.recordRank})"
                }
                var careerRecord = CareerRecord(match, MatchConstants.roundResultShort(item.round, false), record?.bean, rankSeed,
                null, isWin, MatchConstants.getRoundSortValue(item.round))
                match.records.add(careerRecord)
            }
            // 根据Comparable定义重新排序，生成不耗时的统计信息
            list.sortByDescending { period -> period.period }// period降序
            list.forEach { period ->
                var titlesCount = 0
                period.detail = if (period.matches.size > 1) {
                    "${period.matches.size} matches, ${period.winCount}胜${period.loseCount}负"
                }
                else {
                    "${period.matches.size} match, ${period.winCount}胜${period.loseCount}负"
                }
                period.matches.sortByDescending { match -> match.week }// week降序
                period.matches.forEach { match ->
                    match.records.sortByDescending { record -> record.sortValue }// round降序
                    match.records.firstOrNull()?.let { record ->
                        match.result = record.round
                        if (record.isWinner && "F" == record.round) {
                            match.result = "Win"
                            match.championVisibility = View.VISIBLE
                            titlesCount ++
                        }
                    }
                }
                period.titles = titlesCount
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    /**
     * 异步耗时操作
     */
    private fun timeWaste(): TimeWasteTask<CareerPeriod> {
        return object : TimeWasteTask<CareerPeriod> {
            override fun handle(index: Int, data: CareerPeriod) {
                data.matches.forEach { match ->
                    match.records.forEach { record ->
                        // image url
                        record.imageUrl = ImageProvider.getRecordRandomPath(record.record?.name, null)
                    }
                    // rank seed
                    rankRepository.getRecordRankInMatch(mRecordId, match.matchPeriodId)?.let { bean ->
                        match.rankSeed = if (bean.recordSeed?:0 > 0) {
                            "R ${bean.recordRank}/[${bean.recordSeed}]"
                        }
                        else {
                            "R ${bean.recordRank}"
                        }
                    }
                }
            }
        }
    }
}