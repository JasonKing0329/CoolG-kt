package com.king.app.coolg_kt.model.http.bean.request

import com.king.app.coolg_kt.model.http.bean.data.DownloadItem

/**
 * Created by Administrator on 2016/9/1.
 */
class GdbCheckNewFileBean {
    var isStarExisted = false
    var isRecordExisted = false
    var starItems: List<DownloadItem>? = null
    var recordItems: List<DownloadItem>? = null

}