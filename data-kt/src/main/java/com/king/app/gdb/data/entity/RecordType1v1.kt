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
@Entity(tableName = "record_type1")
data class RecordType1v1 (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long?,
    @ColumnInfo(name = "SEQUENCE")
    var sequence:Int = 0,

    @ColumnInfo(name = "SCORE_FK_TYPE1")
    var scoreFkType1:Int = 0,

    @ColumnInfo(name = "SCORE_FK_TYPE2")
    var scoreFkType2:Int = 0,

    @ColumnInfo(name = "SCORE_FK_TYPE3")
    var scoreFkType3:Int = 0,

    @ColumnInfo(name = "SCORE_FK_TYPE4")
    var scoreFkType4:Int = 0,

    @ColumnInfo(name = "SCORE_FK_TYPE5")
    var scoreFkType5:Int = 0,

    @ColumnInfo(name = "SCORE_FK_TYPE6")
    var scoreFkType6:Int = 0,

    @ColumnInfo(name = "SCORE_STORY")
    var scoreStory:Int = 0,

    @ColumnInfo(name = "SCORE_SCENE")
    var scoreScene:Int = 0,

    @ColumnInfo(name = "SCORE_RIM")
    var scoreRim:Int = 0,

    @ColumnInfo(name = "SCORE_BJOB")
    var scoreBjob:Int = 0,

    @ColumnInfo(name = "SCORE_FORE_PLAY")
    var scoreForePlay:Int = 0,

    @ColumnInfo(name = "SCORE_RHYTHM")
    var scoreRhythm:Int = 0,

    @ColumnInfo(name = "SCORE_CSHOW")
    var scoreCshow:Int = 0

)