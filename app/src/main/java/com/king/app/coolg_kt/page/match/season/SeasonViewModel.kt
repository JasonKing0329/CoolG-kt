package com.king.app.coolg_kt.page.match.season

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.gdb.data.entity.match.MatchPeriod
import com.king.app.gdb.data.relation.MatchPeriodWrap
import kotlinx.coroutines.Job

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

    var imageChanged = MutableLiveData<TimeWasteRange>()

    var showPeriod = endPeriod

    var mFilterLevel = MatchConstants.MATCH_LEVEL_ALL

    var urlMap = mutableMapOf<Long, String?>()

    var loadJob: Job? = null

    fun loadMatches() {
        periodText.set("Period $showPeriod")
        loadJob?.cancel()
        loadJob = basicAndTimeWaste(
            blockBasic = {
                urlMap.clear()
                // list按降序排列
                var list = getDatabase().getMatchDao().getMatchPeriodsOrdered(showPeriod)
                val startTime = FormatUtil.formatDate(list.lastOrNull()?.bean?.date?:0)
                val endTime = FormatUtil.formatDate(list.firstOrNull()?.bean?.date?:0)
                periodDateText.set("$startTime To $endTime")
                return@basicAndTimeWaste if (mFilterLevel != MatchConstants.MATCH_LEVEL_ALL) {
                    list.filter { it.match.level == mFilterLevel }
                }
                else {
                    list
                }
            },
            onCompleteBasic = {
                checkLastNext()
                matchesObserver.value = it
            },
            blockWaste = { _, it ->  handleItem(it) },
            5,
            onWasteRangeChanged = {start, count -> imageChanged.value = TimeWasteRange(start, count) },
            withBasicLoading = false
        )
    }

    private fun handleItem(it: MatchPeriodWrap) {
        getDatabase().getMatchDao().queryMatchWinner(it.bean.id, MatchConstants.ROUND_ID_F)?.apply {
            var url = urlMap[id]
            if (url == null) {
                it.imageUrl = ImageProvider.getRecordRandomPath(name, null)
                urlMap[id!!] = it.imageUrl
            }
            else {
                it.imageUrl = url
            }
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
        rankRepository.deleteMatchPeriod(bean)
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

    fun filterByLevel(level: Int) {
        if (level != mFilterLevel) {
            mFilterLevel = level
            loadMatches()
        }
    }

}