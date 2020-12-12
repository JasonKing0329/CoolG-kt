package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/2/9 11:49
 */
@Entity(tableName = "record_type3")
data class RecordType3w (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long,
    var sequence:Int = 0,

    @ColumnInfo(name = "score_fk_type1")
    var scoreFkType1:Int = 0,

    @ColumnInfo(name = "score_fk_type2")
    var scoreFkType2:Int = 0,

    @ColumnInfo(name = "score_fk_type3")
    var scoreFkType3:Int = 0,

    @ColumnInfo(name = "score_fk_type4")
    var scoreFkType4:Int = 0,

    @ColumnInfo(name = "score_fk_type5")
    var scoreFkType5:Int = 0,

    @ColumnInfo(name = "score_fk_type6")
    var scoreFkType6:Int = 0,

    @ColumnInfo(name = "score_fk_type7")
    var scoreFkType7:Int = 0,

    @ColumnInfo(name = "score_fk_type8")
    var scoreFkType8:Int = 0,

    @ColumnInfo(name = "score_story")
    var scoreStory:Int = 0,

    @ColumnInfo(name = "score_scene")
    var scoreScene:Int = 0,

    @ColumnInfo(name = "score_rim")
    var scoreRim:Int = 0,

    @ColumnInfo(name = "score_bjob")
    var scoreBjob:Int = 0,

    @ColumnInfo(name = "score_fore_play")
    var scoreForePlay:Int = 0,

    @ColumnInfo(name = "score_rhythm")
    var scoreRhythm:Int = 0,

    @ColumnInfo(name = "score_cshow")
    var scoreCshow:Int = 0

)