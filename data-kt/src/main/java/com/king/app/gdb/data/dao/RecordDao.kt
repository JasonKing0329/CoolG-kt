package com.king.app.gdb.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.bean.RecordScene
import com.king.app.gdb.data.entity.CountRecord
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.RecordStarWrap
import com.king.app.gdb.data.relation.RecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:59
 */
@Dao
interface RecordDao {

    @Query("select * from record where _id=:id")
    fun getRecordBasic(id: Long): Record

    @Query("select * from record where _id=:id")
    fun getRecord(id: Long): RecordWrap?

    @Query("select * from record")
    fun getAllRecords(): List<RecordWrap>

    @Query("select r._id as recordId, cr.RANK as rank, 0 as seed from record r join count_record cr on r._id = cr.RANK order by cr.RANK")
    fun getRankRecords(): List<RankRecord>

    @Query("select * from record")
    fun getAllBasicRecords(): List<Record>

    @Query("select * from record order by SCORE desc")
    fun getAllBasicRecordsOrderByScore(): List<Record>

    @Query("select count(*) from count_record")
    fun countRecordCountSize(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountRecords(list: List<CountRecord>)

    @Query("select * from record_star where RECORD_ID=:recordId")
    fun getRecordStars(recordId: Long): List<RecordStarWrap>

    @Query("select * from record order by LAST_MODIFY_TIME desc limit :start,:num")
    fun getLatestRecords(start: Int, num: Int): List<RecordWrap>

    @RawQuery
    fun getRecordsBySql(query: SupportSQLiteQuery): List<RecordWrap>

    @Query("select * from record where DEPRECATED=0 order by LAST_MODIFY_TIME desc limit :start, :num")
    fun getOnlineRecords(start: Int, num: Int): List<RecordWrap>

    @Query("select r.* from record r join record_star rs on r._id = rs.RECORD_ID where rs.STAR_ID=:starId and r.DEPRECATED=0 order by r.SCORE desc")
    fun getStarOnlineRecords(starId: Long): List<RecordWrap>

    @Query("select count(*) from record r join record_star rs on r._id = rs.RECORD_ID where rs.STAR_ID=:starId and r.DEPRECATED=0")
    fun countStarOnlineRecords(starId: Long): Int

    @Query("select SCENE as name, count(SCENE) as number from record group by SCENE")
    fun getAllScenes(): List<RecordScene>

    @Query("select r.* from favor_record fr join record r on fr.RECORD_ID=r._id where fr.ORDER_ID=:studioId order by r.SCORE desc limit :num")
    fun getStudioTopRecords(studioId: Long, num: Int): List<RecordWrap>

    @Query("select r.* from favor_record fr join record r on fr.RECORD_ID=r._id where fr.ORDER_ID=:studioId order by r.LAST_MODIFY_TIME desc limit :num")
    fun getStudioRecentRecords(studioId: Long, num: Int): List<RecordWrap>

}