package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.entity.StarRating
import com.king.app.gdb.data.entity.TopStar
import com.king.app.gdb.data.entity.TopStarCategory
import com.king.app.gdb.data.relation.StarWrap

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 12:11
 */
@Dao
interface StarDao {

    @Query("select * from stars")
    fun getAllStars(): List<StarWrap>

    @Query("select * from stars")
    fun getAllBasicStars(): List<Star>

    @Query("select * from star_rating")
    fun getAllStarRatings(): List<StarRating>

    @Query("select * from star_category")
    fun getAllTopStarCategory(): List<TopStarCategory>

    @Query("select * from star_category_details")
    fun getAllTopStar(): List<TopStar>
}