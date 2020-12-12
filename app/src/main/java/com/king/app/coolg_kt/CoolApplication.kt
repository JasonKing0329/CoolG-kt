package com.king.app.coolg_kt

import android.app.Application
import android.os.Build
import android.os.StrictMode
import com.king.app.coolg_kt.conf.AppConfig
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

    override fun onCreate() {
        super.onCreate()
        instance = this

        // android 7开始，intent发送uri暴露file://的方法存在权限问题，用该方法避免
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }

    fun createDatabase() {
        database = AppDatabase.getInstance(this, AppConfig.GDB_DB_FULL_PATH)
    }
}