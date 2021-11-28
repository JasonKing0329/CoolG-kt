package com.king.app.gdb.data.dao

import com.king.app.gdb.data.AppDatabase

/**
 * Desc: 执行一些无法定义在Dao里的sql语句
 * @author：Jing Yang
 * @date: 2021/11/24 14:28
 */
object PureSqlDao {

    fun createHighRank(database: AppDatabase) {
        database.openHelper.writableDatabase.execSQL("insert into temp_high_rank select recordId, min(rank) as high from match_rank_record group by recordId order by high")
    }

    fun createHighRank(database: AppDatabase, lessEqThan: Int) {
        database.openHelper.writableDatabase.execSQL("insert into temp_high_rank select recordId, min(rank) as high from match_rank_record where rank<=$lessEqThan group by recordId order by high")
    }
}