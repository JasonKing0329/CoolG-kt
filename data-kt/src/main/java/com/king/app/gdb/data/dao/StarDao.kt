package com.king.app.gdb.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.king.app.gdb.data.entity.*
import com.king.app.gdb.data.relation.StarRelationship
import com.king.app.gdb.data.relation.StarStudioTag
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountStars(list: List<CountStar>)

    @Query("delete from star_rating")
    fun deleteStarRatings()

    @Query("delete from star_category")
    fun deleteTopStarCategories()

    @Query("delete from star_category_details")
    fun deleteTopStars()

    @Query("select * from stars where _id=:id")
    fun getStar(id: Long): Star?

    @Query("select * from stars where _id=:id")
    fun getStarWrap(id: Long): StarWrap

    @Insert
    fun insertStarRating(bean: StarRating)

    @Update
    fun updateStarRating(bean: StarRating)

    @Query("select s.*, count(*) as count from stars s join record_star rs on s._id=rs.STAR_ID where rs.RECORD_ID in (select RECORD_iD from record_star where STAR_ID =:starId) and rs.STAR_ID !=:starId group by s._id")
    fun getStarRelationships(starId: Long): List<StarRelationship>

    @Query("SELECT fodr._id as studioId, fodr.name as name, count(fodr._id) AS count FROM favor_order_record fodr  LEFT JOIN favor_record fr ON fodr._id=fr.order_id LEFT JOIN record r ON fr.record_id=r._id LEFT JOIN record_star rs ON r._id=rs.record_id  WHERE rs.star_id=:starId AND fodr.parent_id=:orderId GROUP BY fodr._id ORDER BY count DESC")
    fun getStarStudioTag(starId: Long, orderId: Long): List<StarStudioTag>

    @RawQuery
    fun getStarsBySql(query: SupportSQLiteQuery): List<StarWrap>

    @Query("select * from stars s join star_rating sr on s._id=sr.STAR_ID where sr.COMPLEX>=:atLeast order by random() limit :num")
    fun getStarByRating(atLeast: Float, num: Int): List<Star>
}