package com.king.app.gdb.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/6/14 10:47
 */
@Entity(tableName = "record_rating")
data class RecordRating(
    @PrimaryKey
    var recordId: Long,
    var content: Float,// 35%
    var person: Float,// 12%
    var passion: Float,// 16%
    var body: Float,// 12%
    var cum: Float,// 5%
    var scene: Float,// 5%, dynamic
    var dk: Float,// 5%, dynamic
    var butt: Float,// 5%, dynamic
    var special: Float,// 5%, dynamic
    var dynamicMap: String// total 20%, children:scene,dk,butt,special; bean->RecordRatingDynamic
)