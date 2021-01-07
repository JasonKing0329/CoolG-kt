package com.king.app.gdb.data.bean

import androidx.room.Embedded
import androidx.room.Relation
import com.king.app.gdb.data.entity.CountStar
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.StarRating

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 11:16
 */
data class StarWrapWithCount (

    @Embedded
    var bean: Star,

    var extraCount: Int = 0,

    @Relation(parentColumn = "_id",
        entityColumn = "STAR_ID")
    var rating: StarRating? = null,

    @Relation(parentColumn = "_id",
        entityColumn = "_id")
    var countStar: CountStar? = null
){
    var imagePath: String? = null
}