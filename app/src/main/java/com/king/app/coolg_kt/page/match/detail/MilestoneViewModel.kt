package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.MilestoneBean
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/8 15:17
 */
class MilestoneViewModel(application: Application): BaseViewModel(application) {

    var dataObserver = MutableLiveData<List<MilestoneBean>>()

    var mRecordId: Long = 0

    val rankRepository = RankRepository()

    fun loadData() {
        getData()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<MilestoneBean>>(getComposite()) {
                override fun onNext(t: List<MilestoneBean>) {
                    dataObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message?:""
                }
            })
    }

    private fun getData(): Observable<List<MilestoneBean>> {
        return Observable.create { e ->
            val result = mutableListOf<MilestoneBean>()
            // round在数据表中并非实际意义上的升序，因此要波折一番对其进行转换排序
            val items = getDatabase().getMatchDao().getRecordWinMatchItems(mRecordId).sortedWith(
                compareBy({it.period}, {it.orderInPeriod}, {MatchConstants.getRoundSortValue(it.bean.round)})
            )
            // 整百数的胜场
            for (i in 99..items.size step 100) {
                val winIndex = "${i + 1}胜"
                val item = items[i]
                val match = getDatabase().getMatchDao().getMatchByPeriodId(item.bean.matchId)
                val period = "P${item.period}-W${item.orderInPeriod}"
                val bean = MilestoneBean(match, item.bean, winIndex, "", period, "", "")
                result.add(bean)
                getDatabase().getMatchDao().getMatchRecord(item.bean.id, mRecordId)?.let { mr ->
                    bean.rankSeed = if (mr.bean.recordSeed?:0 > 0) {
                        "R ${mr.bean.recordRank}/[${mr.bean.recordSeed}]"
                    }
                    else {
                        "R ${mr.bean.recordRank}"
                    }
                }
                getDatabase().getMatchDao().getMatchRecordCpt(item.bean.id, mRecordId)?.let { mr ->
                    bean.cptRankSeed = if (mr.bean.recordSeed?:0 > 0) {
                        "R ${mr.bean.recordRank}/[${mr.bean.recordSeed}]"
                    }
                    else {
                        "R ${mr.bean.recordRank}"
                    }
                    mr.record?.let { record ->
                        bean.cptName = record.name?:""
                        bean.cptImageUrl = ImageProvider.getRecordRandomPath(record.name, null)
                    }
                }
            }
            result.reverse()
            e.onNext(result)
            e.onComplete()
        }
    }
}