package com.king.app.coolg_kt.page.match.draw

import com.king.app.coolg_kt.CoolApplication
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.entity.match.Match
import java.util.*
import kotlin.math.abs

/**
 * @description:
 * GS、Master Final、GM1000规格较高，暂定严格限制排位，其他级别可以拓宽排位范围
 * @author：Jing
 * @date: 2021/1/10 17:33
 */
abstract class DrawPlan(var list: List<RankRecord>, var match: Match) {

    val database = CoolApplication.instance.database!!

    var seed: Int = 0
    var seedList = listOf<RankRecord>()
    var directInUnSeed: Int = 0
    var directInUnSeedList = listOf<RankRecord>()
    var qualify: Int = 0
    var qualifyList = listOf<RankRecord>()

    fun execute() {
        calcSeed()
        calcDirectInUnSeed()
        calcQualify()
    }

    abstract fun calcSeed()

    abstract fun calcDirectInUnSeed()

    abstract fun calcQualify()

}
class GrandSlamPlan(list: List<RankRecord>, match: Match): DrawPlan(list, match) {

    override fun calcSeed() {
        seed = 32
        seedList = list.take(seed)
    }

    override fun calcDirectInUnSeed() {
        // gs没有bye
        directInUnSeed = match.draws - seed - match.qualifyDraws
        directInUnSeedList = list.subList(seed, seed + directInUnSeed)
    }

    /**
     * 32 q, 3 rounds, 32*2*2*2 = 256
     */
    override fun calcQualify() {
        qualify = 256
        qualifyList = list.subList(seed + directInUnSeed, seed + directInUnSeed + qualify)
    }

}

/**
 * GM1000，Top30 4站128签强制，其他6站选4站强制
 */
class GM1000Plan(list: List<RankRecord>, match: Match): DrawPlan(list, match) {

    override fun calcSeed() {
        seed = if (match.byeDraws < 16) 16 else match.byeDraws
        // 强制
        if (match.draws == 128) {
            seedList = list.take(seed)
        }
        else {
            // TODO 6选4的逻辑太复杂，先按照强制处理
            seedList = list.take(seed)
        }
    }

    override fun calcDirectInUnSeed() {
        directInUnSeed = match.draws - seed - match.byeDraws - match.qualifyDraws
        directInUnSeedList = list.subList(seed, seed + directInUnSeed)
    }

    /**
     * qualify拓展1倍，后50个名额扩展3倍进行shuffle
     */
    override fun calcQualify() {
        qualify = match.qualifyDraws * 8
        val forsure = qualify - 50
        val tempList = mutableListOf<RankRecord>()
        val directIn = seed + directInUnSeed
        tempList.addAll(list.subList(directIn, directIn + forsure))
        // 后50个名额扩展3倍进行shuffle
        val extend = list.subList(directIn + forsure, directIn + forsure + 150).shuffled().take(50)
        tempList.addAll(extend)
        qualifyList = tempList
    }
}

/**
 * GM500，Top30 至少参加2站，至少8个种子
 */
class GM500Plan(list: List<RankRecord>, match: Match): DrawPlan(list, match) {

    val random = Random()

    override fun calcSeed() {
        seed = if (match.byeDraws < 8) 8 else match.byeDraws
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in list.indices) {
            // top10只有五分之一几率参加
            if (i < 10 && abs(random.nextInt()) % 5 == 1) {
                resultSeeds.add(list[i])
            }
            // top11-20只有3分之一几率参加
            else if (i < 20 && abs(random.nextInt()) % 3 == 1) {
                resultSeeds.add(list[i])
            }
            // top21-50只有2分之一几率参加
            else if (i < 50 && abs(random.nextInt()) % 2 == 1) {
                resultSeeds.add(list[i])
            }
            else {
                resultSeeds.add(list[i])
            }
            if (resultSeeds.size == seed) {
                break
            }
        }
        seedList = resultSeeds
    }

    override fun calcDirectInUnSeed() {
        directInUnSeed = match.draws - seed - match.byeDraws - match.qualifyDraws
        val seedEnd = seedList.last().rank
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in seedEnd until list.size) {
            // top10只有五分之一几率参加
            if (i < 10 && abs(random.nextInt()) % 5 == 1) {
                resultSeeds.add(list[i])
            }
            // top11-20只有3分之一几率参加
            else if (i < 20 && abs(random.nextInt()) % 3 == 1) {
                resultSeeds.add(list[i])
            }
            // top20以后都只有2分之一几率参加
            else if (abs(random.nextInt()) % 2 == 1) {
                resultSeeds.add(list[i])
            }
            else {
                resultSeeds.add(list[i])
            }
            if (resultSeeds.size == directInUnSeed) {
                break
            }
        }
        directInUnSeedList = resultSeeds
    }

    /**
     * qualify排名放宽到500
     */
    override fun calcQualify() {
        qualify = match.qualifyDraws * 8
        val directInEnd = directInUnSeedList.last().rank
        val limitList = list.subList(directInEnd, 500)
        qualifyList = limitList.shuffled().take(qualify)
    }
}


/**
 * GM500，Top30 至少参加2站，至少8个种子
 */
class GM250Plan(list: List<RankRecord>, match: Match): DrawPlan(list, match) {

    val random = Random()

    override fun calcSeed() {
        seed = if (match.byeDraws < 8) 8 else match.byeDraws
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in list.indices) {
            // top10只有十分之一几率参加
            if (i < 10 && abs(random.nextInt()) % 10 == 1) {
                resultSeeds.add(list[i])
            }
            // top11-20只有5分之一几率参加
            else if (i < 20 && abs(random.nextInt()) % 5 == 1) {
                resultSeeds.add(list[i])
            }
            // top21-50只有3分之一几率参加
            else if (i < 50 && abs(random.nextInt()) % 3 == 1) {
                resultSeeds.add(list[i])
            }
            else {
                resultSeeds.add(list[i])
            }
            if (resultSeeds.size == seed) {
                break
            }
        }
        seedList = resultSeeds
    }

    override fun calcDirectInUnSeed() {
        directInUnSeed = match.draws - seed - match.byeDraws - match.qualifyDraws
        val seedEnd = seedList.last().rank
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in seedEnd until list.size) {
            // top10只有五分之一几率参加
            if (i < 10 && abs(random.nextInt()) % 5 == 1) {
                resultSeeds.add(list[i])
            }
            // top11-20只有3分之一几率参加
            else if (i < 20 && abs(random.nextInt()) % 3 == 1) {
                resultSeeds.add(list[i])
            }
            // top20以后都只有2分之一几率参加
            else if (abs(random.nextInt()) % 2 == 1) {
                resultSeeds.add(list[i])
            }
            else {
                resultSeeds.add(list[i])
            }
            if (resultSeeds.size == directInUnSeed) {
                break
            }
        }
        directInUnSeedList = resultSeeds
    }

    /**
     * qualify排名放宽到500
     */
    override fun calcQualify() {
        qualify = match.qualifyDraws * 8
        val directInEnd = directInUnSeedList.last().rank
        val limitList = list.subList(directInEnd, 500)
        qualifyList = limitList.shuffled().take(qualify)
    }
}