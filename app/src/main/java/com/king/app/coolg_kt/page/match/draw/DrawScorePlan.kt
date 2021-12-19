package com.king.app.coolg_kt.page.match.draw

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.page.match.DrawScore
import com.king.app.coolg_kt.page.match.ScoreItem
import com.king.app.gdb.data.relation.MatchPeriodWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 17:33
 */
abstract class DrawScorePlan(var match: MatchPeriodWrap) {

    val database = CoolApplication.instance.database!!

    abstract fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int

    companion object {

        fun defGrandSlamPlan(matchId: Long): DrawScore {
            var bean = DrawScore(matchId, mutableListOf())
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_F, 1200, 2000))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_SF, 720, 1200))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_QF, 360, 720))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_16, 180, 360))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 90, 180))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_64, 45, 90))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_128, 10, 45))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q3, 25, 40))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q2, 10, 25))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q1, 5, 10))
            return bean
        }

        fun defGM1000Plan(matchId: Long, draws: Int): DrawScore {
            var bean = DrawScore(matchId, mutableListOf())
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_F, 600, 1000))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_SF, 360, 600))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_QF, 180, 360))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_16, 90, 180))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 45, 90))
            if (draws == 128) {
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_64, 10, 45))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_128, 5, 10))
            }
            else {
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_64, 5, 45))
            }
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q3, 15, 30))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q2, 5, 15))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q1, 1, 5))
            return bean
        }

        fun defGM500Plan(matchId: Long, draws: Int): DrawScore {
            var bean = DrawScore(matchId, mutableListOf())
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_F, 300, 500))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_SF, 180, 300))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_QF, 90, 180))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_16, 45, 90))
            if (draws == 64) {
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 10, 45))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_64, 5, 10))
            }
            else {
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 5, 45))
            }
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q3, 12, 20))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q2, 4, 12))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q1, 1, 4))
            return bean
        }

        fun defGM250Plan(matchId: Long): DrawScore {
            var bean = DrawScore(matchId, mutableListOf())
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_F, 150, 250))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_SF, 90, 150))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_QF, 45, 90))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_16, 20, 45))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 5, 20))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q3, 7, 12))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q2, 2, 7))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q1, 1, 2))
            return bean
        }

        fun defLowPlan(matchId: Long, draws: Int): DrawScore {
            return if (draws == 64) {
                var bean = DrawScore(matchId, mutableListOf())
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_F, 75, 120))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_SF, 45, 75))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_QF, 25, 45))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_16, 12, 25))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 6, 12))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_64, 3, 6))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q3, 1, 5))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q2, 1, 2))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q1, 0, 1))
                bean
            }
            else {
                var bean = DrawScore(matchId, mutableListOf())
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_F, 48, 80))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_SF, 28, 48))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_QF, 14, 28))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_16, 6, 14))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 1, 6))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q3, 2, 3))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q2, 1, 2))
                bean.items.add(ScoreItem(MatchConstants.ROUND_ID_Q1, 0, 1))
                bean
            }
        }

        fun defMicroPlan(matchId: Long): DrawScore {
            var bean = DrawScore(matchId, mutableListOf())
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_F, 16, 25))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_SF, 10, 16))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_QF, 5, 10))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_16, 2, 5))
            bean.items.add(ScoreItem(MatchConstants.ROUND_ID_32, 0, 2))
            return bean
        }

    }
}

class BeanPlan(match: MatchPeriodWrap, var bean: DrawScore): DrawScorePlan(match) {
    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        findRound(round)?.let {
            var score = if (isQualify && it.qualifyItem != null) {
                if (isWinner) it.qualifyItem!!.scoreWin else it.qualifyItem!!.scoreLose
            }
            else {
                if (isWinner) it.scoreWin else it.scoreLose
            }
            if (isQualify && round >= MatchConstants.ROUND_ID_128 && round <= MatchConstants.ROUND_ID_F) {
                findRound(MatchConstants.ROUND_ID_Q3)?.let { q3 ->
                    score += q3.scoreWin
                }
            }
            return score
        }
        return 0
    }

    fun findRound(round: Int): ScoreItem? {
        return bean.items.firstOrNull{ it.round == round }
    }
}

class GrandSlamScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        var score = when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 10 else 5
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 25 else 10
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 40 else 25
            MatchConstants.ROUND_ID_128 -> if (isWinner) 45 else 10
            MatchConstants.ROUND_ID_64 -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_32 -> if (isWinner) 180 else 90
            MatchConstants.ROUND_ID_16 -> if (isWinner) 360 else 180
            MatchConstants.ROUND_ID_QF -> if (isWinner) 720 else 360
            MatchConstants.ROUND_ID_SF -> if (isWinner) 1200 else 720
            MatchConstants.ROUND_ID_F -> if (isWinner) 2000 else 1200
            else -> 0
        }
        if (isQualify && round >= MatchConstants.ROUND_ID_128 && round <= MatchConstants.ROUND_ID_F) {
            score += 40
        }
        return score
    }
}

class GM1000ScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        var score = when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 5 else 1
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 15 else 5
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 30 else 15
            MatchConstants.ROUND_ID_128 -> if (isWinner) 10 else 5
            MatchConstants.ROUND_ID_64 -> if (isWinner) 45 else {
                if (match.match.draws == 128) 10 else 5
            }
            MatchConstants.ROUND_ID_32 -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_16 -> if (isWinner) 180 else 90
            MatchConstants.ROUND_ID_QF -> if (isWinner) 360 else 180
            MatchConstants.ROUND_ID_SF -> if (isWinner) 600 else 360
            MatchConstants.ROUND_ID_F -> if (isWinner) 1000 else 600
            else -> 0
        }
        if (isQualify && round >= MatchConstants.ROUND_ID_128 && round <= MatchConstants.ROUND_ID_F) {
            score += 30
        }
        return score
    }
}

class GM500ScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        var score = when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 4 else 1
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 12 else 4
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 20 else 12
            MatchConstants.ROUND_ID_64 -> if (isWinner) 10 else 5
            MatchConstants.ROUND_ID_32 -> if (isWinner) 45 else {
                if (match.match.draws == 64) 10 else 5
            }
            MatchConstants.ROUND_ID_16 -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_QF -> if (isWinner) 180 else 90
            MatchConstants.ROUND_ID_SF -> if (isWinner) 300 else 180
            MatchConstants.ROUND_ID_F -> if (isWinner) 500 else 300
            else -> 0
        }
        if (isQualify && round >= MatchConstants.ROUND_ID_128 && round <= MatchConstants.ROUND_ID_F) {
            score += 20
        }
        return score
    }
}

class GM250ScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        var score = when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 2 else 0
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 7 else 2
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 12 else 7
            MatchConstants.ROUND_ID_32 -> if (isWinner) 20 else 5
            MatchConstants.ROUND_ID_16 -> if (isWinner) 45 else 20
            MatchConstants.ROUND_ID_QF -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_SF -> if (isWinner) 150 else 90
            MatchConstants.ROUND_ID_F -> if (isWinner) 250 else 150
            else -> 0
        }
        if (isQualify && round >= MatchConstants.ROUND_ID_128 && round <= MatchConstants.ROUND_ID_F) {
            score += 12
        }
        return score
    }
}

class LowScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        var score = if (match.match.draws == 64) {
            when(round) {
                MatchConstants.ROUND_ID_Q1 -> if (isWinner) 1 else 0
                MatchConstants.ROUND_ID_Q2 -> if (isWinner) 2 else 1
                MatchConstants.ROUND_ID_Q3 -> if (isWinner) 5 else 2
                MatchConstants.ROUND_ID_64 -> if (isWinner) 6 else if (isQualify) 5 else 0
                MatchConstants.ROUND_ID_32 -> if (isWinner) 12 else 6
                MatchConstants.ROUND_ID_16 -> if (isWinner) 25 else 12
                MatchConstants.ROUND_ID_QF -> if (isWinner) 45 else 25
                MatchConstants.ROUND_ID_SF -> if (isWinner) 75 else 45
                MatchConstants.ROUND_ID_F -> if (isWinner) 120 else 75
                else -> 0
            }
        }
        else {
            when(round) {
                MatchConstants.ROUND_ID_Q1 -> if (isWinner) 1 else 0
                MatchConstants.ROUND_ID_Q2 -> if (isWinner) 2 else 1
                MatchConstants.ROUND_ID_Q3 -> if (isWinner) 3 else 2
                MatchConstants.ROUND_ID_32 -> if (isWinner) 6 else if (isQualify) 3 else 0
                MatchConstants.ROUND_ID_16 -> if (isWinner) 14 else 6
                MatchConstants.ROUND_ID_QF -> if (isWinner) 28 else 14
                MatchConstants.ROUND_ID_SF -> if (isWinner) 48 else 28
                MatchConstants.ROUND_ID_F -> if (isWinner) 80 else 48
                else -> 0
            }
        }
        if (isQualify && round >= MatchConstants.ROUND_ID_128 && round <= MatchConstants.ROUND_ID_F) {
            score += if (match.match.draws == 64) 5 else 3
        }
        return score
    }
}

class MicroScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        var score = when(round) {
            MatchConstants.ROUND_ID_32 -> if (isWinner) 2 else 0
            MatchConstants.ROUND_ID_16 -> if (isWinner) 5 else 2
            MatchConstants.ROUND_ID_QF -> if (isWinner) 10 else 5
            MatchConstants.ROUND_ID_SF -> if (isWinner) 16 else 10
            MatchConstants.ROUND_ID_F -> if (isWinner) 25 else 16
            else -> 0
        }
        return score
    }
}

/**
 * 累加制
 */
class FinalDrawScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        return when(round) {
            MatchConstants.ROUND_ID_GROUP -> if (isWinner) 150 else 50
            MatchConstants.ROUND_ID_SF -> if (isWinner) 400 else 0
            MatchConstants.ROUND_ID_F -> if (isWinner) 500 else 0
            else -> 0
        }
    }
}