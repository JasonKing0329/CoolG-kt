package com.king.app.coolg_kt.page.match.detail

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.gdb.data.entity.match.MatchRankRecord

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/10 9:06
 */
class BasicViewModel(application: Application): BaseViewModel(application) {

    private var rankRepository = RankRepository()

    var rankChartList = MutableLiveData<List<MatchRankRecord>>()

    fun loadFinalRanks(recordId: Long) {
        launchSingleThread(
            { rankRepository.getRecordFinalRanks(recordId) },
            withLoading = false
        ) {
            rankChartList.value = it
        }
    }
}