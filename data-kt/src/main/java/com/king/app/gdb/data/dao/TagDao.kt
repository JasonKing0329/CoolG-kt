package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Insert
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

    @Query("select * from tag where TYPE=:type")
    fun getTagsByType(type:Int): List<Tag>

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

    @Query("delete from tag where _id=:tagId")
    fun deleteTagById(tagId: Long)

    @Query("delete from tag_record where TAG_ID=:tagId")
    fun deleteTagRecordsByTag(tagId: Long)

    @Query("delete from tag_record where TAG_ID=:tagId and RECORD_ID=:recordId")
    fun deleteTagRecordsBy(tagId: Long, recordId: Long)

    @Query("delete from tag_star where TAG_ID=:tagId and STAR_ID=:starId")
    fun deleteTagStarBy(tagId: Long, starId: Long)

    @Query("delete from tag_star where TAG_ID=:tagId")
    fun deleteTagStarsByTag(tagId: Long)

    @Query("select count(*) from tag where NAME=:name and TYPE=:type")
    fun getTagCountBy(name: String, type: Int): Int

    @Query("select count(*) from tag_record where TAG_ID=:tagId")
    fun countRecordTagItems(tagId: Long): Int

    @Query("select count(*) from tag_star where TAG_ID=:tagId")
    fun countStarTagItems(tagId: Long): Int

    @Query("select t.* from tag t join tag_record tr on t._id=tr.TAG_ID where tr.RECORD_ID =:recordId")
    fun getRecordTags(recordId: Long): List<Tag>

    @Query("select count(*) from tag_record where RECORD_ID=:recordId and TAG_ID=:tagId")
    fun countRecordTag(recordId: Long, tagId: Long): Int

    @Query("select count(*) from tag_star where STAR_ID=:starId and TAG_ID=:tagId")
    fun countStarTag(starId: Long, tagId: Long): Int

    @Query("select t.* from tag t join tag_star tr on t._id=tr.TAG_ID where tr.STAR_ID =:starId")
    fun getStarTags(starId: Long): List<Tag>

}