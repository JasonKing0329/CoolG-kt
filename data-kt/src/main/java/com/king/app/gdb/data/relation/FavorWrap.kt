package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.king.app.gdb.data.entity.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:36
 */
data class FavorRecordWrap (

    @Embedded
    var bean: FavorRecord,

    @Relation(parentColumn = "RECORD_ID",
        entityColumn = "_id")
    var record: Record?,

    @Relation(parentColumn = "ORDER_ID",
        entityColumn = "_id")
    var order: FavorRecordOrder?
)
data class FavorRecordOrderWrap (

    @Embedded
    var bean: FavorRecordOrder,

    @Relation(parentColumn = "_id",
        entityColumn = "PARENT_ID")
    var childList: List<FavorRecordOrder>,

    @Relation(parentColumn = "PARENT_ID",
        entityColumn = "_id")
    var parent : FavorRecordOrder,

    @Relation(parentColumn = "_id",
        entityColumn = "_id",
        entity = Record::class,
        associateBy = Junction(FavorRecord::class, parentColumn = "ORDER_ID", entityColumn = "RECORD_ID")
    )
    var recordList: List<Record>

)

data class FavorStarWrap (

    @Embedded
    var bean: FavorStar,

    @Relation(parentColumn = "STAR_ID",
        entityColumn = "_id")
    var star: Star,

    @Relation(parentColumn = "ORDER_ID",
        entityColumn = "_id")
    var order: FavorStarOrder
)
data class FavorStarOrderWrap (

    @Embedded
    var bean: FavorStarOrder,

    @Relation(parentColumn = "_id",
        entityColumn = "PARENT_ID")
    var childList: List<FavorStarOrder>,

    @Relation(parentColumn = "PARENT_ID",
        entityColumn = "_id")
    var parent : FavorStarOrder,

    @Relation(parentColumn = "_id",
        entityColumn = "_id",
        entity = Star::class,
        associateBy = Junction(FavorStar::class, parentColumn = "ORDER_ID", entityColumn = "STAR_ID")
    )
    var recordList: List<Star>

)

data class StudioStarCountWrap (

    @Embedded
    var bean: FavorRecordOrder,
    var count: Int
)