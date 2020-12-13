package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Query

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 16:08
 */
@Dao
interface PropertyDao {

    @Query("select VALUE from properties where `KEY`='version'")
    fun getVersion(): String
}