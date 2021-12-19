package com.king.app.coolg_kt.page.match.draw

import com.king.app.coolg_kt.page.match.WildcardBean

/**
 * @description:
 * @author：Jing
 * @date: 2021/5/29 10:37
 */
class DrawStrategy {
    var gm1000: GM1000Strategy? = null
    var gm500: GM500Strategy? = null
    var gm250: GM250Strategy? = null
    var low: LowStrategy? = null
    var micro: MicroStrategy? = null
    var preAppliers = mutableListOf<WildcardBean>()// list中符合条件的会被添加至draw中，并从list中删除，剩余的将作为后续wildcards使用
}

data class GM1000Strategy(
    var shuffleRate: Int = 50,
    var lowRank: Int = 300
)

data class GM500Strategy(
    var top10: Int = 2,
    var top20: Int = 3,
    var top50: Int = 10,
    var mainLowRank: Int = 80,
    var qualifyLowRank: Int = 250
)

data class GM250Strategy(
    var top10: Int = 1,
    var top20: Int = 2,
    var top50: Int = 8,
    var mainLowRank: Int = 100,
    var qualifyLowRank: Int = 350
)

data class LowStrategy(
    var rankTopLimit: Int = 180,
    var mainSeedLow: Int = 300,
    var mainLow: Int = 450,
    var qualifySeedLow: Int = 550,
    var qualifyLow: Int = 1000
)

data class MicroStrategy(
    var rankTopLimit: Int = 1001,
    var mainSeedLow: Int = 1200
)