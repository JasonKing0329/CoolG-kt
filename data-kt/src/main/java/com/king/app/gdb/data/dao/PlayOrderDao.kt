package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.king.app.gdb.data.entity.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 12:08
 */
@Dao
interface PlayOrderDao {

    @Query("select * from play_order where _id=:id")
    fun getPlayOrder(id: Long): PlayOrder

    @Insert
    fun insertPlayOrder(order: PlayOrder)

    @Query("select * from play_item")
    fun getAllPlayItems(): List<PlayItem>

    @Query("select * from play_order")
    fun getAllPlayOrders(): List<PlayOrder>

    @Query("select * from play_duration")
    fun getAllPlayDurations(): List<PlayDuration>

    @Query("select * from video_cover_star")
    fun getVideoCoverStars(): List<VideoCoverStar>

    @Query("select * from video_cover_order")
    fun getVideoCoverOrders(): List<VideoCoverPlayOrder>

    @Insert
    fun insertPlayItems(list: List<PlayItem>)

    @Insert
    fun insertPlayOrders(list: List<PlayOrder>)

    @Insert
    fun insertPlayDurations(list: List<PlayDuration>)

    @Insert
    fun insertVideoCoverStars(list: List<VideoCoverStar>)

    @Insert
    fun insertVideoCoverPlayOrders(list: List<VideoCoverPlayOrder>)

    @Query("delete from play_item")
    fun deletePlayItems()

    @Query("delete from play_order")
    fun deletePlayOrders()

    @Query("delete from play_duration")
    fun deletePlayDurations()

    @Query("delete from video_cover_star")
    fun deleteVideoCoverStars()

    @Query("delete from video_cover_order")
    fun deleteVideoCoverPlayOrders()

    @Insert
    fun updatePlayDuration(bean: PlayDuration)

    @Delete
    fun deletePlayDuration(bean: PlayDuration)

    @Query("select t.* from play_order t join play_item pi on t._id=pi.ORDER_ID where pi.RECORD_ID=:recordId")
    fun getRecordOrders(recordId: Long): List<PlayOrder>

    @Query("delete from play_item where RECORD_ID=:recordId")
    fun deleteRecordFromOrder(recordId: Long)

    @Query("select count(*) from play_item where RECORD_ID=:recordId and ORDER_ID=:orderId")
    fun countPlayItem(recordId: Long, orderId: Long): Int

    @Query("select * from play_duration where RECORD_ID=:recordId")
    fun getDurationByRecord(recordId: Long): PlayDuration?

}