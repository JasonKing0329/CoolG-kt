package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/2/9 11:46
 */
@Entity(tableName = "record")
data class Record (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "SCENE")
    var scene: String? = null,
    @ColumnInfo(name = "DIRECTORY")
    var directory: String? = null,
    @ColumnInfo(name = "NAME")
    var name: String? = null,

    @ColumnInfo(name = "HD_LEVEL")
    var hdLevel:Int = 0,
    @ColumnInfo(name = "SCORE")
    var score:Int = 0,

    @ColumnInfo(name = "SCORE_FEEL")
    var scoreFeel:Int = 0,

    @ColumnInfo(name = "SCORE_STAR")
    var scoreStar:Int = 0,

    @ColumnInfo(name = "SCORE_PASSION")
    var scorePassion:Int = 0,

    @ColumnInfo(name = "SCORE_BODY", defaultValue = "0")
    var scoreBody:Int?,

    @ColumnInfo(name = "SCORE_COCK", defaultValue = "0")
    var scoreCock:Int?,

    @ColumnInfo(name = "SCORE_ASS", defaultValue = "0")
    var scoreAss:Int?,

    @ColumnInfo(name = "SCORE_CUM")
    var scoreCum:Int = 0,

    @ColumnInfo(name = "SCORE_SPECIAL")
    var scoreSpecial:Int = 0,

    @ColumnInfo(name = "SCORE_BAREBACK")
    var scoreBareback:Int = 0,
    @ColumnInfo(name = "DEPRECATED")
    var deprecated:Int = 0,

    @ColumnInfo(name = "SPECIAL_DESC")
    var specialDesc: String? = null,

    @ColumnInfo(name = "LAST_MODIFY_TIME")
    var lastModifyTime: Long = 0,
    @ColumnInfo(name = "TYPE")
    var type:Int = 0,

    @ColumnInfo(name = "RECORD_DETAIL_ID")
    var recordDetailId: Long = 0
)