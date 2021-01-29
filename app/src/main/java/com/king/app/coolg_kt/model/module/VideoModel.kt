package com.king.app.coolg_kt.model.module

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.provider.MediaStore.Video
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.conf.AppConfig.getGdbVideoDir
import com.king.app.coolg_kt.model.bean.VideoData
import com.king.app.coolg_kt.model.image.CropHelper
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.coolg_kt.utils.FormatUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 描述: loadVideos in GHomeActivity, clear int GHomeActivity.onDestroy
 *
 * 作者：景阳
 *
 * 创建时间: 2017/7/18 13:53
 */
class VideoModel {
    var columns = arrayOf(
        Video.Media.DATA,
        Video.Media._ID,
        Video.Media.TITLE,
        Video.Media.DISPLAY_NAME,
        Video.Media.SIZE,
        Video.Media.DURATION,
        Video.Media.DATE_ADDED,
        Video.Media.MIME_TYPE,
        Video.Media.WIDTH,
        Video.Media.HEIGHT
    )

    /**
     * create thumb nail mode image, to save the memory
     * @param src
     * @param width
     * @param height
     * @param scale if true, 按width*height的比例压缩, false 则压缩为width与height指定的大小
     * @return
     */
    private fun convertToThumbnail(
        src: Bitmap?,
        width: Int,
        height: Int,
        scale: Boolean
    ): Bitmap? {
        var bitmap: Bitmap? = null
        val matrix = Matrix()
        // 按比例缩放，宽高比不变
        if (scale) {
            val totalRate =
                width.toFloat() * height / (src!!.width.toFloat() * src.height)
            matrix.postScale(
                Math.sqrt(totalRate.toDouble()).toFloat(),
                Math.sqrt(totalRate.toDouble()).toFloat()
            )
        } else {
            val dx = width.toFloat() / src!!.width.toFloat()
            val dy = height.toFloat() / src.height.toFloat()
            matrix.postScale(dx, dy)
        }
        bitmap = try {
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        } catch (e: Exception) {
            null
        }
        src.recycle()
        return bitmap
    }

    /**
     * load image in videos
     * @param filePath
     * @param minus 毫秒数
     * @param width
     * @param height
     * @param scale if true, 按width*height的比例压缩, false 则压缩为width与height指定的大小
     * @return
     */
    fun getVideoFrame(filePath: String?, minus: Int, width: Int, height: Int, scale: Boolean): Bitmap {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        // 第一个参数是毫秒数再乘以1000
        var bitmap =
            retriever.getFrameAtTime(minus * 1000.toLong(), MediaMetadataRetriever.OPTION_CLOSEST)
        DebugLog.e("bitmap before convert[" + bitmap!!.width + "," + bitmap.height + "]")
        if (bitmap.width * bitmap.height <= width * height) {
            return bitmap
        }
        bitmap = convertToThumbnail(bitmap, width, height, scale)
        DebugLog.e("bitmap after convert[" + bitmap!!.width + "," + bitmap.height + "]")
        retriever.release()
        return bitmap
    }

    fun queryVideoDataByPath(context: Context, path: String): VideoData? {
        var data: VideoData? = null
        val cursor = context.contentResolver.query(
            Video.Media.EXTERNAL_CONTENT_URI,
            columns, Video.Media.DATA + " = ?", arrayOf(path), null
        )
        cursor?.let {
            if (it.moveToNext()) {
                data = getVideoDataFromCursor(it)
            }
            it.close()
        }
        return data
    }

    private fun getVideoDataFromCursor(cursor: Cursor): VideoData {
        val data = VideoData()
        data.id = (cursor.getString(cursor.getColumnIndexOrThrow(Video.Media._ID)))
        data.name = (cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME)))
        data.path = (cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA)))
        data.sizeLong = (cursor.getLong(cursor.getColumnIndexOrThrow(Video.Media.SIZE)))
        data.size = (FormatUtil.formatSize(data.sizeLong))
        data.durationInt = (cursor.getInt(cursor.getColumnIndexOrThrow(Video.Media.DURATION)))
        data.duration = (FormatUtil.formatTime(data.durationInt.toLong()))
        try {
            data.dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATE_ADDED)).toLong()
        } catch (e: Exception) {
            data.dateAdded = 0
        }
        try {
            data.width = cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.WIDTH)).toInt()
        } catch (e: Exception) {
            data.width = 0
        }
        try {
            data.height = cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.HEIGHT)).toInt()
        } catch (e: Exception) {
            data.height = 0
        }
        data.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.MIME_TYPE))
        return data
    }

    fun createVideoFrames(videoData: VideoData, total: Int): List<Int> {
        val timeList: MutableList<Int> = ArrayList()
        val duration: Int = videoData.durationInt
        val step = duration / total
        for (i in 0 until total) {
            timeList.add(i * step)
        }
        val random = Random()
        for (i in 0 until total) {
            val start = timeList[i]
            var end = duration
            if (i < total - 1) {
                end = timeList[i + 1] - 1000
            }
            val time = start + Math.abs(random.nextInt()) % (end - start)
            timeList[i] = time
        }
        return timeList
    }

    private fun thumbnail(videoData: VideoData, timeList: List<Int>, updateNum: Int
                          , width: Int, height: Int, scale: Boolean, callback: VideoThumbCallback): Observable<List<Bitmap>> {
        return Observable.create {
            var list = mutableListOf<Bitmap>()
            for (i in timeList.indices) {
                if (i % updateNum == 0) {
                    list = ArrayList()
                }
                list.add(getVideoFrame(videoData.path, timeList[i], width, height, scale))
                if (i % updateNum == updateNum - 1) {
                    it.onNext(list)
                }
            }
            it.onComplete()
        }
    }

    /**
     *
     * @param videoData
     * @param timeList 创建缩略图的时间帧
     * @param updateNum 每加载几张通知一次更新
     * @param width 缩略图width
     * @param height 缩略图height
     * @param callback
     */
    fun createThumbnails(
        videoData: VideoData, timeList: List<Int>, updateNum: Int
        , width: Int, height: Int, scale: Boolean, callback: VideoThumbCallback
    ) {
        thumbnail(videoData, timeList, updateNum, width, height, scale, callback)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                Consumer { t -> callback.onThumbnailCreated(t) },
                Consumer { t -> t?.printStackTrace() }
            )
    }

    fun saveBitmap(bitmap: Bitmap?, name: String) {
        val folder = AppConfig.GDB_IMG_RECORD + "/" + name
        val file = File(folder)
        if (!file.exists()) {
            file.mkdir()
            // 移动外部文件
            val outFile = File("$folder.png")
            FileUtil.moveFile(
                outFile.path,
                folder + "/" + outFile.name
            )
        }
        val path: String = CropHelper.saveBitmap(
            bitmap,
            folder + "/" + System.currentTimeMillis() + ".png"
        )
        DebugLog.e("save image:$path")
    }

    companion object {
        private var map: MutableMap<String, String>? = null
        fun loadVideos(context: Context?) {
            map = HashMap()
            val file = File(getGdbVideoDir(context))
            if (file.exists() && file.isDirectory) {
                val files = file.listFiles()
                for (f in files) {
                    try {
                        val name =
                            f.name.substring(0, f.name.lastIndexOf('.'))
                        map!![name] = f.path
                    } catch (e: Exception) {
                    }
                }
            }
        }

        fun clear() {
            map = null
        }

        fun getVideoPath(name: String?): String? {
            map?.let { map ->
                name?.let { name ->
                    return map[name]
                }
            }
            return null
        }
    }
}