package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.king.app.gdb.data.entity.CountRecord
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.RecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 11:59
 */
@Dao
interface RecordDao {

    @Query("select * from record")
    fun getAllRecords(): List<RecordWrap>

    @Query("select * from record order by SCORE desc")
    fun getAllBasicRecordsOrderByScore(): List<Record>

    @Query("select count(*) from count_record")
    fun countRecordCountSize(): Int

    @Insert
    fun insertCountRecords(list: List<CountRecord>)

}