package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.king.app.gdb.data.entity.Star
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

    @Insert
    fun insertTags(list: List<Tag>)

    @Insert
    fun insertTagStars(list: List<TagStar>)

    @Insert
    fun insertTagRecords(list: List<TagRecord>)

    @Query("delete from tag")
    fun deleteTags()

    @Query("delete from tag_star")
    fun deleteTagStars()

    @Query("delete from tag_record")
    fun deleteTagRecords()

}