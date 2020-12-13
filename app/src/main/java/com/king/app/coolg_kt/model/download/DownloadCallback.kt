package com.king.app.coolg_kt.model.download

import com.king.app.coolg_kt.model.http.bean.data.DownloadItem

/**
 * Created by Administrator on 2016/9/2.
 */
interface DownloadCallback {
    fun onDownloadFinish(item: DownloadItem)
    fun onDownloadError(item: DownloadItem)
    fun onDownloadAllFinish()
}