package com.king.app.coolg_kt.page.match.season

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.gdb.data.entity.match.MatchPeriod
import com.king.app.gdb.data.relation.MatchPeriodWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 23:29
 */
class SeasonViewModel(application: Application): BaseViewModel(application) {

    var matchesObserver = MutableLiveData<List<MatchPeriodWrap>>()

    var periodLastVisibility = ObservableInt(View.GONE)
    var periodNextVisibility = ObservableInt(View.GONE)
    var periodText = ObservableField<String>()
    var periodDateText = ObservableField<String>()

    val rankRepository = RankRepository()

    var endPeriod = rankRepository.getRTFPeriodPack().startPeriod

    var showPeriod = endPeriod

    fun loadMatches() {
        periodText.set("Period $showPeriod")
        launchSingle(
            {
                // list按降序排列
                val list = getDatabase().getMatchDao().getMatchPeriodsOrdered(showPeriod)
                val startTime = FormatUtil.formatDate(list.lastOrNull()?.bean?.date?:0)
                val endTime = FormatUtil.formatDate(list.firstOrNull()?.bean?.date?:0)
                periodDateText.set("$startTime To $endTime")
                list
            },
            withLoading = true
        ) {
            checkLastNext()
            matchesObserver.value = it
        }
    }

    fun insertOrUpdate(match: MatchPeriod) {
        if (match.id == 0.toLong()) {
            val list = listOf(match)
            getDatabase().getMatchDao().insertMatchPeriods(list)
            // 新增如果创建了新的period，直接切换到新的period
            val end = rankRepository.getRTFPeriodPack().startPeriod
            if (end != endPeriod) {
                endPeriod = end
                showPeriod = endPeriod
            }
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

    fun nextPeriod() {
        showPeriod ++
        loadMatches()
    }

    fun lastPeriod() {
        showPeriod --
        loadMatches()
    }

    fun targetPeriod(period: Int) {
        showPeriod = period
        loadMatches()
    }

    private fun checkLastNext() {
        // last
        if (showPeriod + 1 > endPeriod) {
            periodNextVisibility.set(View.INVISIBLE)
        }
        else {
            periodNextVisibility.set(View.VISIBLE)
        }
        if (showPeriod - 1 < 1) {
            periodLastVisibility.set(View.INVISIBLE)
        }
        else {
            periodLastVisibility.set(View.VISIBLE)
        }
    }

}