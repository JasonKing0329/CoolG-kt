package com.king.app.gdb.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.king.app.gdb.data.bean.RankRecord
import com.king.app.gdb.data.bean.RecordScene
import com.king.app.gdb.data.entity.*
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
    fun getRecordBasic(id: Long): Record?

    @Query("select * from record where _id=:id")
    fun getRecord(id: Long): RecordWrap?

    @Query("select * from record")
    fun getAllRecords(): List<RecordWrap>

    @Query("select r._id as recordId, cr.RANK as rank, 0 as seed from record r join count_record cr on r._id = cr._id where cr.RANK<=:limitMax order by cr.RANK")
    fun getRankRecords(limitMax: Int): List<RankRecord>

    @Query("select * from record")
    fun getAllBasicRecords(): List<Record>

    @Query("select * from record order by SCORE desc")
    fun getAllBasicRecordsOrderByScore(): List<Record>

    @Query("select * from record_type1")
    fun getAllRecordType1v1(): List<RecordType1v1>

    @Query("select * from record_type3")
    fun getAllRecordType3w(): List<RecordType3w>

    @Query("select * from record_star")
    fun getAllRecordStars(): List<RecordStar>

    @Query("select count(*) from count_record")
    fun countRecordCountSize(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountRecords(list: List<CountRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecords(list: List<Record>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecordType1v1(list: List<RecordType1v1>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecordType3w(list: List<RecordType3w>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecordStars(list: List<RecordStar>)

    @Update
    fun updateRecord(record: Record)

    @Query("select * from record_star where RECORD_ID=:recordId")
    fun getRecordStars(recordId: Long): List<RecordStarWrap>

    @Query("select * from record order by LAST_MODIFY_TIME desc limit :start,:num")
    fun getLatestRecords(start: Int, num: Int): List<RecordWrap>

    @RawQuery
    fun getRecordsBySql(query: SupportSQLiteQuery): List<RecordWrap>

    @Query("select r.* from record r left join match_rank_record mrr on r._id=mrr.recordId and mrr.period=:period and mrr.orderInPeriod=:orderInPeriod where mrr.id is null order by r.SCORE desc")
    fun getRecordsOutOfRank(period: Int, orderInPeriod: Int): List<RecordWrap>

    @Query("select * from record where DEPRECATED=0 order by LAST_MODIFY_TIME desc limit :start, :num")
    fun getOnlineRecords(start: Int, num: Int): List<RecordWrap>

    @Query("select r.* from record r join record_star rs on r._id = rs.RECORD_ID where rs.STAR_ID=:starId and r.DEPRECATED=0 order by r.SCORE desc")
    fun getStarOnlineRecords(starId: Long): List<RecordWrap>

    @Query("select count(*) from record r join record_star rs on r._id = rs.RECORD_ID where rs.STAR_ID=:starId and r.DEPRECATED=0")
    fun countStarOnlineRecords(starId: Long): Int

    @Query("select SCENE as name, count(SCENE) as number from record group by SCENE")
    fun getAllScenes(): List<RecordScene>

    @Query("select * from record where studioId=:studioId order by SCORE desc limit :num")
    fun getStudioTopRecords(studioId: Long, num: Int): List<RecordWrap>

    @Query("select * from record where studioId=:studioId order by LAST_MODIFY_TIME desc limit :num")
    fun getStudioRecentRecords(studioId: Long, num: Int): List<RecordWrap>

    @Query("select * from record where studioId=0")
    fun getRecordsWithoutStudio(): List<RecordWrap>

    @Query("delete from record")
    fun deleteRecords()

    @Query("delete from record_type1")
    fun deleteRecordType1v1()

    @Query("delete from record_type3")
    fun deleteRecordType3w()

    @Query("delete from record_star")
    fun deleteRecordStars()

    @Query("select count(*) from record where studioId=:studioId")
    fun getStudioCount(studioId: Long): Int

}