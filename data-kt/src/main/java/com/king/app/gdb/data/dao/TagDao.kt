package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagRecord
import com.king.app.gdb.data.entity.TagStar

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 23:05
 */
@Dao
interface TagDao {
    @Query("select * from tag")
    fun getAllTags(): List<Tag>

    @Query("select * from tag_star")
    fun getAllTagStars(): List<TagStar>

    @Query("select * from tag_record")
    fun getAllTagRecords(): List<TagRecord>

}