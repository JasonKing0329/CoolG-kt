package com.king.app.coolg_kt.page.home

import com.king.app.gdb.data.relation.RecordStarWrap
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/24 11:38
 */
data class HomeRecord(
    var bean: RecordWrap,
    var date: String,
    var showDate: Boolean,
    var cell: Int = 2
)
data class HomeStar(
    var bean: RecordStarWrap,
    var date: String,
    var cell: Int = 1,
    var gravity: Int = 0,
    var imageHeight: Int = 0
)