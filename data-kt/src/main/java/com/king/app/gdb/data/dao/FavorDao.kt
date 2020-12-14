package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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

}