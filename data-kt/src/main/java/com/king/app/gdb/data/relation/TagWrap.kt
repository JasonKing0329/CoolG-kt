package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagRecord
import com.king.app.gdb.data.entity.TagStar

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:32
 */
data class TagRecordWrap(
    @Embedded
    var bean: TagRecord,

    @Relation(parentColumn = "tag_id",
        entityColumn = "_id")
    var tag: Tag? = null
)
data class TagStarWrap(
    @Embedded
    var bean: TagStar,

    @Relation(parentColumn = "tag_id",
        entityColumn = "_id")
    var tag: Tag? = null
)