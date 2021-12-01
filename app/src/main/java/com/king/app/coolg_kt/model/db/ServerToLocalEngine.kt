package com.king.app.coolg_kt.model.db

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.gdb.data.AppDatabase
import io.reactivex.rxjava3.core.Observable

/**
 * Desc: 本地数据库不变，将server数据插入到本地
 * @author：Jing Yang
 * @date: 2021/7/2 13:17
 */
class ServerToLocalEngine:DbUpgradeEngine() {

    fun transformData(): Observable<Boolean> {
        return Observable.create {
            // 加载temp目录数据库，将更新内容提取到内存
            AppDatabase.newInstance(CoolApplication.instance, AppConfig.GDB_DB_TEMP_FULL_PATH)
            val recordDao = AppDatabase.instance!!.getRecordDao()
            val starDao = AppDatabase.instance!!.getStarDao()
            val favorDao = AppDatabase.instance!!.getFavorDao()
            val propDao = AppDatabase.instance!!.getPropertyDao()
            val serverData = ServerData(
                recordDao.getAllBasicRecords(),
                recordDao.getAllRecordType1v1(),
                recordDao.getAllRecordType3w(),
                recordDao.getAllRecordStars(),
                starDao.getAllBasicStars(),
                favorDao.getAllFavorRecordOrders(),
                propDao.getProperties()
            )
            AppDatabase.instance!!.destroy()

            // 先将本地数据库备份至History文件夹
            backupDatabase()
            // 重新加载本地数据库，更新内容
            CoolApplication.instance.reCreateDatabase()
            AppDatabase.instance!!.getRecordDao().let { dao ->
                dao.deleteRecords()
                dao.deleteRecordType1v1()
                dao.deleteRecordType3w()
                dao.deleteRecordStars()
                dao.insertRecords(serverData.recordList)
                dao.insertRecordType1v1(serverData.recordType1v1List)
                dao.insertRecordType3w(serverData.recordType3wList)
                dao.insertRecordStars(serverData.recordStarList)
            }
            AppDatabase.instance!!.getStarDao().let { dao ->
                dao.deleteStars()
                dao.insertStars(serverData.starList)
            }
            AppDatabase.instance!!.getPropertyDao().let { dao ->
                dao.deleteProperties()
                dao.insertProperties(serverData.properties)
            }
            AppDatabase.instance!!.getFavorDao().let { dao ->
                // 替换本地设置的imageUrl
                val map = mutableMapOf<Long, String?>()
                dao.getAllFavorRecordOrders().forEach { item ->
                    map[item.id!!] = item.coverUrl
                }
                dao.deleteFavorRecordOrders()
                dao.insertFavorRecordOrders(serverData.favorRecordOrderList)
            }
            // 重新计算star rank and record rank(score)
            createCountData()
            it.onNext(true)
            it.onComplete()
        }
    }
}