package com.king.app.gdb.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.king.app.gdb.data.entity.FavorRecord
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.FavorStar
import com.king.app.gdb.data.entity.FavorStarOrder
import com.king.app.gdb.data.relation.FavorRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 22:58
 */
@Dao
interface FavorDao {

    @Query("select * from favor_record")
    fun getAllFavorRecords(): List<FavorRecord>

    @Query("select * from favor_star")
    fun getAllFavorStars(): List<FavorStar>

    @Query("select * from favor_order_record")
    fun getAllFavorRecordOrders(): List<FavorRecordOrder>

    @Query("select * from favor_order_star")
    fun getAllFavorStarOrders(): List<FavorStarOrder>

    @Insert
    fun insertFavorRecords(list: List<FavorRecord>)

    @Insert
    fun insertFavorStars(list: List<FavorStar>)

    @Insert
    fun insertFavorRecordOrders(list: List<FavorRecordOrder>)

    @Insert
    fun insertFavorStarOrders(list: List<FavorStarOrder>)

    @Query("delete from favor_record")
    fun deleteFavorRecords()

    @Query("delete from favor_star")
    fun deleteFavorStars()

    @Query("delete from favor_order_record")
    fun deleteFavorRecordOrders()

    @Query("delete from favor_order_star")
    fun deleteFavorStarOrders()

    @Update
    fun updateFavorRecord(bean: FavorRecord)

    @Delete
    fun deleteFavorRecord(bean: FavorRecord)

    @Update
    fun updateFavorRecordOrder(bean: FavorRecordOrder)

    @Delete
    fun deleteFavorRecordOrder(bean: FavorRecordOrder)

    @Update
    fun updateFavorRecordOrders(list: List<FavorRecordOrder>)

    @Query("select t.* from favor_order_record t join favor_record fr on t._id=fr.ORDER_ID where fr.RECORD_ID=:recordId")
    fun getRecordOrders(recordId: Long): List<FavorRecordOrder>

    @Query("delete from favor_record where RECORD_ID=:recordId and ORDER_ID=:orderId")
    fun deleteRecordFromOrder(recordId: Long, orderId: Long)

    @Query("delete from favor_record where ORDER_ID=:orderId")
    fun deleteAllRecordsInOrder(orderId: Long)

    @Query("select * from favor_record where RECORD_ID=:recordId and ORDER_ID=:orderId")
    fun getFavorRecordBy(recordId: Long, orderId: Long): FavorRecord?

    @Query("select * from favor_order_record where _id=:orderId")
    fun getFavorRecordOrderBy(orderId: Long): FavorRecordOrder?

    @Query("select t.* from favor_order_star t join favor_star fr on t._id=fr.ORDER_ID where fr.STAR_ID=:starId")
    fun getStarOrders(starId: Long): List<FavorStarOrder>

    @Query("delete from favor_star where STAR_ID=:starId and ORDER_ID=:orderId")
    fun deleteStarFromOrder(starId: Long, orderId: Long)

    @Query("select * from favor_order_record where NAME=:name")
    fun getRecordOrderByName(name: String): FavorRecordOrder?

    @Query("select * from favor_order_record where NAME=:name and PARENT_ID=:studioParentId")
    fun getStudioByName(name: String, studioParentId: Long): FavorRecordOrder?

    @Query("select fo.* from favor_order_record fo join favor_record fr on fo._id=fr.ORDER_ID where fr.RECORD_ID=:recordId and fo.PARENT_ID=:studioParentId")
    fun getStudioByRecord(recordId: Long, studioParentId: Long): FavorRecordOrder?

    @Query("select fr.* from favor_record fr join favor_order_record fo on fo._id=fr.ORDER_ID where fr.RECORD_ID=:recordId and fo.PARENT_ID=:studioParentId")
    fun getStudioRelationByRecord(recordId: Long, studioParentId: Long): List<FavorRecordWrap>

    @Query("select * from favor_star where STAR_ID=:starId and ORDER_ID=:orderId")
    fun getFavorStarBy(starId: Long, orderId: Long): FavorStar?

    @Query("select * from favor_order_star where _id=:orderId")
    fun getFavorStarOrderBy(orderId: Long): FavorStarOrder?

    @Update
    fun updateFavorStarOrder(bean: FavorStarOrder)

    @Query("select count(*) from favor_record where ORDER_ID=:orderId")
    fun countRecordOrderItems(orderId: Long): Int

    @RawQuery
    fun getRecordOrdersBySql(query: SupportSQLiteQuery): List<FavorRecordOrder>

    @Query("select count(*) from (select rs.STAR_ID as num from favor_record fr join record_star rs on fr.RECORD_ID=rs.RECORD_ID where fr.ORDER_ID=:orderId group by rs.STAR_ID)")
    fun countStudioStarNumber(orderId: Long): Int

    @Query("select count(*) from favor_record fr join record r on fr.RECORD_ID=r._id where fr.ORDER_ID=:orderId and r.SCORE>=:score")
    fun countRecordScoreOver(orderId: Long, score: Int): Int

    @Query("select * from favor_order_record where NAME='Studio'")
    fun getStudioOrder(): FavorRecordOrder?

}