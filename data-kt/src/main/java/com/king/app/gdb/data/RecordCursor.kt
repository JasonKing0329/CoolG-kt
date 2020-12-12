package com.king.app.gdb.data

/**
 * 描述: 控制查询record的条件
 *
 * 作者：景阳
 *
 * 创建时间: 2017/10/23 10:48
 */
data class RecordCursor (
    /**
     * 从record表中的偏移量
     */
    var offset:Int = 0,

    /**
     * 总共查询的数量
     */
    var number:Int = 0,

    /**
     * 关键词对应的最小值
     * 不限制则为-1
     */
    var min:Int = -1,

    /**
     * 关键词对应的最大值
     * 不限制则为-1
     */
    var max:Int = -1
)