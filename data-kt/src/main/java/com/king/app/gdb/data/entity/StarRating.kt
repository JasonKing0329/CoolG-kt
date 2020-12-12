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
    var id: Long?,
    @ColumnInfo(name = "STAR_ID")
    var starId: Long = 0,
    @ColumnInfo(name = "FACE")
    var face:Float = 0f,
    @ColumnInfo(name = "BODY")
    var body:Float = 0f,
    @ColumnInfo(name = "SEXUALITY")
    var sexuality:Float = 0f,
    @ColumnInfo(name = "DK")
    var dk:Float = 0f,
    @ColumnInfo(name = "PASSION")
    var passion:Float = 0f,
    @ColumnInfo(name = "VIDEO")
    var video:Float = 0f,
    @ColumnInfo(name = "COMPLEX")
    var complex:Float = 0f,
    @ColumnInfo(name = "PREFER")
    var prefer:Float = 0f
)