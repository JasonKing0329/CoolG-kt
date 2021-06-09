package com.king.app.coolg_kt.page.match.draw

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
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
abstract class DrawPlan(var list: List<RankRecord>, var match: MatchPeriodWrap, var drawStrategy: DrawStrategy?) {

    val database = CoolApplication.instance.database!!

    var seed: Int = 0
    var seedList = listOf<RankRecord>()// 需要按升序排列
    var directInUnSeed: Int = 0
    var directInUnSeedList = listOf<RankRecord>()// 需要按升序排列
    var qualify: Int = 0
    var qualifyList = listOf<RankRecord>()// 需要按升序排列

    /**
     * GS, GM1000同期无其他match，不需要考虑重复参赛问题
     */
    var samePeriodMap = listOf<Long>()

    fun prepare() {
        prepareSamePeriodMap()

        calcSeed()
        setSeed()
        calcDirectInUnSeed()
        calcQualify()
    }

    abstract fun calcSeed()

    abstract fun calcDirectInUnSeed()

    abstract fun calcQualify()

    open fun prepareSamePeriodMap() {
        samePeriodMap = database.getMatchDao().getSamePeriodRecordIds(match.bean.period, match.bean.orderInPeriod)
    }

    open fun setSeed() {
        // rank可能不连续，需要设置seed
        seedList.forEachIndexed { index, rankRecord -> rankRecord.seed = index + 1 }
    }

    fun arrangeMainDraw(): MutableList<DrawCell> {
        val draws = mutableListOf<DrawCell>()
        for (i in 0 until match.match.draws) {
            draws.add(DrawCell(null))
        }
        createMainDraw(draws)
        return draws
    }

    abstract fun createMainDraw(draws: MutableList<DrawCell>)

    fun arrangeQualifyDraw(): MutableList<DrawCell> {
        val draws = mutableListOf<DrawCell>()
        for (i in 0 until qualify) {
            draws.add(DrawCell(null))
        }
        createQualifyDraw(draws)
        return draws
    }

    open fun fillSeed(draws: MutableList<DrawCell>, index: Int, rankRecord: RankRecord) {
        draws[index].matchRecord = MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, match.bean.id, 0, rankRecord.recordId, rankRecord.rank, rankRecord.seed, 0)
    }

    open fun fillNormal(draws: MutableList<DrawCell>, index: Int, rankRecord: RankRecord) {
        draws[index].matchRecord = MatchRecord(0, MatchConstants.MATCH_RECORD_NORMAL, match.bean.id, 0, rankRecord.recordId, rankRecord.rank, 0, 0)
    }

    open fun fillBye(draws: MutableList<DrawCell>, index: Int) {
        draws[index].matchRecord = MatchRecord(0, MatchConstants.MATCH_RECORD_BYE, match.bean.id, 0, 0, 0, 0, 0)
    }

    open fun fillWildCard(draws: MutableList<DrawCell>, index: Int) {
        draws[index].matchRecord = MatchRecord(0, MatchConstants.MATCH_RECORD_WILDCARD, match.bean.id, 0, 0, 0, 0, 0)
    }

    open fun fillQualify(draws: MutableList<DrawCell>, index: Int) {
        draws[index].matchRecord = MatchRecord(0, MatchConstants.MATCH_RECORD_QUALIFY, match.bean.id, 0, 0, 0, 0, 0)
    }

    /**
     * arrange unseed and wildcard, qualify in main draw
     * call arrange seed and bye before this
     */
    open fun arrangeUnSeeds(draws: MutableList<DrawCell>) {
        val unArranged = mutableListOf<Int>()
        for (i in draws.indices) {
            if (draws[i].matchRecord == null) {
                unArranged.add(i)
            }
        }
        unArranged.shuffle()
        // arrange wildcard
        for (i in 0 until match.bean.mainWildcard) {
            fillWildCard(draws, unArranged[i])
        }
        // arrange qualify
        for (i in match.bean.mainWildcard until match.bean.mainWildcard + match.match.qualifyDraws) {
            fillQualify(draws, unArranged[i])
        }
        // arrange direct in
        var directInIndex = 0
        for (i in match.bean.mainWildcard + match.match.qualifyDraws until unArranged.size) {
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
     * @param seed 16或8
     */
    open fun arrangeDraw64(draws: MutableList<DrawCell>, seed: Int) {
        fillSeed(draws, 0, seedList[0])
        fillSeed(draws, 63, seedList[1])
        val seed34 = seedList.subList(2, 4).shuffled()
        fillSeed(draws, 31, seed34[0])
        fillSeed(draws, 32, seed34[1])
        val seed5to8 = seedList.subList(4, 8).shuffled()
        fillSeed(draws, 15, seed5to8[0])
        fillSeed(draws, 16, seed5to8[1])
        fillSeed(draws, 47, seed5to8[2])
        fillSeed(draws, 48, seed5to8[3])
        if (seed == 16) {
            val seed9to12 = seedList.subList(8, 12).shuffled()
            fillSeed(draws, 8, seed9to12[0])
            fillSeed(draws, 23, seed9to12[1])
            fillSeed(draws, 40, seed9to12[2])
            fillSeed(draws, 55, seed9to12[3])
            val seed13to16 = seedList.subList(12, 16).shuffled()
            fillSeed(draws, 7, seed13to16[0])
            fillSeed(draws, 24, seed13to16[1])
            fillSeed(draws, 39, seed13to16[2])
            fillSeed(draws, 56, seed13to16[3])
        }

        // arrange bye
        if (match.match.byeDraws == 16) {
            // 每八个签位，第一个和第八个是种子位，第2个、第7个为轮空位
            for (i in 0 until 63) {
                var index = i % 8
                if (index == 1 || index == 6) {
                    fillBye(draws, i)
                }
            }
        }
        // 每16个签位，第一个和第16个是种子位，第2个、第15个为轮空位
        else if (match.match.byeDraws == 8) {
            for (i in 0 until 63) {
                var index = i % 16
                if (index == 1 || index == 14) {
                    fillBye(draws, i)
                }
            }
        }
    }

    open fun arrangeDraw32Seed8(draws: MutableList<DrawCell>) {
        fillSeed(draws, 0, seedList[0])
        fillSeed(draws, 31, seedList[1])
        val seed34 = seedList.subList(2, 4).shuffled()
        fillSeed(draws, 15, seed34[0])
        fillSeed(draws, 16, seed34[1])
        val seed5to8 = seedList.subList(4, 8).shuffled()
        fillSeed(draws, 7, seed5to8[0])
        fillSeed(draws, 8, seed5to8[1])
        fillSeed(draws, 23, seed5to8[2])
        fillSeed(draws, 24, seed5to8[3])

        // arrange bye
        if (match.match.byeDraws == 8) {
            // 每八个签位，第一个和第八个是种子位，第2个、第7个为轮空位
            for (i in 0 until 31) {
                var index = i % 8
                if (index == 1 || index == 6) {
                    fillBye(draws, i)
                }
            }
        }
        // 8个种子只轮空4个，每16个签位，第一个和第16个是种子位，第2个、第15个为轮空位
        else if (match.match.byeDraws == 4) {
            for (i in 0 until 31) {
                var index = i % 16
                if (index == 1 || index == 14) {
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
        // 确定wildcard所在位置
        var wcIndices = listOf<Int>()
        if (match.bean.qualifyWildcard > 0) {
            // 不能出现在种子位，随机出现在非种子位
            val unSeedIndices = mutableListOf<Int>()
            draws.forEachIndexed { index, drawCell ->
                if (index % 8 != 0) {// 3轮，所以是每8个签位一个
                    unSeedIndices.add(index)
                }
            }
            wcIndices = unSeedIndices.shuffled().take(match.bean.qualifyWildcard)
        }
        draws.forEachIndexed { index, drawCell ->
            if (index % 8 == 0) {// 3轮，所以是每8个签位一个种子
                val seedIndex = index / 8
                seedQualify[seedIndex].seed = seedIndex + 1
                fillSeed(draws, index, seedQualify[seedIndex])
            }
            else {
                val wcIndex = wcIndices.firstOrNull { it == index }
                if (wcIndex == null) {
                    fillNormal(draws, index, unSeedQualify[unSeedIndex])
                    unSeedIndex ++
                }
                else {
                    fillWildCard(draws, index)
                }
            }
        }
    }
}

class GrandSlamPlan(list: List<RankRecord>, match: MatchPeriodWrap, drawStrategy: DrawStrategy?): DrawPlan(list, match, drawStrategy) {

    override fun calcSeed() {
        seed = 32
        seedList = list.take(seed)
    }

    override fun calcDirectInUnSeed() {
        // gs没有bye,wildcard
        directInUnSeed = match.match.draws - seed - match.match.qualifyDraws - match.bean.mainWildcard
        directInUnSeedList = list.subList(seed, seed + directInUnSeed)
    }

    /**
     * 32 q, 3 rounds, 32*2*2*2 = 256
     */
    override fun calcQualify() {
        qualify = 256
        qualifyList = list.subList(seed + directInUnSeed, seed + directInUnSeed + qualify - match.bean.qualifyWildcard)
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        arrangeDraw128Seed32(draws)
        arrangeUnSeeds(draws)
    }
}

/**
 * GM1000，Top30 4站128签强制，其他6站选4站强制
 */
class GM1000Plan(list: List<RankRecord>, match: MatchPeriodWrap, drawStrategy: DrawStrategy?): DrawPlan(list, match, drawStrategy) {

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
        directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard
        directInUnSeedList = list.subList(seed, seed + directInUnSeed)
    }

    /**
     * qualify设置shuffle比例
     * shuffle比例之前的按排名直接入围，shuffle比例部分的按最低排名以内进行shuffle
     */
    override fun calcQualify() {
        qualify = match.match.qualifyDraws * 8
        // 确定shuffle的数量
        var shuffleNum = qualify / 2// 默认shuffle的数量
        drawStrategy?.gm1000?.let { shuffleNum = (qualify * (it.shuffleRate.toFloat() / 100)).toInt() }
        // 确定直接入围的数量
        val forsure = qualify - shuffleNum
        val tempList = mutableListOf<RankRecord>()
        // qualify的起始rank
        val directIn = seed + directInUnSeed
        // 添加直接入围
        tempList.addAll(list.subList(directIn, directIn + forsure))
        // 在最低排名内进行shuffle
        var lowRank = directIn + forsure + shuffleNum * 3// 默认最低，按shuffleNum的3倍扩充
        drawStrategy?.gm1000?.let { lowRank = it.lowRank }
        // 添加所有shuffle的数据
        val extend = list.subList(directIn + forsure, lowRank).shuffled().take(shuffleNum - match.bean.qualifyWildcard)
        tempList.addAll(extend)
        qualifyList = tempList
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        if (match.match.draws == 128) {
            arrangeDraw128Seed32(draws)
        }
        else {
            arrangeDraw64(draws, 16)
        }
        arrangeUnSeeds(draws)
    }
}

/**
 * GM500，排名300以内有条件随机
 */
class GM500Plan(list: List<RankRecord>, match: MatchPeriodWrap, drawStrategy: DrawStrategy?): DrawPlan(list, match, drawStrategy) {

    val random = Random()
    var mainList = mutableListOf<RankRecord>()

    private fun addToResult(rankRecord: RankRecord, resultSeeds: MutableList<RankRecord>) {
        when {
            // top10只有5分之一几率参加
            rankRecord.rank < 10 -> {
                if (abs(random.nextInt()) % 5 == 1 && !samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
            // top11-20只有3分之一几率参加
            rankRecord.rank < 20 -> {
                if (abs(random.nextInt()) % 3 == 1 && !samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
            // top21-100只有2分之一几率参加
            rankRecord.rank < 100 -> {
                if (abs(random.nextInt()) % 2 == 1 && !samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
            else -> {
                if (!samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
        }
    }

    override fun calcSeed() {
        drawStrategy?.gm500?.let {
            seed = if (match.match.byeDraws < 8) 8 else match.match.byeDraws
            directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard
            var mainSure = seed + directInUnSeed
            if (it.top10 > 0) {
                val result = list.filter { item -> item.rank <= 10 && !samePeriodMap.contains(item.recordId)}.shuffled().take(it.top10)
                mainList.addAll(result)
            }
            if (it.top20 > 0) {
                val result = list.filter { item -> item.rank in 11..20 && !samePeriodMap.contains(item.recordId)}.shuffled().take(it.top20)
                mainList.addAll(result)
            }
            if (it.top50 > 0) {
                val result = list.filter { item -> item.rank in 21..50 && !samePeriodMap.contains(item.recordId)}.shuffled().take(it.top50)
                mainList.addAll(result)
            }
            // 超出则删去
            if (mainList.size > mainSure) {
                mainList = mainList.subList(0, mainSure)
            }
            // 不足则在最低排名内补
            else if (mainList.size < mainSure) {
                var limitList = list.filter { item -> item.rank in 51..it.mainLowRank && !samePeriodMap.contains(item.recordId)}
                mainList.addAll(limitList.shuffled().take(mainSure - mainList.size))
            }
            mainList.sortBy { item -> item.rank }
            // 确定种子列表
            seedList = mainList.subList(0, seed)
            // 确定直接入围列表
            directInUnSeedList = mainList.subList(seed, mainList.size)
            return
        }
        // 默认方式
        seed = if (match.match.byeDraws < 8) 8 else match.match.byeDraws
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in list.indices) {
            addToResult(list[i], resultSeeds)
            if (resultSeeds.size == seed) {
                break
            }
        }
        seedList = resultSeeds
    }

    override fun calcDirectInUnSeed() {
        drawStrategy?.gm500?.let {
            return
        }
        // 默认方式
        directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard
        val seedEnd = seedList.last().rank
        var indexStart = list.indexOfFirst { it.rank == seedEnd } + 1
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in indexStart until list.size) {
            addToResult(list[i], resultSeeds)
            if (resultSeeds.size == directInUnSeed) {
                break
            }
        }
        directInUnSeedList = resultSeeds
    }

    /**
     * qualify排名300以内
     */
    override fun calcQualify() {
        qualify = match.match.qualifyDraws * 8
        val directInEnd = directInUnSeedList.last().rank
        var lowRank = 300// 默认最低
        drawStrategy?.gm500?.let {
            lowRank = it.qualifyLowRank
        }
        val limitList = list.filter { it.rank in (directInEnd + 1)..lowRank }
        val result = mutableListOf<RankRecord>()
        run outside@{
            limitList.shuffled().forEach {
                if (!samePeriodMap.contains(it.recordId)) {
                    result.add(it)
                }
                if (result.size == qualify - match.bean.qualifyWildcard) {
                    return@outside
                }
            }
        }
        qualifyList = result.sortedBy { it.rank }
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        if (match.match.draws == 64) {
            arrangeDraw64(draws, match.match.byeDraws)
        }
        else {
            arrangeDraw32Seed8(draws)
        }
        arrangeUnSeeds(draws)
    }
}

/**
 * GM250，排名500内有条件随机
 */
class GM250Plan(list: List<RankRecord>, match: MatchPeriodWrap, drawStrategy: DrawStrategy?): DrawPlan(list, match, drawStrategy) {

    val random = Random()
    var mainList = mutableListOf<RankRecord>()

    private fun addToResult(rankRecord: RankRecord, resultSeeds: MutableList<RankRecord>) {
        when {
            // top10只有,10分之一几率参加
            rankRecord.rank < 10 -> {
                if (abs(random.nextInt()) % 10 == 1 && !samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
            // top11-20只有5分之一几率参加
            rankRecord.rank < 20 -> {
                if (abs(random.nextInt()) % 5 == 1 && !samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
            // top21-50只有3分之一几率参加
            rankRecord.rank < 50 -> {
                if (abs(random.nextInt()) % 3 == 1 && !samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
            // top51-100只有2分之一几率参加
            rankRecord.rank < 50 -> {
                if (abs(random.nextInt()) % 2 == 1 && !samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
            else -> {
                if (!samePeriodMap.contains(rankRecord.recordId)) {
                    resultSeeds.add(rankRecord)
                }
            }
        }
    }

    override fun calcSeed() {
        drawStrategy?.gm250?.let {
            seed = if (match.match.byeDraws < 8) 8 else match.match.byeDraws
            directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard
            var mainSure = seed + directInUnSeed
            if (it.top10 > 0) {
                val result = list.filter { item -> item.rank <= 10 && !samePeriodMap.contains(item.recordId)}.shuffled().take(it.top10)
                mainList.addAll(result)
            }
            if (it.top20 > 0) {
                val result = list.filter { item -> item.rank in 11..20 && !samePeriodMap.contains(item.recordId)}.shuffled().take(it.top20)
                mainList.addAll(result)
            }
            if (it.top50 > 0) {
                val result = list.filter { item -> item.rank in 21..50 && !samePeriodMap.contains(item.recordId)}.shuffled().take(it.top50)
                mainList.addAll(result)
            }
            // 超出则删去
            if (mainList.size > mainSure) {
                mainList = mainList.subList(0, mainSure)
            }
            // 不足则在最低排名内补
            else if (mainList.size < mainSure) {
                var limitList = list.filter { item -> item.rank in 51..it.mainLowRank && !samePeriodMap.contains(item.recordId)}
                mainList.addAll(limitList.shuffled().take(mainSure - mainList.size))
            }
            mainList.sortBy { item -> item.rank }
            // 确定种子列表
            seedList = mainList.subList(0, seed).sortedBy { item -> item.rank }
            // 确定直接入围列表
            directInUnSeedList = mainList.subList(seed, mainList.size).sortedBy { item -> item.rank }
            return
        }
        seed = if (match.match.byeDraws < 8) 8 else match.match.byeDraws
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in list.indices) {
            addToResult(list[i], resultSeeds)
            if (resultSeeds.size == seed) {
                break
            }
        }
        seedList = resultSeeds
    }

    override fun calcDirectInUnSeed() {
        drawStrategy?.gm500?.let {
            return
        }
        directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard
        val seedEnd = seedList.last().rank
        var indexStart = list.indexOfFirst { it.rank == seedEnd } + 1
        val resultSeeds = mutableListOf<RankRecord>()
        for (i in indexStart until list.size) {
            addToResult(list[i], resultSeeds)
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
        var lowRank = 500// 默认最低
        drawStrategy?.gm500?.let {
            lowRank = it.qualifyLowRank
        }
        val limitList = list.filter { it.rank in (directInEnd + 1)..lowRank }
        val result = mutableListOf<RankRecord>()
        run outside@{
            limitList.shuffled().forEach {
                if (!samePeriodMap.contains(it.recordId)) {
                    result.add(it)
                }
                if (result.size == qualify - match.bean.qualifyWildcard) {
                    return@outside
                }
            }
        }
        qualifyList = result.sortedBy { it.rank }
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        arrangeDraw32Seed8(draws)
        arrangeUnSeeds(draws)
    }
}

/**
 * Low范围为rankLimit以后1200(MatchConstants.RANK_LIMIT_MAX)以内条件随机
 * list已经满足了1200的条件
 */
class LowPlan(list: List<RankRecord>, match: MatchPeriodWrap, drawStrategy: DrawStrategy?): DrawPlan(list, match, drawStrategy) {

    var rankLimit = 180
    val random = Random()
    private val total = match.match.draws - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard + match.match.qualifyDraws * 8 - match.bean.qualifyWildcard
    private lateinit var rangeList: List<RankRecord>

    override fun prepareSamePeriodMap() {
        super.prepareSamePeriodMap()
        drawStrategy?.low?.let {

            rankLimit = it.rankTopLimit

            // 如果是64签，固定设16种子，32签固定设8种子。均无轮空
            seed = if (match.match.draws == 64) {
                16
            } else {
                8
            }
            seedList = list.filter { item -> item.rank in (rankLimit + 1)..it.mainSeedLow && !samePeriodMap.contains(item.recordId) }
                .shuffled().take(seed).sortedBy { item -> item.rank }
            // 直接入围
            val seedEnd = seedList.last().rank
            directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard
            directInUnSeedList = list.filter { item -> item.rank in (seedEnd + 1)..it.mainLow && !samePeriodMap.contains(item.recordId) }
                .shuffled().take(directInUnSeed).sortedBy { item -> item.rank }
            // qualify
            val directInEnd = directInUnSeedList.last().rank
            qualify = match.match.qualifyDraws * 8
            val qualifySeed = match.match.qualifyDraws
            var qualifies = mutableListOf<RankRecord>()
            var quaSeeds = list.filter { item -> item.rank in (directInEnd + 1)..it.qualifySeedLow && !samePeriodMap.contains(item.recordId) }
                .shuffled().take(qualifySeed).sortedBy { item -> item.rank }
            var quaLefts = list.filter { item -> item.rank > it.qualifySeedLow && !samePeriodMap.contains(item.recordId) }
                .shuffled().take(qualify - qualifySeed - match.bean.qualifyWildcard).sortedBy { item -> item.rank }
            qualifies.addAll(quaSeeds)
            qualifies.addAll(quaLefts)
            qualifyList = qualifies
            return
        }
        rangeList = list.filter { it.rank>rankLimit && !samePeriodMap.contains(it.recordId) }
            .shuffled()
            .take(total)
            .sortedBy { it.rank }
    }

    override fun calcSeed() {
        drawStrategy?.low?.let {
            return
        }
        // 如果是64签，固定设16种子，32签固定设8种子。均无轮空
        seed = if (match.match.draws == 64) {
            16
        } else {
            8
        }
        seedList = rangeList.take(seed)
    }

    override fun calcDirectInUnSeed() {
        drawStrategy?.low?.let {
            return
        }
        directInUnSeed = match.match.draws - seed - match.match.byeDraws - match.match.qualifyDraws - match.bean.mainWildcard
        directInUnSeedList = rangeList.subList(seed, seed + directInUnSeed)
    }

    override fun calcQualify() {
        drawStrategy?.low?.let {
            return
        }
        qualify = match.match.qualifyDraws * 8
        qualifyList = rangeList.takeLast(qualify - match.bean.qualifyWildcard)
    }

    override fun createMainDraw(draws: MutableList<DrawCell>) {
        if (match.match.draws == 64) {
            arrangeDraw64(draws, 16)
        }
        else {
            arrangeDraw32Seed8(draws)
        }
        arrangeUnSeeds(draws)
    }
}