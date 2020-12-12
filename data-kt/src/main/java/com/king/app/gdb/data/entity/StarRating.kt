package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * Desc: rating of star
 *
 * @author：Jing Yang
 * @date: 2018/5/8 18:55
 */
@Entity(tableName = "star_rating")
data class StarRating (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    @ColumnInfo(name = "star_id")
    var starId: Long = 0,
    var face:Float = 0f,
    var body:Float = 0f,
    var sexuality:Float = 0f,
    var dk:Float = 0f,
    var passion:Float = 0f,
    var video:Float = 0f,
    var complex:Float = 0f,
    var prefer:Float = 0f
)