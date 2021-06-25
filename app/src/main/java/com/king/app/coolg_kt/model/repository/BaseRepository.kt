package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.PeriodPack
import com.king.app.coolg_kt.page.match.ShowPeriod
import com.king.app.gdb.data.AppDatabase
import com.king.app.gdb.data.entity.match.MatchPeriod

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/6 17:17
 */
abstract class BaseRepository {

    fun getDatabase(): AppDatabase = CoolApplication.instance.database!!

    /**
     * 获取创建draw应参照的rank period
     * 创建draw意味着最后一个matchPeriod是当前赛事，上一个才是创建过排名的period
     */
    fun getRankPeriodToDraw(): MatchPeriod? {
        val last = getDatabase().getMatchDao().getLastMatchPeriod()
        last?.let {
            var period = it.period
            var orderInPeriod = it.orderInPeriod - 1
            if (orderInPeriod == 0) {
                period -= 1
                orderInPeriod = MatchConstants.MAX_ORDER_IN_PERIOD
            }
            if (period > 0) {
                return getDatabase().getMatchDao().getMatchPeriods(period, orderInPeriod).firstOrNull()
            }
        }
        return null
    }

    /**
     * 已完成赛事的积分周期
     */
    fun getCompletedPeriodPack(): PeriodPack {
        var bean = PeriodPack()
        // 取最近一站已完成的为截至日期
        var last = getDatabase().getMatchDao().getLastCompletedMatchPeriod()
        last?.let { period ->
            bean.matchPeriod = period
            bean.endPeriod = period.period
            bean.endPIO = period.orderInPeriod
            // 确认起始站有3种情况
            // 当前结束的orderInPeriod等于45或46（46为Final）,计分周期为 1 to orderInPeriod
            // 当前结束的orderInPeriod小于45，计分周期为 last(orderInPeriod + 1) to orderInPeriod
            bean.startPeriod = 0
            bean.startPIO = 0
            if (period.orderInPeriod == MatchConstants.MAX_ORDER_IN_PERIOD - 1 || period.orderInPeriod == MatchConstants.MAX_ORDER_IN_PERIOD) {
                bean.startPeriod = period.period
                bean.startPIO = 1
            } else {
                bean.startPeriod = period.period - 1
                bean.startPIO = period.orderInPeriod + 1
            }
        }
        return bean
    }

    /**
     * 确认当前排名的积分周期
     */
    fun getRankPeriodPack(): PeriodPack {
        var bean = PeriodPack()
        // 取最近一站已完成的为截至日期
        var last = getDatabase().getMatchDao().getLastMatchPeriod()
        last?.let { period ->
            bean.matchPeriod = period
            bean.endPeriod = period.period
            bean.endPIO = period.orderInPeriod
            // 确认起始站有3种情况
            // 当前结束的orderInPeriod等于45或46（46为Final）,计分周期为 1 to orderInPeriod
            // 当前结束的orderInPeriod小于45，计分周期为 last(orderInPeriod + 1) to orderInPeriod
            bean.startPeriod = 0
            bean.startPIO = 0
            if (period.orderInPeriod == MatchConstants.MAX_ORDER_IN_PERIOD - 1 || period.orderInPeriod == MatchConstants.MAX_ORDER_IN_PERIOD) {
                bean.startPeriod = period.period
                bean.startPIO = 1
            } else {
                bean.startPeriod = period.period - 1
                bean.startPIO = period.orderInPeriod + 1
            }
        }
        return bean
    }

    /**
     * 确认RaceToFinal的积分周期
     */
    fun getRTFPeriodPack(): PeriodPack {
        var bean = PeriodPack()
        var last = getDatabase().getMatchDao().getLastMatchPeriod()
        last?.let { period ->
            bean.matchPeriod = period
            bean.endPeriod = period.period
            bean.endPIO = period.orderInPeriod
            bean.startPeriod = period.period
            bean.startPIO = 1
        }
        return bean
    }

    /**
     * All time
     */
    fun getAllTimePeriodPack(): PeriodPack {
        var bean = PeriodPack()
        var last = getDatabase().getMatchDao().getLastMatchPeriod()
        last?.let { period ->
            bean.matchPeriod = period
            bean.endPeriod = period.period
            bean.endPIO = period.orderInPeriod
            bean.startPeriod = 1
            bean.startPIO = 1
        }
        return bean
    }

    /**
     * Specific Period
     */
    fun getSpecificPeriodPack(period: Int): PeriodPack {
        var bean = PeriodPack()
        bean.endPeriod = period
        bean.endPIO = MatchConstants.MAX_ORDER_IN_PERIOD
        bean.startPeriod = period
        bean.startPIO = 1
        return bean
    }

    fun getNextPeriod(showPeriod: ShowPeriod): ShowPeriod {
        var period = showPeriod.period
        var orderInPeriod = showPeriod.orderInPeriod + 1
        if (orderInPeriod > MatchConstants.MAX_ORDER_IN_PERIOD) {
            period += 1
            orderInPeriod = 1
        }
        return ShowPeriod(period, orderInPeriod)
    }

    fun getNextPeriodFinal(periodFinal: ShowPeriod): ShowPeriod {
        return ShowPeriod(periodFinal.period + 1, periodFinal.orderInPeriod)
    }

    fun getLastPeriod(showPeriod: ShowPeriod): ShowPeriod {
        var period = showPeriod.period
        var orderInPeriod = showPeriod.orderInPeriod - 1
        if (orderInPeriod == 0) {
            period -= 1
            orderInPeriod = MatchConstants.MAX_ORDER_IN_PERIOD
        }
        return ShowPeriod(period, orderInPeriod)
    }

    fun getLastPeriodFinal(periodFinal: ShowPeriod): ShowPeriod {
        return ShowPeriod(periodFinal.period - 1, periodFinal.orderInPeriod)
    }

    fun getAllPeriods(): List<Int> {
        val list = mutableListOf<Int>()
        // 取最近一站已完成的为截至日期
        var last = getDatabase().getMatchDao().getLastMatchPeriod()
        last?.let {
            for (i in 1..it.period) {
                list.add(i)
            }
        }
        return list
    }
}