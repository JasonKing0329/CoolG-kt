package com.king.app.coolg_kt

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.utils.CrashHandler
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.gdb.data.AppDatabase


/**
 * @description:
 * @author：Jing
 * @date: 2020/12/11 21:41
 */
class CoolApplication: Application() {

    companion object {
        lateinit var instance:CoolApplication
    }

    var database: AppDatabase? = null
    var activityCount = 0

    override fun onCreate() {
        super.onCreate()
        instance = this

        // android 7开始，intent发送uri暴露file://的方法存在权限问题，用该方法避免
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }

        // 采集崩溃信息存储到本地
        CrashHandler.getInstance().init(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                activityCount --
                DebugLog.e("activityCount=$activityCount")
                // 最后一个activity退出时关闭数据库，否则.db会一直以.db, .db-shm, .db-wal三个文件存在，经测试发现room的写操作应该是先写进了.db-wal这个文件
                // 因此，如果不关闭，直接导出.db的话，数据会不全
                if (activityCount <= 0) {
                    database?.destroy()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityCount ++
                DebugLog.e("activityCount=$activityCount")
            }

            override fun onActivityResumed(activity: Activity) {

            }
        })
    }

    fun createDatabase() {
        database = AppDatabase.getInstance(this, AppConfig.GDB_DB_FULL_PATH)
    }

    fun reCreateDatabase() {
        database = AppDatabase.newInstance(this, AppConfig.GDB_DB_FULL_PATH)
    }
}