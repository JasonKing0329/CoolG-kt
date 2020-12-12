package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Query
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
}