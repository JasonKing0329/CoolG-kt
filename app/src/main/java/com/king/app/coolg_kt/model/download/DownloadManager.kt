package com.king.app.coolg_kt.model.download

import android.graphics.Bitmap
import android.os.Handler
import android.os.Message
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.conf.AppConfig.createNoMedia
import com.king.app.coolg_kt.model.http.Command
import com.king.app.coolg_kt.model.http.DownloadClient
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.model.http.progress.ProgressListener
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.FileUtil
import id.zelory.compressor.Compressor
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Created by Administrator on 2016/9/2.
 */
class DownloadManager(private val mCallback: DownloadCallback, private val MAX_TASK: Int) {
    private val MSG_ERROR = 0
    private val MSG_NEXT = 1
    private val MSG_COMPLETE = 2

    private inner class DownloadPack(
        var item: DownloadItem,
        var progressListener: ProgressListener
    )

    private val downloadQueue: Queue<DownloadPack>
    private val executingdList: MutableList<DownloadPack>
    private var savePath: String? = null
    fun setSavePath(path: String?) {
        savePath = path
    }

    fun downloadFile(
        item: DownloadItem,
        progressListener: ProgressListener
    ) {
        DebugLog.e(item.name)
        // 检查正在执行的任务，如果已经在执行则放弃重复执行，没有则新建下载任务
        for (pack in executingdList) {
            if (pack.item.name == item.name && pack.item.flag == item.flag) {
                if (pack.item.key != null) {
                    if (pack.item.key == item.key) {
                        DebugLog.e("return")
                        return
                    }
                } else {
                    DebugLog.e("return")
                    return
                }
            }
        }

        // 新建下载任务
        if (item.key == null) {
            DebugLog.e("new pack：" + item.name)
        } else {
            DebugLog.e("new pack：" + item.key + "/" + item.name)
        }
        val pack = DownloadPack(item, progressListener)

        // 如果正在执行的任务已经达到MAX_TASK，则进入下载队列进行排队
        if (executingdList.size >= MAX_TASK) {
            if (pack.item.key == null) {
                DebugLog.e("进入排队：" + pack.item.name)
            } else {
                DebugLog.e("进入排队：" + pack.item.key + "/" + pack.item.name)
            }
            downloadQueue.offer(pack)
            return
        }

        // 满足执行条件，开始执行新的下载任务
        startDownloadService(pack)
    }

    private fun startDownloadService(pack: DownloadPack?): Boolean {
        if (pack == null) {
            DebugLog.e("没有排队的任务了")
            // 这里只是确认没有待排队下载的任务了
            return false
        }
        // 任务可执行，加入到正在执行列表中
        if (pack.item.key == null) {
            DebugLog.e("开始执行任务：" + pack.item.name)
        } else {
            DebugLog.e("开始执行任务：" + pack.item.key + "/" + pack.item.name)
        }
        executingdList.add(pack)
        val handler = DownloadHandler(pack)
        DebugLog.e("download name：" + pack.item.name + ", key:" + pack.item.key)
        // 开始网络请求下载
        DownloadClient(pack.progressListener).downloadService.download(
            pack.item.flag,
            pack.item.name,
            pack.item.key
        )
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(object : Observer<ResponseBody> {
                override fun onComplete() {
                    if (pack.item.key == null) {
                        DebugLog.e("任务完成：" + pack.item.name)
                    } else {
                        DebugLog.e("任务完成：" + pack.item.key + "/" + pack.item.name)
                    }
                    handler.sendEmptyMessage(MSG_COMPLETE)
                }

                override fun onSubscribe(d: Disposable?) {

                }

                override fun onNext(t: ResponseBody) {
                    DebugLog.e("")
                    saveFile(pack.item, t.byteStream())
                    handler.sendEmptyMessage(MSG_NEXT)
                }

                override fun onError(e: Throwable?) {
                    DebugLog.e(e.toString())
                    e?.printStackTrace()
                    handler.sendEmptyMessage(MSG_ERROR)
                }

            })
        return true
    }

    private inner class DownloadHandler(private val pack: DownloadPack) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_ERROR -> {
                    completeDownload(pack)
                    mCallback.onDownloadError(pack.item)
                }
                MSG_NEXT -> mCallback.onDownloadFinish(pack.item)
                MSG_COMPLETE -> completeDownload(pack)
            }
            super.handleMessage(msg)
        }

    }

    private fun completeDownload(pack: DownloadPack) {
        // 完成后从正在执行列表中删除当前任务
        for (execPack in executingdList) {
            if (pack === execPack) {
                executingdList.remove(execPack)
                break
            }
        }
        // 从排队队列中选取排在最前面的任务进行执行
        if (startDownloadService(downloadQueue.peek())) {
            downloadQueue.poll()
        }

        // 所有任务执行完了才是真的全部下载完了
        if (executingdList.size == 0) {
            mCallback.onDownloadAllFinish()
        }
    }

    /**
     * 保存应用
     *
     * @param input  输入流
     */
    private fun saveFile(item: DownloadItem, input: InputStream): File {
        val file: File
        // star, record支持保存多文件
        file = if (Command.TYPE_STAR == item.flag || Command.TYPE_RECORD == item.flag) {
            getStarRecordFile(item)
        } else {
            File(savePath + "/" + item.name)
        }
        DebugLog.e("save file:" + file.path)
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var ch: Int
            while (input.read(buf).also { ch = it } != -1) {
                fileOutputStream.write(buf, 0, ch)
            }
            fileOutputStream.flush()
            fileOutputStream.close()
            input.close()

            // 只有下载的图片文件才执行压缩
            if (FileUtil.isImageFile(file.path)) {
                compressFile(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 设置实际path，用于后续加密操作
        item.path = file.path
        return file
    }

    private fun compressFile(file: File) {
        // 150K以上的才压缩，gif不压缩
        if (file.length() > 153600 && !file.name.endsWith(".gif")) {
            try {
                DebugLog.e("compress " + file.path)
                val tempFolder = File(AppConfig.APP_DIR_IMG + "_temp_")
                if (!tempFolder.exists()) {
                    tempFolder.mkdir()
                }
                val target: File = Compressor(CoolApplication.instance)
                    .setMaxWidth(1080)
                    .setMaxHeight(607)
                    .setQuality(75)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .setDestinationDirectoryPath(tempFolder.path)
                    .compressToFile(file)
                file.delete()
                target.renameTo(file)
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
    }

    /**
     * star 和 record均以key创建文件目录
     * @param item
     * @return
     */
    private fun getStarRecordFile(item: DownloadItem): File {
        val file: File
        var key = item.key
        // 位于服务端star/record的一级目录
        if (key == null) {
            key = item.name!!.substring(0, item.name!!.lastIndexOf("."))
        }
        val parent = "$savePath/$key"
        val out = savePath + "/" + item.name
        val outFile = File(out)
        val dir = File(parent)
        file = saveInFolder(item, dir, outFile)

        // create .nomedia
        createNoMedia(dir)
        return file
    }

    private fun saveInFolder(
        item: DownloadItem,
        parent: File,
        outFile: File
    ): File {
        var file: File?
        // 创建文件夹
        parent.mkdirs()

        // 移动out file
        if (outFile.exists()) {
            FileUtil.moveFile(
                outFile.path,
                parent.path + "/" + outFile.name
            )
        }

        // 待下载的文件存入到parent目录中
        file = File(parent.path + "/" + item.name)
        // 重名文件重命名
        if (File(
                parent.path + "/" + item.name!!.substring(
                    0,
                    item.name!!.lastIndexOf(".")
                ) + ".png"
            ).exists()
        ) {
            val newName =
                parent.path + "/" + System.currentTimeMillis() + "_" + item.name
            file = File(newName)
        }
        return file
    }

    init {
        downloadQueue = LinkedList()
        executingdList = ArrayList()
    }
}