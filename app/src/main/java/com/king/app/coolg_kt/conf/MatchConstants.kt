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
}