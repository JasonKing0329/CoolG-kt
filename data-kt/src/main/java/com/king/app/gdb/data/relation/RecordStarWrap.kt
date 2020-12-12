package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.king.app.gdb.data.entity.RecordStar
import com.king.app.gdb.data.entity.Star

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:11
 */
data class RecordStarWrap (
    @Embedded
    var bean:RecordStar,

    @Relation(parentColumn = "STAR_ID",
        entityColumn = "_id")
    var star: Star
)