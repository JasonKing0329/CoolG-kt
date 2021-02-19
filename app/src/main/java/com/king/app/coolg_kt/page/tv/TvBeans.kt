package com.king.app.coolg_kt.page.tv

import com.king.app.coolg_kt.model.udp.ServerBody

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/16 20:36
 */
data class TvServers (
    var list: List<ServerBody> = listOf()
)

data class PlayTime (
    var url: String,
    var time: Int
)

data class TvPlayTimes (
    var list: MutableList<PlayTime> = mutableListOf()
)