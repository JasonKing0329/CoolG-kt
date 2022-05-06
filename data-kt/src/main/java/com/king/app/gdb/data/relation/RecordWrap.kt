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

    @Relation(parentColumn = "RECORD_DETAIL_ID",
        entityColumn = "_id")
    var recordType1v1: RecordType1v1? = null,

    @Relation(parentColumn = "RECORD_DETAIL_ID",
        entityColumn = "_id")
    var recordType3w: RecordType3w? = null,

    @Relation(parentColumn = "_id",
        entityColumn = "RECORD_ID")
    var recordStars: List<RecordStar>,

    @Relation(parentColumn = "_id",
        entityColumn = "_id",
        entity = Star::class,
        associateBy = Junction(RecordStar::class, parentColumn = "RECORD_ID", entityColumn = "STAR_ID")
    )
    var starList: List<Star>,

    @Relation(parentColumn = "_id",
        entityColumn = "_id")
    var countRecord: CountRecord? = null
) {
    var imageUrl: String? = null
    var canSelect: Boolean? = null
    var extraInfo: String? = null
}