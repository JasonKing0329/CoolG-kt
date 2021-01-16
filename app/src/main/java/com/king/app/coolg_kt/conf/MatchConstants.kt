package com.king.app.coolg_kt.conf

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:19
 */
object MatchConstants {
    
    val MAX_ORDER_IN_PERIOD = 46

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

    val MATCH_LEVEL = arrayOf(
        "Grand Slam",
        "Master Final",
        "GM1000",
        "GM500",
        "GM250",
        "LOW"
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

}

data class RoundPack(
    var id: Int,
    var fullName: String,
    var shortName: String
)