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