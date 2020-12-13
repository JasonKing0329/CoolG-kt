package com.king.app.coolg_kt.page.download

import com.king.app.coolg_kt.model.http.bean.data.DownloadItem

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/7 13:54
 */
interface OnDownloadListener {
    fun onDownloadFinish(item: DownloadItem)
    fun onDownloadFinish()
}