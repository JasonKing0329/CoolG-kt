package com.king.app.coolg_kt.model.bean

import com.king.app.coolg_kt.model.http.bean.data.DownloadItem

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 12:16
 */
data class HsvColorBean (
    var hStart:Int = -1,
    var hArg:Int = -1,
    var s:Float = -1f,
    var v:Float = -1f,

    /**
     * 0: 随机
     * 1: 配合白色文字的背景颜色
     * 2: 配合深色文字的背景颜色
     */
    var type:Int = 0
)
class DownloadDialogBean {
    /**
     * 本地不存在的待下载内容
     */
    var downloadList: MutableList<DownloadItem>? = null

    /**
     * 本地已存在的待下载任务
     */
    var existedList: List<DownloadItem>? = null

    /**
     * 下载目录
     */
    var savePath: String = ""

    /**
     * 直接下载，不提示
     */
    var isShowPreview = false

}
data class DownloadItemProxy (
    var item: DownloadItem,
    var progress:Int = 0
)

class CheckDownloadBean {
    var hasNew = false
    var downloadList = mutableListOf<DownloadItem>()
    var repeatList = mutableListOf<DownloadItem>()
    var targetPath: String = ""
}