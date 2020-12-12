package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.king.app.gdb.data.entity.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:18
 */
data class RecordWrap (

    @Embedded
    var bean:Record,

    @Relation(parentColumn = "record_detail_id",
        entityColumn = "_id")
    var recordType1v1: RecordType1v1? = null,

    @Relation(parentColumn = "record_detail_id",
        entityColumn = "_id")
    var recordType3w: RecordType3w? = null,

    @Relation(parentColumn = "_id",
        entityColumn = "record_id")
    var relationList: List<RecordStar>,

    @Relation(parentColumn = "_id",
        entityColumn = "_id",
        entity = Star::class,
        associateBy = Junction(RecordStar::class, parentColumn = "record_id", entityColumn = "star_id")
    )
    var starList: List<Star>,

    @Relation(parentColumn = "_id",
        entityColumn = "record_id")
    var countRecord: CountRecord? = null

)