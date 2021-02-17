package com.king.app.coolg_kt.model.http.download

import com.king.app.coolg_kt.utils.DebugLog
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import java.io.*

/**
 * 写入磁盘
 *
 * 作者： lxg
 *
 * 创建时间: 17/08/29 10:59.
 */
object WriteDisk {
    fun saveFile(
        body: ResponseBody,
        filePatch: String
    ): Observable<Boolean> {
        return Observable.create { e -> e.onNext(writeResponseBodyToDisk(body, filePatch)) }
    }

    /**
     * @param body
     * @param filePatch 文件存放全路径
     * @return
     */
    @JvmStatic
    fun writeResponseBodyToDisk(body: ResponseBody, filePatch: String): Boolean {
        DebugLog.e("start $filePatch")
        val file = File(filePatch)
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val contentLength = body.contentLength()
        return try {
            val fileReader = ByteArray(4096)
            inputStream = body.byteStream()
            outputStream = FileOutputStream(file)
            while (true) {
                val read = inputStream.read(fileReader)
                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
            }
            outputStream.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            val lastTotal = file.length()
            DebugLog.e("$filePatch contentLength=$contentLength, lastTotal=$lastTotal")
            if (lastTotal != contentLength) {
                DebugLog.e("delete $filePatch")
                file.delete()
            }
            inputStream?.close()
            outputStream?.close()
        }
    }
}