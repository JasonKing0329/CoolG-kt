package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.king.app.gdb.data.entity.*
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

    @Update
    fun updateStar(star: Star)

    @Query("select * from star_rating")
    fun getAllStarRatings(): List<StarRating>

    @Query("select * from star_rating order by COMPLEX desc")
    fun getAllStarRatingsDesc(): List<StarRating>

    @Query("select * from star_category")
    fun getAllTopStarCategory(): List<TopStarCategory>

    @Query("select * from star_category_details")
    fun getAllTopStar(): List<TopStar>

    @Insert
    fun insertStarRatings(list: List<StarRating>)

    @Insert
    fun insertTopStarCategories(list: List<TopStarCategory>)

    @Insert
    fun insertTopStars(list: List<TopStar>)

    @Query("select count(*) from count_star")
    fun countStarCountSize(): Int

    @Insert
    fun insertCountStars(list: List<CountStar>)

    @Query("delete from star_rating")
    fun deleteStarRatings()

    @Query("delete from star_category")
    fun deleteTopStarCategories()

    @Query("delete from star_category_details")
    fun deleteTopStars()
}