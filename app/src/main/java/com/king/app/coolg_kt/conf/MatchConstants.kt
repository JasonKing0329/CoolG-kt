package com.king.app.coolg_kt.conf

import com.king.app.coolg_kt.model.bean.StandardMatchDraw

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:19
 */
object MatchConstants {

    val MATCH_LEVEL = arrayOf(
        "Grand Slam",
        "Master Final",
        "GM1000",
        "GM500",
        "GM250"
    )

    val MATCH_LEVEL_DRAW_STD = arrayOf(
        StandardMatchDraw(128, 0, 32),
        StandardMatchDraw(8, 0, 0),
        StandardMatchDraw(64, 8, 8),
        StandardMatchDraw(32, 4, 8),
        StandardMatchDraw(32, 4, 8)
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

}

data class RoundPack(
    var id: Int,
    var fullName: String,
    var shortName: String
)