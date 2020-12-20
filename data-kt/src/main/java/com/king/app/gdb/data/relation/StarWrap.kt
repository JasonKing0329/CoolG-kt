package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.king.app.gdb.data.entity.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:04
 */
data class StarWrap (

    @Embedded
    var bean: Star,

    @Relation(parentColumn = "_id",
        entityColumn = "STAR_ID")
    var rating: StarRating? = null,

    @Relation(parentColumn = "_id",
        entityColumn = "_id")
    var countStar: CountStar? = null,

    @Relation(parentColumn = "_id",
        entityColumn = "_id",
        entity = Record::class,
        associateBy = Junction(RecordStar::class, parentColumn = "STAR_ID", entityColumn = "RECORD_ID")
    )
    var recordList: List<Record>

)

data class TopStarWrap (
    @Embedded
    var bean:TopStar,

    @Relation(parentColumn = "STAR_ID",
        entityColumn = "_id")
    var star:Star
)

data class StarRelationship (
    @Embedded
    var star: Star,
    var count: Int = 0
) {
    var imagePath: String? = null
}

data class StarStudioTag (
    var studioId: Long?,
    var name: String?,
    var count: Int = 0
)