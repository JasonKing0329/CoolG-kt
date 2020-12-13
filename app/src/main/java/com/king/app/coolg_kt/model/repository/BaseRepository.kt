package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.CoolApplication
import com.king.app.gdb.data.AppDatabase

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/6 17:17
 */
abstract class BaseRepository {

    fun getDatabase(): AppDatabase = CoolApplication.instance.database!!
}