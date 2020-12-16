package com.king.app.coolg_kt.model.image

import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.setting.SettingProperty
import java.io.File
import java.io.FileFilter
import java.util.*

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2017/7/21 10:51
 */
object ImageProvider {
    /**
     *
     * @param name
     * @param indexPackage save the real index, can be null
     * @return
     */
    fun getRecordRandomPath(name: String?, indexPackage: IndexPackage?): String? {
        return getImagePath(AppConfig.GDB_IMG_RECORD, name, -1, indexPackage)
    }

    fun getRecordPath(name: String, index: Int): String? {
        return getImagePath(AppConfig.GDB_IMG_RECORD, name, index, null)
    }

    fun getRecordPathList(name: String?): List<String> {
        return getImagePathList(AppConfig.GDB_IMG_RECORD, name)
    }

    fun hasRecordFolder(name: String?): Boolean {
        return hasFolder(AppConfig.GDB_IMG_RECORD, name)
    }

    /**
     *
     * @param name
     * @param indexPackage save the real index, can be null
     * @return
     */
    fun getStarRandomPath(name: String?, indexPackage: IndexPackage?): String? {
        return getImagePath(AppConfig.GDB_IMG_STAR, name, -1, indexPackage)
    }

    fun getStarPath(name: String, index: Int): String? {
        return getImagePath(AppConfig.GDB_IMG_STAR, name, index, null)
    }

    fun getStarPathList(name: String): List<String?> {
        return getImagePathList(AppConfig.GDB_IMG_STAR, name)
    }

    fun hasStarFolder(name: String): Boolean {
        return hasFolder(AppConfig.GDB_IMG_STAR, name)
    }

    private fun hasFolder(parent: String, name: String?): Boolean {
        val file = File("$parent/$name")
        return file.exists() && file.isDirectory
    }

    fun getRecordPicNumber(name: String?): Int {
        return getPicNumber(AppConfig.GDB_IMG_RECORD, name)
    }

    fun getStarPicNumber(name: String): Int {
        return getPicNumber(AppConfig.GDB_IMG_STAR, name)
    }

    private fun getPicNumber(parent: String, name: String?): Int {
        if (name == null) {
            return 0
        }
        var count = 0
        var path: String
        if (hasFolder(parent, name)) {
            val file = File("$parent/$name")
            count = countImageFiles(file)
        }
        if (count == 0) {
            path = "$parent/$name"
            if (!name.endsWith(".png")) {
                path = "$path.png"
            }
            if (File(path).exists()) {
                count++
            }
        }
        return count
    }

    /**
     *
     * @param parent
     * @param name
     * @param index if random, then -1
     * @param indexPackage save the true index, can be null
     * @return
     */
    private fun getImagePath(
        parent: String,
        name: String?,
        index: Int,
        indexPackage: IndexPackage?
    ): String? {
        if (name == null) {
            return ""
        }
        if (SettingProperty.isNoImageMode()) {
            return ""
        }
        if (SettingProperty.isDemoImageMode()) {
            return getRandomDemoImage(index, indexPackage)
        }
        var path: String
        if (hasFolder(parent, name)) {
            val file = File("$parent/$name")
            val fileList: MutableList<File> =
                ArrayList()
            getImageFiles(file, fileList)
            if (fileList.size == 0) {
                path = "$parent/$name"
                if (!name.endsWith(".png")) {
                    path = "$path.png"
                }
            } else {
                return if (index == -1 || index >= fileList.size) {
                    val pos = Math.abs(Random().nextInt()) % fileList.size
                    if (indexPackage != null) {
                        indexPackage.index = pos
                    }
                    fileList[pos].path
                } else {
                    fileList[index].path
                }
            }
        } else {
            path = "$parent/$name"
            if (!name.endsWith(".png")) {
                path = "$path.png"
            }
        }
        return path
    }

    fun getRandomDemoImage(index: Int, indexPackage: IndexPackage?): String? {
        var path: String? = null
        val fileList =
            File(AppConfig.GDB_IMG_DEMO).listFiles(fileFilter)
        if (index >= 0 && index < fileList.size) {
            path = fileList[index].path
        } else {
            if (fileList.size > 0) {
                val pos = Math.abs(Random().nextInt()) % fileList.size
                if (indexPackage != null) {
                    indexPackage.index = pos
                }
                path = fileList[pos].path
            }
        }
        return path
    }

    private val demoImages: List<String>
        private get() {
            val list: MutableList<String> =
                ArrayList()
            val fileList =
                File(AppConfig.GDB_IMG_DEMO).listFiles(fileFilter)
            for (file in fileList) {
                list.add(file.path)
            }
            return list
        }

    /**
     * @param file
     */
    private fun countImageFiles(file: File): Int {
        var result = 0
        if (file.isDirectory) {
            val files = file.listFiles(fileFilter)
            for (f in files) {
                result += countImageFiles(f)
            }
            return result
        } else {
            result = 1
        }
        return result
    }

    /**
     * v2.0.2 it supported multi-level directories since v2.0.1
     * @param file
     * @param list
     */
    private fun getImageFiles(
        file: File,
        list: MutableList<File>?
    ) {
        if (file.isDirectory) {
            val files = file.listFiles(fileFilter)
            for (f in files) {
                getImageFiles(f, list)
            }
        } else {
            list!!.add(file)
        }
    }

    private val fileFilter = FileFilter { file -> !file.name.endsWith(".nomedia") }

    private fun getImagePathList(
        parent: String,
        name: String?
    ): List<String> {
        if (SettingProperty.isDemoImageMode()) {
            return demoImages
        }
        val list: MutableList<String> = ArrayList()
        val file = File("$parent/$name")
        val fileList: MutableList<File> =
            ArrayList()
        getImageFiles(file, fileList)
        if (fileList != null) {
            for (f in fileList) {
                if (SettingProperty.isNoImageMode()) {
                    list.add("")
                } else {
                    list.add(f.path)
                }
            }
            list.shuffle()
        }
        return list
    }

    /**
     * 控制无图模式
     * @param path
     * @return
     */
    fun parseFilePath(path: String): String {
        return if (SettingProperty.isNoImageMode()) {
            ""
        } else {
            path
        }
    }

    fun getRecordCuPath(name: String): String? {
        if (SettingProperty.isNoImageMode()) {
            return ""
        }
        if (SettingProperty.isDemoImageMode()) {
            return getRandomDemoImage(-1, null)
        }
        val path = AppConfig.GDB_IMG_RECORD + "/" + name + "/cu"
        val folder = File(path)
        if (folder.exists()) {
            val files = folder.listFiles(fileFilter)
            if (files.size > 0) {
                return files[0].path
            }
        }
        return null
    }

    fun parseCoverUrl(coverUrl: String?): String? {
        if (SettingProperty.isNoImageMode()) {
            return ""
        }
        return if (SettingProperty.isDemoImageMode()) {
            getRandomDemoImage(-1, null)
        } else coverUrl
    }

    class IndexPackage {
        var index = 0
    }
}