package com.king.app.gdb.data.dao

import androidx.room.Dao
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
}