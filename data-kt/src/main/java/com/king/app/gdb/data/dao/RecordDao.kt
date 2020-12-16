package com.king.app.gdb.data.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
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
    fun getRecord(id: Long): RecordWrap

    @Query("select * from record")
    fun getAllRecords(): List<RecordWrap>

    @Query("select * from record")
    fun getAllBasicRecords(): List<Record>

    @Query("select * from record order by SCORE desc")
    fun getAllBasicRecordsOrderByScore(): List<Record>

    @Query("select count(*) from count_record")
    fun countRecordCountSize(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountRecords(list: List<CountRecord>)

    @RawQuery
    fun getRecordsBySql(query: SupportSQLiteQuery): List<RecordWrap>

    @Query("select * from record_star where RECORD_ID=:recordId")
    fun getRecordStars(recordId: Long): List<RecordStarWrap>

}