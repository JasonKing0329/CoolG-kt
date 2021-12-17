package com.king.app.coolg_kt.page.match.season

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.MatchPeriodTitle
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.gdb.data.entity.match.MatchPeriod
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 23:29
 */
class SeasonViewModel(application: Application): BaseViewModel(application) {

    var matchesObserver = MutableLiveData<MutableList<Any>>()

    val rankRepository = RankRepository()

    fun loadMatches() {
        toClassifyMatches()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<Any>>(getComposite()){
                override fun onNext(t: MutableList<Any>?) {
                    matchesObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = "error: $e"
                }
            })
    }

    fun insertOrUpdate(match: MatchPeriod) {
        if (match.id == 0.toLong()) {
            val list = listOf(match)
            getDatabase().getMatchDao().insertMatchPeriods(list)
        }
        else{
            getDatabase().getMatchDao().updateMatchPeriod(match)
        }
        loadMatches()
    }

    fun deleteMatch(bean: MatchPeriod) {
        getDatabase().getMatchDao().deleteMatchPeriod(bean)
        loadMatches()
    }

    /**
     * @param list 已按period, orderInPeriod降序排列
     */
    private fun toClassifyMatches(): Observable<MutableList<Any>> = Observable.create {
        val list = getDatabase().getMatchDao().getAllMatchPeriodsOrdered()
        var result = mutableListOf<Any>()
        var lastPeriod: MatchPeriodTitle? = null
        for (match in list) {
            var title = lastPeriod
            if (title == null || match.bean.period != title.period) {
                title = MatchPeriodTitle(match.bean.period)
                title.endDate = FormatUtil.formatDate(match.bean.date)
                result.add(title)

                lastPeriod = title
            }
            // 确保startDate肯定会最后会设置为该period的第一个
            title.startDate = FormatUtil.formatDate(match.bean.date)

            result.add(match)
        }

        it.onNext(result)
        it.onComplete()
    }

    fun isRankCreated(): Boolean {
        rankRepository.getCompletedPeriodPack()?.matchPeriod?.apply {
            val result = rankRepository.isLastCompletedRankCreated()
            if (!result) {
                messageObserver.value = "The rank list of P$period-W$orderInPeriod haven't been created-"
            }
            return result
        }
        return true
    }
}