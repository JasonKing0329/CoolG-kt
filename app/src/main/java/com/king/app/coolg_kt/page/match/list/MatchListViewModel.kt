package com.king.app.coolg_kt.page.match.list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:08
 */
class MatchListViewModel(application: Application): BaseViewModel(application) {

    var matchesObserver = MutableLiveData<List<Match>>()

    fun loadMatches() {
        matchesObserver.value = getDatabase().getMatchDao().getAllMatchesByOrder()
    }

    fun deleteMatch(bean: Match) {
        getDatabase().getMatchDao().deleteMatch(bean)
    }
}