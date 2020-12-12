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
    var id: Long,
    var scene: String? = null,
    var directory: String? = null,
    var name: String? = null,

    @ColumnInfo(name = "hd_level")
    var hdLevel:Int = 0,
    var score:Int = 0,

    @ColumnInfo(name = "score_feel")
    var scoreFeel:Int = 0,

    @ColumnInfo(name = "score_star")
    var scoreStar:Int = 0,

    @ColumnInfo(name = "score_passion")
    var scorePassion:Int = 0,

    @ColumnInfo(name = "score_body")
    var scoreBody:Int = 0,

    @ColumnInfo(name = "score_cock")
    var scoreCock:Int = 0,

    @ColumnInfo(name = "score_ass")
    var scoreAss:Int = 0,

    @ColumnInfo(name = "score_cum")
    var scoreCum:Int = 0,

    @ColumnInfo(name = "score_special")
    var scoreSpecial:Int = 0,

    @ColumnInfo(name = "score_bareback")
    var scoreBareback:Int = 0,
    var deprecated:Int = 0,

    @ColumnInfo(name = "special_desc")
    var specialDesc: String? = null,

    @ColumnInfo(name = "last_modify_time")
    var lastModifyTime: Long = 0,
    var type:Int = 0,

    @ColumnInfo(name = "record_detail_id")
    var recordDetailId: Long = 0
)