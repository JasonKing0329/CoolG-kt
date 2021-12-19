package com.king.app.coolg_kt.conf

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:19
 */
object MatchConstants {
    
    val MAX_ORDER_IN_PERIOD = 46
    val RANK_OUT_OF_SYSTEM = 9999 // 不在match_rank_record中的record第一次参与赋值9999
    val RANK_LIMIT_MAX = 1800 // 满足入围match条件是，count_record中的rank小于等于该值
    val MATCH_COUNT_SCORE = 20 // 超过20站只取20站最高积分

    val DRAW_QUALIFY = 0
    val DRAW_MAIN = 1

    val MATCH_RECORD_NORMAL = 0
    val MATCH_RECORD_BYE = 1
    val MATCH_RECORD_WILDCARD = 2
    val MATCH_RECORD_QUALIFY = 3

    val MATCH_RECORD_ORDER1 = 1
    val MATCH_RECORD_ORDER2 = 2

    /**
     * index与MATCH_LEVEL一一对应
     */
    val MATCH_LEVEL_GS = 0
    val MATCH_LEVEL_FINAL = 1
    val MATCH_LEVEL_GM1000 = 2
    val MATCH_LEVEL_GM500 = 3
    val MATCH_LEVEL_GM250 = 4
    val MATCH_LEVEL_LOW = 5
    val MATCH_LEVEL_MICRO = 6

    val MATCH_LEVEL_ALL = MATCH_LEVEL_MICRO + 1 //为方便array数组直接对应value，MATCH_LEVEL_ALL为最后一个

    val MATCH_LEVEL = arrayOf(
        "Grand Slam",
        "Master Final",
        "GM1000",
        "GM500",
        "GM250",
        "LOW",
        "MICRO"
    )

    val ROUND_ID_128 = 1
    val ROUND_ID_64 = 2
    val ROUND_ID_32 = 3
    val ROUND_ID_16 = 4
    val ROUND_ID_QF = 5
    val ROUND_ID_SF = 6
    val ROUND_ID_F = 7
    val ROUND_ID_GROUP = 8
    val ROUND_ID_Q1 = 9
    val ROUND_ID_Q2 = 10
    val ROUND_ID_Q3 = 11
    /**
     * round的排名权值
     */
    fun getRoundSortValue(round: Int): Int {
        return when {
            round >= ROUND_ID_Q1 -> round - ROUND_ID_Q3
            round == ROUND_ID_GROUP -> ROUND_ID_SF - 1
            else -> round
        }
    }

    val ROUND_MAIN_DRAW128 = listOf(
        RoundPack(ROUND_ID_128, "Round One", "R128"),
        RoundPack(ROUND_ID_64, "Round Two", "R64"),
        RoundPack(ROUND_ID_32, "Round Three", "R32"),
        RoundPack(ROUND_ID_16, "Round Four", "R16"),
        RoundPack(ROUND_ID_QF, "Quarter Final", "QF"),
        RoundPack(ROUND_ID_SF, "Semi Final", "SF"),
        RoundPack(ROUND_ID_F, "Final", "F")
    )

    val ROUND_MAIN_DRAW64 = listOf(
        RoundPack(ROUND_ID_64, "Round One", "R64"),
        RoundPack(ROUND_ID_32, "Round Two", "R32"),
        RoundPack(ROUND_ID_16, "Round Three", "R16"),
        RoundPack(ROUND_ID_QF, "Quarter Final", "QF"),
        RoundPack(ROUND_ID_SF, "Semi Final", "SF"),
        RoundPack(ROUND_ID_F, "Final", "F")
    )

    val ROUND_MAIN_DRAW32 = listOf(
        RoundPack(ROUND_ID_32, "Round One", "R32"),
        RoundPack(ROUND_ID_16, "Round Two", "R16"),
        RoundPack(ROUND_ID_QF, "Quarter Final", "QF"),
        RoundPack(ROUND_ID_SF, "Semi Final", "SF"),
        RoundPack(ROUND_ID_F, "Final", "F")
    )

    val ROUND_ROBIN = listOf(
        RoundPack(ROUND_ID_GROUP, "Group", "RR"),
        RoundPack(ROUND_ID_SF, "Semi Final", "SF"),
        RoundPack(ROUND_ID_F, "Final", "F")
    )

    val ROUND_QUALIFY = listOf(
        RoundPack(ROUND_ID_Q1, "Q1", "Q1"),
        RoundPack(ROUND_ID_Q2, "Q2", "Q2"),
        RoundPack(ROUND_ID_Q3, "Q3", "Q3")
    )

    fun roundResultShort(id: Int, isWinner: Boolean): String {
        return when(id) {
            ROUND_ID_128 -> "R128"
            ROUND_ID_64 -> "R64"
            ROUND_ID_32 -> "R32"
            ROUND_ID_16 -> "R16"
            ROUND_ID_QF -> "QF"
            ROUND_ID_SF -> "SF"
            ROUND_ID_F -> if (isWinner) "Win" else "F"
            ROUND_ID_GROUP -> "RR"
            ROUND_ID_Q1 -> "Q1"
            ROUND_ID_Q2 -> "Q2"
            ROUND_ID_Q3 -> "Q3"
            else -> ""
        }
    }

    fun roundFull(id: Int): String {
        return when(id) {
            ROUND_ID_128 -> "Round 128"
            ROUND_ID_64 -> "Round 64"
            ROUND_ID_32 -> "Round 32"
            ROUND_ID_16 -> "Round 16"
            ROUND_ID_QF -> "Quarter Final"
            ROUND_ID_SF -> "Semi Final"
            ROUND_ID_F -> "Final"
            ROUND_ID_GROUP -> "Round Robin"
            ROUND_ID_Q1 -> "Qualify 1"
            ROUND_ID_Q2 -> "Qualify 2"
            ROUND_ID_Q3 -> "Qualify 3"
            else -> ""
        }
    }

}

data class RoundPack(
    var id: Int,
    var fullName: String,
    var shortName: String
)