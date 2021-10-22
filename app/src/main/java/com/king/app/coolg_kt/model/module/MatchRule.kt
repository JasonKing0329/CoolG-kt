package com.king.app.coolg_kt.model.module

import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.FinalScore

/**
 * Desc: match rules
 * @author：Jing Yang
 * @date: 2021/10/22 14:59
 */
class MatchRule {

    /**
     * 解决Final，相同胜场包括连环套的排名
     */
    fun resolveFinalCircle(firstRound: MutableList<DrawItem>, scoreList: MutableList<FinalScore>) {
        var i = 0
        var result = mutableListOf<FinalScore>()
        while (i < scoreList.size) {
            var win = scoreList[i].win
            var sameWins = mutableListOf<Int>()
            for (j in i + 1 until scoreList.size) {
                if (scoreList[j].win == win) {
                    sameWins.add(j)
                }
                else {
                    break
                }
            }
            if (sameWins.size > 0) {
                sameWins.add(0, i)
                i += sameWins.size
                var sameScores = scoreList.subList(sameWins.first(), sameWins.last() + 1)
                // 只可能出现2 3 5的连环套局面
                if (sameScores.size == 2) {
                    // two的情况肯定可以直接通过胜负判断
                    resolveTwo(firstRound, sameScores)
                }
                else if (sameScores.size == 3) {
                    // three的情况，即可能是真连环套，也可能会有肯定的第一名或第三名
                    resolveThree(firstRound, sameScores)
                }
                else if (sameScores.size == 5) {
                    // 肯定是22222的胜场，且互相真连环套，直接按record排，不使用先决出第一再比剩下
                    sameScores.sortBy { it.record.countRecord!!.rank }
                }
                result.addAll(sameScores)
            }
            else {
                result.add(scoreList[i])
                i ++
            }
        }
        for (i in 0 until scoreList.size) {
            scoreList[i] = result[i]
        }
    }

    data class ResolveIndex (
        var indexInList: Int,
        var value: Int
    )

    /**
     * 解决3人连环的局面
     * 胜场数可能为333, 222, 111，均遵循一个规则
     * 先决出第一（按互相胜负权值或record排名）
     * 剩下的两个用resolveTwo的规则
     */
    private fun resolveThree(firstRound: MutableList<DrawItem>, sameScores: MutableList<FinalScore>) {
        var result = mutableListOf<FinalScore>()
        // 先比较互相胜场的累计（与4人组3人连环套不一样，5人组的3人连环套是可能
        var value1 = ResolveIndex(0, 0)
        var value2 = ResolveIndex(1, 0)
        var value3 = ResolveIndex(2, 0)
        var result12 = compareTwoResult(firstRound, sameScores[0].record.bean.id!!, sameScores[1].record.bean.id!!)
        if (result12 == 1) value1.value ++ else value2.value ++
        var result23 = compareTwoResult(firstRound, sameScores[1].record.bean.id!!, sameScores[2].record.bean.id!!)
        if (result23 == 1) value2.value ++ else value3.value ++
        var result13 = compareTwoResult(firstRound, sameScores[0].record.bean.id!!, sameScores[2].record.bean.id!!)
        if (result13 == 1) value1.value ++ else value3.value ++
        var list = listOf(value1, value2, value3)
        var sorted = list.sortedByDescending { it.value }

        var first: FinalScore? = null
        var restTwo = mutableListOf<FinalScore>()
        // three规则为先角逐出第一名，剩余的两名进入two规则
        if (sorted[0].value == sorted[1].value) {
            // 222, 胜场全相同，第一名为record rank最高
            if (sorted[0].value == sorted[1].value && sorted[0].value == sorted[2].value) {
                first = sameScores.minBy { it.record.countRecord!!.rank }
                restTwo = sameScores.filter { it != first }.toMutableList()
            }
            // 221, 22进行比较，1直接第三名
            else {
                if (compareTwoResult(firstRound, sameScores[sorted[0].indexInList].record.bean.id!!, sameScores[sorted[1].indexInList].record.bean.id!!) == 1) {
                    first = sameScores[sorted[0].indexInList]
                    restTwo.add(sameScores[sorted[1].indexInList])
                    restTwo.add(sameScores[sorted[2].indexInList])
                }
                else {
                    first = sameScores[sorted[1].indexInList]
                    restTwo.add(sameScores[sorted[0].indexInList])
                    restTwo.add(sameScores[sorted[2].indexInList])
                }
            }
        }
        // 第一名确定
        else {
            first = sameScores[sorted[0].indexInList]
            restTwo.add(sameScores[sorted[1].indexInList])
            restTwo.add(sameScores[sorted[2].indexInList])
        }
        result.add(first!!)
        resolveTwo(firstRound, restTwo)
        result.addAll(restTwo)

        for (i in 0 until sameScores.size) {
            sameScores[i] = result[i]
        }
    }

    /**
     * 两个record间直接比较胜负关系
     */
    private fun resolveTwo(firstRound: MutableList<DrawItem>, sameScores: MutableList<FinalScore>) {
        var first = sameScores.first().record.bean.id!!
        var second = sameScores.last().record.bean.id!!
        var result = compareTwoResult(firstRound, first, second)
        if (result < 0) {
            sameScores.reverse()
        }
    }

    /**
     * 比较胜负关系
     */
    private fun compareTwoResult(firstRound: MutableList<DrawItem>, firstId: Long, secondId: Long): Int {
        firstRound.firstOrNull {
            var record1Id = it.matchRecord1?.bean?.recordId
            var record2Id = it.matchRecord2?.bean?.recordId
            (record1Id == firstId || record1Id == secondId) && (record2Id == firstId || record2Id == secondId)
        }?.let {
            return if (it.winner?.bean?.recordId == firstId) {
                1
            } else {
                -1;
            }
        }
        return 0
    }

}