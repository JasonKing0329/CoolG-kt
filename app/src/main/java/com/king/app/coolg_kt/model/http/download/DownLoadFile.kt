package com.king.app.coolg_kt.model.http.download

import com.king.app.coolg_kt.model.http.DownloadService
import com.king.app.coolg_kt.model.http.download.WriteDisk.writeResponseBodyToDisk
import io.reactivex.rxjava3.core.Observable

/**
 * 文件下载类
 *
 * 作者： lxg
 *
 * 创建时间: 17/08/29 11:09.
 */
class DownLoadFile {

    var service: DownloadService = ImageClient.getInstance().getDownloadService()

    /**
     * 下载文件并存储为指定文件，指定目录必须存在
     * @param path 文件存位置，包含文件名
     * @param url  图片下载路径
     * @return
     */
    fun downLoad(path: String, url: String): Observable<Boolean> {
        return if (url == null) {
            Observable.create { it.onNext(false) }
        } else service.download(url)
            .map { body ->
                writeResponseBodyToDisk(body, path)
            }
    }
}