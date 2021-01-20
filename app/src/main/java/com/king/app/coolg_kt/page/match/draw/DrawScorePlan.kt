package com.king.app.coolg_kt.page.match.draw

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.gdb.data.relation.MatchPeriodWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 17:33
 */
abstract class DrawScorePlan(var match: MatchPeriodWrap) {

    val database = CoolApplication.instance.database!!

    abstract fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int
}

class GrandSlamScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        return when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 5 else 0
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 10 else 5
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 25 else 10
            MatchConstants.ROUND_ID_128 -> {
                if (isWinner) {
                    if (isQualify) 45 else 10
                }
                else {
                    if (isQualify) 25 else 10
                }
            }
            MatchConstants.ROUND_ID_64 -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_32 -> if (isWinner) 180 else 90
            MatchConstants.ROUND_ID_16 -> if (isWinner) 360 else 180
            MatchConstants.ROUND_ID_QF -> if (isWinner) 720 else 360
            MatchConstants.ROUND_ID_SF -> if (isWinner) 1200 else 720
            MatchConstants.ROUND_ID_F -> if (isWinner) 2000 else 1200
            else -> 0
        }
    }
}

/**
 * Final所有轮次都是累加积分方案，SF与F要在前序轮次的基础上再累计计算
 */
class FinalScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        return when(round) {
            MatchConstants.ROUND_ID_GROUP -> if (isWinner) 200 else 100
            MatchConstants.ROUND_ID_SF -> if (isWinner) 400 else 0
            MatchConstants.ROUND_ID_F -> if (isWinner) 500 else 0
            else -> 0
        }
    }
}

class GM1000ScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        return when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 5 else 0
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 10 else 5
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 25 else 10
            MatchConstants.ROUND_ID_128 -> {
                if (isWinner) {
                    if (isQualify) 35 else 10
                }
                else {
                    if (isQualify) 25 else 0
                }
            }
            MatchConstants.ROUND_ID_64 -> if (isWinner) 45 else {
                if (match.match.draws == 128) {
                    if (isQualify) 35 else 10
                }
                else {
                    if (isQualify) 25 else 5
                }
            }
            MatchConstants.ROUND_ID_32 -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_16 -> if (isWinner) 180 else 90
            MatchConstants.ROUND_ID_QF -> if (isWinner) 360 else 180
            MatchConstants.ROUND_ID_SF -> if (isWinner) 600 else 360
            MatchConstants.ROUND_ID_F -> if (isWinner) 1000 else 600
            else -> 0
        }
    }
}

class GM500ScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        return when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 4 else 0
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 8 else 4
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 20 else 8
            MatchConstants.ROUND_ID_64 -> {
                if (isWinner) {
                    if (isQualify) 25 else 10
                }
                else {
                    if (isQualify) 20 else 5
                }
            }
            MatchConstants.ROUND_ID_32 -> if (isWinner) 45 else {
                if (match.match.draws == 64) {
                    if (isQualify) 25 else 10
                }
                else {
                    if (isQualify) 20 else 5
                }
            }
            MatchConstants.ROUND_ID_16 -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_QF -> if (isWinner) 180 else 90
            MatchConstants.ROUND_ID_SF -> if (isWinner) 300 else 180
            MatchConstants.ROUND_ID_F -> if (isWinner) 500 else 300
            else -> 0
        }
    }
}

class GM250ScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        return when(round) {
            MatchConstants.ROUND_ID_Q1 -> if (isWinner) 2 else 0
            MatchConstants.ROUND_ID_Q2 -> if (isWinner) 5 else 2
            MatchConstants.ROUND_ID_Q3 -> if (isWinner) 12 else 5
            MatchConstants.ROUND_ID_32 -> if (isWinner) 20 else { if (isQualify) 12 else 5 }
            MatchConstants.ROUND_ID_16 -> if (isWinner) 45 else 20
            MatchConstants.ROUND_ID_QF -> if (isWinner) 90 else 45
            MatchConstants.ROUND_ID_SF -> if (isWinner) 150 else 90
            MatchConstants.ROUND_ID_F -> if (isWinner) 250 else 150
            else -> 0
        }
    }
}

class LowScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        if (match.match.draws == 64) {
            return when(round) {
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
            return when(round) {
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
    }
}

/**
 * 累加制
 */
class FinalDrawScorePlan(match: MatchPeriodWrap): DrawScorePlan(match) {

    override fun getRoundScore(round: Int, isWinner: Boolean, isQualify: Boolean): Int {
        return when(round) {
            MatchConstants.ROUND_ID_GROUP -> if (isWinner) 150 else 30
            MatchConstants.ROUND_ID_SF -> if (isWinner) 400 else 0
            MatchConstants.ROUND_ID_F -> if (isWinner) 500 else 0
            else -> 0
        }
    }
}