package com.king.app.coolg_kt.model.db

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.gdb.data.AppDatabase
import com.king.app.gdb.data.entity.CountRecord
import com.king.app.gdb.data.entity.CountStar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/2 11:29
 */
open class DbUpgradeEngine {

    fun closeDatabase() {
        getDatabase().destroy()
    }

    fun getDatabase(): AppDatabase {
        return CoolApplication.instance.database!!
    }

    /**
     * CountStar and CountRecord
     */
    fun createCountData() {
        var ratings = getDatabase().getStarDao().getAllStarRatingsDesc()
        var countStars = mutableListOf<CountStar>()
        ratings.forEachIndexed { index, starRating ->
            countStars.add(CountStar(starRating.starId, index + 1))
        }
        getDatabase().getStarDao().insertCountStars(countStars)

        var records = getDatabase().getRecordDao().getAllBasicRecordsOrderByScore()
        var countRecords = mutableListOf<CountRecord>()
        records.forEachIndexed { index, record ->
            countRecords.add(CountRecord(record.id, index + 1))
        }
        getDatabase().getRecordDao().insertCountRecords(countRecords)
    }

    fun backupDatabase() {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        FileUtil.copyFile(
            File("${AppConfig.APP_DIR_CONF}/${AppConfig.DB_NAME}"),
            File("${AppConfig.APP_DIR_DB_HISTORY}/${sdf.format(Date())}.db")
        )
    }
}