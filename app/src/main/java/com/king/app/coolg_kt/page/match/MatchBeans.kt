package com.king.app.coolg_kt.page.match

import com.king.app.gdb.data.entity.match.MatchItem
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 11:30
 */
data class DrawItem (
    var matchItem: MatchItem,
    var matchRecord1: MatchRecordWrap? = null,
    var matchRecord2: MatchRecordWrap? = null,
    var winner: MatchRecordWrap? = null
)
data class DrawCell<T> (
    var data: T,
    var type: Int // 0:MatchRecord, 1:bye
)