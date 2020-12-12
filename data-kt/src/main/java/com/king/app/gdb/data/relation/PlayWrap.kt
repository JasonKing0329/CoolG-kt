package com.king.app.gdb.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.king.app.gdb.data.entity.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:40
 */
data class PlayItemWrap (
    @Embedded
    var bean: PlayItem,

    @Relation(parentColumn = "order_id",
        entityColumn = "_id")
    var playOrder: PlayOrder? = null,

    @Relation(parentColumn = "record_id",
        entityColumn = "_id")
    var record:Record? = null
)
data class PlayOrderWrap (
    @Embedded
    var bean: PlayOrder,

    @Relation(parentColumn = "_id",
        entityColumn = "order_id")
    var itemList: List<PlayItem>
)

data class VideoCoverPlayOrderWrap (
    @Embedded
    var bean: VideoCoverPlayOrder,

    @Relation(parentColumn = "order_id",
        entityColumn = "_id")
    var playOrder: PlayOrder? = null
)
data class VideoCoverStarWrap(
    @Embedded
    var bean: VideoCoverStar,

    @Relation(parentColumn = "star_id",
        entityColumn = "_id")
    var playOrder: Star? = null
)