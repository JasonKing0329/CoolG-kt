package com.king.app.coolg_kt.page.match.list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.bean.MatchListItem
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.DrawRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.MatchItemGroup
import com.king.app.gdb.data.entity.ScorePlan
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:08
 */
class MatchListViewModel(application: Application): BaseViewModel(application) {

    var matchesObserver = MutableLiveData<List<Any>>()
    var originList = listOf<MatchListItem>()
    var drawRepository = DrawRepository()
    var gson = Gson()

    fun loadMatches() {
        launchMain {
            val result = mutableListOf<MatchListItem>()
            val parent = getDatabase().getFavorDao().getRecordOrderByName(AppConstants.ORDER_STUDIO_NAME)
            getDatabase().getMatchDao().getAllMatchesByOrder()
                .mapTo(result) {
                    val item = MatchListItem(it, 0)
                    it.imgUrl = ImageProvider.parseCoverUrl(it.imgUrl)?:""
                    parent?.let { order ->
                        var studio = getDatabase().getFavorDao().getStudioByName(it.name, order.id!!)
                        studio?.let { s ->
                            item.studioCount = s.number
                        }
                    }
                    item
                }
            originList = result
            matchesObserver.value = result
        }
    }

    fun jumpTo(): Int {
        RankRepository().getCompletedPeriodPack()?.matchPeriod?.let {
            matchesObserver.value?.indexOfFirst { match ->
                match is MatchListItem && match.match.orderInPeriod == it.orderInPeriod
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
        val levelMap = mutableMapOf<Int, MutableList<MatchListItem>>()
        originList.forEach {
            var items = levelMap[it.match.level]?: mutableListOf()
            levelMap[it.match.level] = items
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

    /**
     * only switch orderInPeriod
     */
    fun switchWeek(from: MatchListItem, toMatchId: Long) {
        launchMain {
            getDatabase().getMatchDao().getMatch(toMatchId)?.apply {
                val toWeek = orderInPeriod
                orderInPeriod = from.match.orderInPeriod
                from.match.orderInPeriod = toWeek
                getDatabase().runInTransaction {
                    getDatabase().getMatchDao().updateMatch(this)
                    getDatabase().getMatchDao().updateMatch(from.match)
                }
                loadMatches()
            }
        }
    }

    /**
     * switch orderInPeriod, draws, score plan
     */
    fun switchWeekAndDraws(from: MatchListItem, toMatchId: Long) {
        launchMain {
            getDatabase().getMatchDao().getMatch(toMatchId)?.apply {
                // score plan有历史记录的作用，因此交换其实是copy对方的plan作为新周期的plan
                // getDefaultScorePlan跟draws有关，需要放在交换draws之前
                var fromPlan = drawRepository.getScorePlan(from.match.id)
                if (fromPlan == null) {
                    fromPlan = drawRepository.getDefaultScorePlan(from.match)
                }
                var toPlan = drawRepository.getScorePlan(id)
                if (toPlan == null) {
                    toPlan = drawRepository.getDefaultScorePlan(this)
                }
                val toWeek = orderInPeriod
                val toDraw = draws
                val toQualify = qualifyDraws
                val toBye = byeDraws
                // 交换match里的字段
                orderInPeriod = from.match.orderInPeriod
                draws = from.match.draws
                qualifyDraws = from.match.qualifyDraws
                byeDraws = from.match.byeDraws
                from.match.orderInPeriod = toWeek
                from.match.draws = toDraw
                from.match.qualifyDraws = toQualify
                from.match.byeDraws = toBye
                getDatabase().runInTransaction {
                    getDatabase().getMatchDao().updateMatch(this)
                    getDatabase().getMatchDao().updateMatch(from.match)
                    val period = drawRepository.getCurrentPeriodPack().startPeriod
                    fromPlan?.let { plan ->
                        // 赋值给toMatch
                        plan.matchId = id
                        plan.period = period
                        getDatabase().getMatchDao().insertOrReplaceScorePlan(ScorePlan(id, period, gson.toJson(plan)))
                    }
                    toPlan?.let { plan ->
                        // 赋值给fromMatch
                        plan.matchId = from.match.id
                        plan.period = period
                        getDatabase().getMatchDao().insertOrReplaceScorePlan(ScorePlan(from.match.id, period, gson.toJson(plan)))
                    }
                }
                loadMatches()
            }
        }
    }

    /**
     * switch name and imgUrl
     */
    fun switchStudio(from: MatchListItem, toMatchId: Long) {
        launchMain {
            getDatabase().getMatchDao().getMatch(toMatchId)?.apply {
                val toName = name
                val toUrl = imgUrl
                name = from.match.name
                imgUrl = from.match.imgUrl
                from.match.name = toName
                from.match.imgUrl = toUrl
                getDatabase().runInTransaction {
                    getDatabase().getMatchDao().updateMatch(this)
                    getDatabase().getMatchDao().updateMatch(from.match)
                }
                loadMatches()
            }
        }
    }
}