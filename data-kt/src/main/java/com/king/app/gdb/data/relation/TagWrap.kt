package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.king.app.gdb.data.entity.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:32
 */
data class TagRecordWrap(
    @Embedded
    var bean: TagRecord,

    @Relation(parentColumn = "TAG_ID",
        entityColumn = "_id")
    var tag: Tag? = null
)
data class TagStarWrap(
    @Embedded
    var bean: TagStar,

    @Relation(parentColumn = "TAG_ID",
        entityColumn = "_id")
    var tag: Tag? = null
)

data class TagClassWrap (

    @Embedded
    var bean: TagClass,

    @Relation(parentColumn = "id",
        entityColumn = "_id",
        entity = Tag::class,
        associateBy = Junction(TagClassItem::class, parentColumn = "classId", entityColumn = "tagId")
    )
    var itemList: List<Tag>

)