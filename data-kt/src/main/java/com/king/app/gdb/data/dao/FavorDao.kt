package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.king.app.gdb.data.entity.FavorRecord
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.FavorStar
import com.king.app.gdb.data.entity.FavorStarOrder

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
    fun updateFavorRecordOrder(bean: FavorRecordOrder)

    @Query("select t.* from favor_order_record t join favor_record fr on t._id=fr.ORDER_ID where fr.RECORD_ID=:recordId")
    fun getRecordOrders(recordId: Long): List<FavorRecordOrder>

    @Query("delete from favor_record where RECORD_ID=:recordId and ORDER_ID=:orderId")
    fun deleteRecordFromOrder(recordId: Long, orderId: Long)

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

    @Query("select * from favor_star where STAR_ID=:starId and ORDER_ID=:orderId")
    fun getFavorStarBy(starId: Long, orderId: Long): FavorStar?

    @Query("select * from favor_order_star where _id=:orderId")
    fun getFavorStarOrderBy(orderId: Long): FavorStarOrder?

    @Update
    fun updateFavorStarOrder(bean: FavorStarOrder)

}