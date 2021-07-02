package com.king.app.coolg_kt.page.match.list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.MatchItemGroup
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:08
 */
class MatchListViewModel(application: Application): BaseViewModel(application) {

    var matchesObserver = MutableLiveData<List<Any>>()
    var originList = listOf<Match>()

    fun loadMatches() {
        var list = getDatabase().getMatchDao().getAllMatchesByOrder()
        list.forEach {
            if (SettingProperty.isDemoImageMode()) {
                it.imgUrl = ImageProvider.getRandomDemoImage(-1, null)?:""
            }
        }
        originList = list
        matchesObserver.value = list
    }

    fun jumpTo(): Int {
        RankRepository().getCompletedPeriodPack()?.matchPeriod?.let {
            matchesObserver.value?.indexOfFirst { match ->
                match is Match && match.orderInPeriod == it.orderInPeriod
            }?.let { index ->
                if (index != -1) {
                    return index
                }
            }
        }
        return 0
    }

    fun deleteMatch(bean: Match) {
        getDatabase().getMatchDao().deleteMatch(bean)
    }

    fun groupByLevel() {
        val levelMap = mutableMapOf<Int, MutableList<Match>>()
        originList.forEach {
            var items = levelMap[it.level]?: mutableListOf()
            levelMap[it.level] = items
            items.add(it)
        }
        val result = mutableListOf<Any>()
        val levels = levelMap.keys.sorted()
        levels.forEach {
            val title = "${MatchConstants.MATCH_LEVEL[it]}(${levelMap[it]!!.size})"
            val head = MatchItemGroup(title, it)
            result.add(head)
            levelMap[it]!!.forEach { item ->
                result.add(item)
            }
        }
        matchesObserver.value = result
    }

    fun groupByWeek() {
        matchesObserver.value = originList
    }
}