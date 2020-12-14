package com.king.app.coolg_kt.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.gdb.data.dao.RecordDao
import com.king.app.gdb.data.dao.StarDao
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.*

/**
 * 描述: 支持start和bind方式
 * start方式直接后台运行
 * bind方式提供回调
 *
 * 作者：景阳
 *
 * 创建时间: 2017/11/23 10:35
 */
class FileService : Service() {
    private var disposable: Disposable? = null
    private var useMap = mutableMapOf<String, Boolean>()
    private var serviceCallback: IFileServiceCallback? = null
    private var isWorking = false
    override fun onBind(intent: Intent): IBinder? {
        DebugLog.e("")
        return FileBinder()
    }

    inner class FileBinder : Binder() {
        val service: FileService
            get() = this@FileService

        fun setServiceCallback(serviceCallback: IFileServiceCallback?) {
            this@FileService.serviceCallback = serviceCallback
        }

        fun removeServiceCallback() {
            serviceCallback = null
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        DebugLog.e("")
        if (!isWorking) {
            isWorking = true
            removeUselessFiles()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun removeUselessFiles() {
        DebugLog.e("")
        disposable =
            Observable.create { e: ObservableEmitter<Any?> ->
                removeUselessRecords()
                removeUselessStars()
                e.onNext(Any())
            }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { o: Any? ->
                        DebugLog.e("finished")
                        isWorking = false
                        if (serviceCallback != null) {
                            serviceCallback!!.onClearFinished()
                        }
                    }
                ) { throwable: Throwable -> throwable.printStackTrace() }
    }

    private fun removeUselessRecords() {
        val file = File(AppConfig.GDB_IMG_RECORD)
        val list = CoolApplication.instance.database!!.getRecordDao().getAllBasicRecords()
        useMap.clear()
        list.forEach { record ->
            record.name?.let {
                useMap[it] = true
            }
        }
        file.listFiles().forEach {
            removeFiles(it)
        }
    }

    private fun removeUselessStars() {
        val file = File(AppConfig.GDB_IMG_STAR)
        val list = CoolApplication.instance.database!!.getStarDao().getAllBasicStars()
        useMap.clear()
        list.forEach { record ->
            record.name?.let {
                useMap[it] = true
            }
        }
        file.listFiles().forEach {
            removeFiles(it)
        }
    }

    /**
     * 文件层级结构类型如下
     * 1. 直接以 key.图片格式 命名的文件
     * 2. 以key命名的目录
     * @param file
     */
    private fun removeFiles(file: File) {
        if (file.isDirectory) {
            if (useMap[file.name] == null) {
                DebugLog.e(file.path)
                FileUtil.deleteFile(file)
            }
        } else {
            if (file.name == AppConfig.FILE_NOMEDIA) {
                return
            }
            // 数据库里没有相关引用就删除
            if (file.name.contains(".")) {
                val name =
                    file.name.substring(0, file.name.lastIndexOf("."))
                if (useMap[name] == null) {
                    DebugLog.e(file.path)
                    file.delete()
                }
            }
        }
    }

    override fun onDestroy() {
        DebugLog.e("")
        serviceCallback = null
        disposable?.dispose()
        super.onDestroy()
    }
}