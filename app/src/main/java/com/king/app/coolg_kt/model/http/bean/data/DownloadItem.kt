package com.king.app.coolg_kt.model.http.bean.data

/**
 * Created by Administrator on 2016/9/2.
 */
class DownloadItem {
    /**
     * 用于服务端识别下载内容的关键信息，可以是url
     * 在star与record下载中，key充当parent目录（没有parent则为null）
     * gdb和app update中，key为null
     */
    var key: String? = null

    /**
     * 下载文件的文件名
     */
    var name: String = ""

    /**
     * 下载文件的文件flag
     */
    var flag: String = ""

    /**
     * 下载文件的总大小
     */
    var size: Long = 0

    /**
     * 下载后的完整目录(客户端生成)
     */
    var path: String = ""

}