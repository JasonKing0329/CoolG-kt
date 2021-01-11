package com.king.app.coolg_kt.page.match.draw

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.page.match.DrawCell
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.entity.match.MatchRecord
import com.king.app.gdb.data.relation.MatchPeriodWrap
import java.util.*
import kotlin.math.abs

/**
 * @description:
 * GS、Master Final、GM1000严格按排位入围、确定种子
 * GM500范围拓宽到500以内有条件随机
 * GM250范围为500以后1500以内条件随机
 * @author：Jing
 * @date: 2021/1/10 17:33
 */
abstract class DrawPlan(var list: List<RankRecord>, var match: MatchPeriodWrap) {

    val database = CoolApplication.instance.database!!

    var seed: Int = 0
    var seedList = listOf<RankRecord>()
    var directInUnSeed: Int = 0
    var directInUnSeedList = listOf<RankRecord>()
    var qualify: Int = 0
    var qualifyList = listOf<RankRecord>()

    fun prepare() {
        calcSeed()
        setSeed()
        calcDirectInUnSeed()
        calcQualify()
    }

    abstract fun calcSeed()

    abstract fun calcDirectInUnSeed()

    abstract fun calcQualify()

    open fun setSeed() {
        // rank可能不连续，需要设置seed
        seedList.forEachIndexed { index, rankRecord -> rankRecord.seed = index + 1 }
    }

    fun arrangeMainDraw(): MutableList<DrawCell> {
        val draws = mutableListOf<DrawCell>()
        for (i in 0 until match.match.draws) {
            draws.add(DrawCell(null, 0))
        }
        createMainDraw(draws)
        return draws
    }

    abstract fun createMainDraw(draws: MutableList<DrawCell>)

    fun arrangeQualifyDraw(): MutableList<DrawCell> {
        val draws = mutableListOf<DrawCell>()
        for (i in 0 until qualify) {
            draws.add(DrawCell(null, 0))
        }
        createQualifyDraw(draws)
        return draws
    }

    open fun fillSeed(draws: MutableList<DrawCell>, index: Int, rankRecord: RankRecord) {
        draws[index].matchRecord = MatchRecord(0, match.bean.matchId, 0, rankRecord.recordId, rankRecord.rank, rankRecord.seed, 0)
        draws[index].type = 0
    }

    open fun fillNormal(draws: MutableList<DrawCell>, index: Int, rankRecord: RankRecord) {
        draws[index].matchRecord = MatchRecord(0, match.bean.matchId, 0, rankRecord.recordId, rankRecord.rank, 0, 0)
        draws[index].type = 0
    }

    open fun fillBye(draws: MutableList<DrawCell>, index: Int) {
        draws[index].type = 1
    }

    /**
     * arrange unseed and wildcard, qualify in main draw
     * call arrange seed and bye before this
     */
    open fun arrangeUnSeeds(draws: MutableList<DrawCell>) {
        val unArranged = mutableListOf<Int>()
        for (i in draws.indices) {
            if (draws[i].matchRecord == null && draws[i].type == 0) {
                unArranged.add(i)
            }
        }
        unArranged.shuffle()
        // arrange wildcard
        for (i in 0 until match.match.wildcardDraws) {
            draws[unArranged[i]].type = 2
        }
        // arrange qualify
        for (i in match.match.wildcardDraws until match.match.wildcardDraws + match.match.qualifyDraws) {
            draws[unArranged[i]].type = 3
        }
        // arrange direct in
        var directInIndex = 0
        for (i in match.match.wildcardDraws + match.match.qualifyDraws until unArranged.size) {
            fillNormal(draws, unArranged[i], directInUnSeedList[directInIndex])
            directInIndex ++
        }
    }

    open fun arrangeDraw128Seed32(draws: MutableList<DrawCell>) {
        fillSeed(draws, 0, seedList[0])
        fillSeed(draws, 127, seedList[1])
        val seed34 = seedList.subList(2, 4).shuffled()
        fillSeed(draws, 63, seed34[0])
        fillSeed(draws, 64, seed34[1])
        val seed5to8 = seedList.subList(4, 8).shuffled()
        fillSeed(draws, 31, seed5to8[0])
        fillSeed(draws, 32, seed5to8[1])
        fillSeed(draws, 95, seed5to8[2])
        fillSeed(draws, 96, seed5to8[3])
        val seed9to12 = seedList.subList(8, 12).shuffled()
        fillSeed(draws, 16, seed9to12[0])
        fillSeed(draws, 47, seed9to12[1])
        fillSeed(draws, 80, seed9to12[2])
        fillSeed(draws, 111, seed9to12[3])
        val seed13to16 = seedList.subList(12, 16).shuffled()
        fillSeed(draws, 15, seed13to16[0])
        fillSeed(draws, 48, seed13to16[1])
        fillSeed(draws, 79, seed13to16[2])
        fillSeed(draws, 112, seed13to16[3])
        val seed17to20 = seedList.subList(16, 20).shuffled()
        fillSeed(draws, 8, seed17to20[0])
        fillSeed(draws, 55, seed17to20[1])
        fillSeed(draws, 72, seed17to20[2])
        fillSeed(draws, 119, seed17to20[3])
        val seed21to24 = seedList.subList(20, 24).shuffled()
        fillSeed(draws, 23, seed21to24[0])
        fillSeed(draws, 40, seed21to24[1])
        fillSeed(draws, 87, seed21to24[2])
        fillSeed(draws, 104, seed21to24[3])
        val seed25to28 = seedList.subList(24, 28).shuffled()
        fillSeed(draws, 24, seed25to28[0])
        fillSeed(draws, 39, seed25to28[1])
        fillSeed(draws, 88, seed25to28[2])
        fillSeed(draws, 103, seed25to28[3])
        val seed29to32 = seedList.subList(28, 32).shuffled()
        fillSeed(draws, 7, seed29to32[0])
        fillSeed(draws, 56, seed29to32[1])
        fillSeed(draws, 71, seed29to32[2])
        fillSeed(draws, 120, seed29to32[3])

        // arrange bye
        if (match.match.byeDraws == 32) {
            // 每八个签位，第一个和第八个是种子位，第2个、第7个为轮空位
            for (i in 0 until 127) {
                var index = i % 8
                if (index == 1 || index == 6) {
                    fillBye(draws, i)
                }
            }
        }
    }

    /**
     * qualify设置qualifyDraws个种子，每8个(3轮，2的3次方)签位占第一个
     */
    private fun createQualifyDraw(draws: MutableList<DrawCell>) {
        val seedQualify = qualifyList.take(match.match.qualifyDraws)
        val unSeedQualify = qualifyList.subList(match.match.qualifyDraws, qualifyList.size).shuffled()
        var unSeedIndex = 0
        draws.forEachIndexed { index, drawCell ->
            if (index % 8 == 0) {// 3轮，所以是每8个签位一个种子
                val seedIndex = index / 8
                seedQualify[seedIndex].seed = seedIndex + 1
                fillSeed(draws, index, seedQualify[seedIndex])
            }
            else {
                fillNormal(draws, index, unSeedQualify[unSeedIndex])
                unSeedIndex ++
            }
        }
    }
}
class GrandSlamPlan(list: List<RankRecord>, match: MatchPeriodWrap): DrawPlan(list, match) {

    override fun calcSeed() {
        seed = 32
        seedList = list.take(seed)
    }

    override fun calcDirectInUnSeed() {
        // gs没有bye,wildcard
        directInUnSeed = match.match.draws - seed - match.match.qualifyDraws
        directInUnSeedList = list.subList(seed, seed + directInUnSeed)
    }

    /**
     * 32 q, 3 rounds, 32*2*2*2 = 256
     */
    override fun calcQualify() {
        qualify = 256
        qualifyList = list.subList(seed + directInUnSeed, seed + directInUnSeed + qualify)
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        arrangeDraw128Seed32(draws)
        arrangeUnSeeds(draws)
    }
}

/**
 * GM1000，Top30 4站128签强制，其他6站选4站强制
 */
class GM1000Plan(list: List<RankRecord>, match: MatchPeriodWrap): DrawPlan(list, match) {

    override fun calcSeed() {
        seed = if (match.match.byeDraws < 16) 16 else match.match.byeDraws
        // 强制
        if (match.match.draws == 128) {
            seedList = list.take(seed)
        }
        else {
            // TODO 6选4的逻辑太复杂，先按照强制处理
            seedList = list.take(seed)
        }
    }

    override fun calcDirectInUnSeed() {
        directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws
        directInUnSeedList = list.subList(seed, seed + directInUnSeed)
    }

    /**
     * qualify拓展1倍，后50个名额扩展3倍进行shuffle
     */
    override fun calcQualify() {
        qualify = match.match.qualifyDraws * 8
        val forsure = qualify - 50
        val tempList = mutableListOf<RankRecord>()
        val directIn = seed + directInUnSeed
        tempList.addAll(list.subList(directIn, directIn + forsure))
        // 后50个名额扩展3倍进行shuffle
        val extend = list.subList(directIn + forsure, directIn + forsure + 150).shuffled().take(50)
        tempList.addAll(extend)
        qualifyList = tempList
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        TODO("Not yet implemented")
    }
}

/**
 * GM500，500以内有条件随机
 */
class GM500Plan(list: List<RankRecord>, match: MatchPeriodWrap): DrawPlan(list, match) {

    val random = Random()

    override fun calcSeed() {
        seed = if (match.match.byeDraws < 8) 8 else match.match.byeDraws
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
        directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws
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
     * qualify排名500以内
     */
    override fun calcQualify() {
        qualify = match.match.qualifyDraws * 8
        val directInEnd = directInUnSeedList.last().rank
        val limitList = list.subList(directInEnd, 500)
        qualifyList = limitList.shuffled().take(qualify)
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        TODO("Not yet implemented")
    }
}


/**
 * GM250范围为500以后1500以内条件随机
 */
class GM250Plan(list: List<RankRecord>, match: MatchPeriodWrap): DrawPlan(list, match) {

    val random = Random()
    val total = match.match.draws - match.match.byeDraws - match.match.qualifyDraws - match.match.wildcardDraws + match.match.qualifyDraws * 8
    val rangeList = list.subList(500, 1500).shuffled().take(total).sortedBy { it.rank }

    override fun calcSeed() {
        seed = 8
        rangeList.take(seed)
    }

    override fun calcDirectInUnSeed() {
        directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.match.wildcardDraws
        directInUnSeedList = rangeList.subList(seed, seed + directInUnSeed)
    }

    /**
     * qualify排名放宽到500
     */
    override fun calcQualify() {
        qualify = match.match.qualifyDraws * 8
        qualifyList = rangeList.takeLast(qualify)
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        TODO("Not yet implemented")
    }
}