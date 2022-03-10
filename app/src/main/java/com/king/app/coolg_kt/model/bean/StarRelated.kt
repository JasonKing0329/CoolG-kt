package com.king.app.coolg_kt.model.bean

import com.king.app.gdb.data.entity.Star

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/3/8 13:17
 */
data class TimelineStar(
    var star: Star,
    var type: Int,
    var debut: Long,
    var debutStr: String,
    var isHidden: Boolean,
    var imageUrl: String? = null
)