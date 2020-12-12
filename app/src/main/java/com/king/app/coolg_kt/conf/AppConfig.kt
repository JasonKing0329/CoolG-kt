package com.king.app.coolg_kt.conf

import android.content.Context
import android.os.Environment
import com.king.app.coolg_kt.utils.StorageUtil
import java.io.File
import java.io.IOException

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/3/23 15:46
 */
object AppConfig {
    val DB_NAME = "gdata.db"
    val DEMO_IMAGE_VERSION = "1.1"
    val SDCARD = Environment.getExternalStorageDirectory().path
    val APP_ROOT = "$SDCARD/fileencrypt"
    val APP_DIR_IMG = "$APP_ROOT/img"
    val APP_DIR_CROP_IMG = "$APP_DIR_IMG/crop"
    val DOWNLOAD_IMAGE_DEFAULT = "$APP_DIR_IMG/download"
    val GDB_IMG = "$APP_DIR_IMG/gdb"
    val GDB_IMG_STAR = "$GDB_IMG/star"
    val GDB_IMG_RECORD = "$GDB_IMG/record"
    val GDB_IMG_DEMO = "$GDB_IMG/demo"
    val APP_DIR_IMG_SAVEAS = "$APP_ROOT/saveas"
    val APP_DIR_DB_HISTORY = "$APP_ROOT/history"
    val APP_DIR_GAME = "$APP_ROOT/game"
    val APP_DIR_EXPORT = "$APP_ROOT/export"
    val EXTEND_RES_DIR = "$APP_ROOT/res"
    val EXTEND_RES_COLOR = "$EXTEND_RES_DIR/color.xml"
    val APP_DIR_CONF = "$APP_ROOT/conf"
    val APP_DIR_CONF_PREF = "$APP_DIR_CONF/shared_prefs"
    val APP_DIR_CONF_PREF_DEF = "$APP_DIR_CONF_PREF/default"
    val APP_DIR_CONF_CRASH = "$APP_DIR_CONF/crash"
    val APP_DIR_CONF_APP = "$APP_DIR_CONF/app"

    // 采用自动更新替代gdata.db的方法，因为jornal的存在，会使重新使用这个db出现问题
    var GDB_DB_JOURNAL = "$APP_DIR_CONF/gdata.db-journal"
    var GDB_DB_FULL_PATH = "$APP_DIR_CONF/$DB_NAME"
    var PREF_NAME = "com.jing.app.jjgallery_preferences"
    var DISK_PREF_DEFAULT_PATH: String? = null
    val DIRS = arrayOf(
        APP_ROOT,
        APP_DIR_IMG,
        APP_DIR_CROP_IMG,
        DOWNLOAD_IMAGE_DEFAULT,
        GDB_IMG,
        GDB_IMG_STAR,
        GDB_IMG_RECORD,
        GDB_IMG_DEMO,
        APP_DIR_IMG_SAVEAS,
        APP_DIR_DB_HISTORY,
        APP_DIR_GAME,
        APP_DIR_EXPORT,
        EXTEND_RES_DIR,
        EXTEND_RES_COLOR,
        APP_DIR_CONF,
        APP_DIR_CONF_PREF,
        APP_DIR_CONF_PREF_DEF,
        APP_DIR_CONF_CRASH,
        APP_DIR_CONF_APP
    )

    /**
     * 遍历程序所有目录，创建.nomedia文件
     */
    fun createNoMedia() {
        val file = File(APP_ROOT)
        createNoMedia(file)
    }

    /**
     * 遍历file下所有目录，创建.nomedia文件
     * @param file
     */
    fun createNoMedia(file: File) {
        val files = file.listFiles()
        for (f in files) {
            if (f.isDirectory) {
                createNoMedia(f)
            }
        }
        val nomediaFile = File(file.path + "/.nomedia")
        if (!nomediaFile.exists()) {
            try {
                File(file.path, ".nomedia").createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getGdbVideoDir(context: Context?): String {
        return StorageUtil.getOutterStoragePath(context) + "/video"
    }
}