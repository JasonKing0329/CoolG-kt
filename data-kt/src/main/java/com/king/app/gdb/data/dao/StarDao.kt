package com.king.app.gdb.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.king.app.gdb.data.bean.StarWrapWithCount
import com.king.app.gdb.data.entity.*
import com.king.app.gdb.data.relation.DebutStar
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

    @Query("select * from stars order by NAME COLLATE NOCASE")
    fun getAllStarsOrderByName(): List<StarWrap>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStars(list: List<Star>)

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

    @Query("delete from stars")
    fun deleteStars()

    @Query("delete from star_rating")
    fun deleteStarRatings()

    @Query("delete from star_category")
    fun deleteTopStarCategories()

    @Query("delete from star_category_details")
    fun deleteTopStars()

    @Query("select * from stars where _id=:id")
    fun getStar(id: Long): Star?

    @Query("select * from stars where _id=:id")
    fun getStarWrap(id: Long): StarWrap?

    @Insert
    fun insertStarRating(bean: StarRating)

    @Update
    fun updateStarRating(bean: StarRating)

    @Query("select s.*, count(*) as count from stars s join record_star rs on s._id=rs.STAR_ID where rs.RECORD_ID in (select RECORD_iD from record_star where STAR_ID =:starId) and rs.STAR_ID !=:starId group by s._id")
    fun getStarRelationships(starId: Long): List<StarRelationship>

    @Query("SELECT fodr._id as studioId, fodr.name as name, count(fodr._id) AS count FROM favor_order_record fodr  LEFT JOIN record r ON fodr._id=r.studioId LEFT JOIN record_star rs ON r._id=rs.record_id WHERE rs.star_id=:starId AND fodr.parent_id=:studioParentId GROUP BY fodr._id ORDER BY count DESC")
    fun getStarStudioTag(starId: Long, studioParentId: Long): List<StarStudioTag>

    @RawQuery
    fun getStarsBySql(query: SupportSQLiteQuery): List<StarWrap>

    @Query("select * from stars s join star_rating sr on s._id=sr.STAR_ID where sr.COMPLEX>=:atLeast order by random() limit :num")
    fun getStarByRating(atLeast: Float, num: Int): List<Star>

    @Query("select s.*, count(s._id) as extraCount from record r join record_star rs on r._id=rs.RECORD_ID join stars s on rs.STAR_ID=s._id where r.studioId=:studioId group by s._id order by extraCount desc limit :num")
    fun getStudioTopStars(studioId: Long, num: Int): List<StarWrapWithCount>

    @Query("select s.* from record r join record_star rs on r._id=rs.RECORD_ID join stars s on rs.STAR_ID=s._id where r.studioId=:studioId group by s._id")
    fun getStudioStars(studioId: Long): List<Star>

    /**
     * 所有star在record中第一次出现的记录
     * sql难点：record_star中record_id与star_id是1：N，
     * 即，star_id会出现在多个record关联的记录中，导致关联查询出来的列表会有许多重复的star_id项（因为modify_time不重复）。
     * 1）如果想用distinct，就得去掉modify_time，但是modify_time是必须要的结果
     * 2）用groupBy，groupBy后能做到star_id不再重复，但modify_time在group过程中并不能按照后面order by来呈现，也即无法保证取到star_id对应的最早的modify_time
     * 3) 最终，仍然采用groupBy，只要在select中加上min(r.LAST_MODIFY_TIME)就为group的过程加了条件，能保证取到最早的modify_time
     */
    @Query("select rs.STAR_ID as starId, min(r.LAST_MODIFY_TIME) as debut, s.* from record r \n" +
            "join record_star rs on r._id=rs.RECORD_ID \n" +
            "join stars s on rs.STAR_ID = s._id \n" +
            "where r.LAST_MODIFY_TIME > 0 \n" +
            "group by rs.STAR_ID \n" +
            "order by r.LAST_MODIFY_TIME ")
    fun getDebutStar(): List<DebutStar>

}