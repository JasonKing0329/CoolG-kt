package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.king.app.gdb.data.entity.GProperties

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 16:08
 */
@Dao
interface PropertyDao {

    @Query("select VALUE from properties where `KEY`='version'")
    fun getVersion(): String

    @Query("select * from properties")
    fun getProperties(): List<GProperties>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProperties(list: List<GProperties>)

    @Query("delete from properties")
    fun deleteProperties()

}