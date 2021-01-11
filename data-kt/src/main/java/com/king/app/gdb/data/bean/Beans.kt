package com.king.app.gdb.data.bean

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/6 21:22
 */
data class RecordScene(
    var name: String?,
    var number: Int
)
data class RankRecord (
    var recordId: Long,
    var rank: Int
) {
    var seed: Int = 0
}