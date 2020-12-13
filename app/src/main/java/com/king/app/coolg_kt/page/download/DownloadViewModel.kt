package com.king.app.coolg_kt.page.download

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.bean.DownloadItemProxy
import com.king.app.coolg_kt.model.download.DownloadCallback
import com.king.app.coolg_kt.model.download.DownloadManager
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.model.http.progress.ProgressListener
import com.king.app.coolg_kt.utils.DebugLog
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/7 13:40
 */
class DownloadViewModel(application: Application) : BaseViewModel(application) {

    private val downloadCallback: DownloadCallback = object : DownloadCallback {
        override fun onDownloadFinish(item: DownloadItem) {
            onDownloadListener?.onDownloadFinish(item)
        }

        override fun onDownloadError(item: DownloadItem) {
            DebugLog.e(item.name)
            messageObserver.value = "Error: ${item.name}"
        }

        override fun onDownloadAllFinish() {
            onDownloadListener?.onDownloadFinish()
        }
    }

    private val downloadManager = DownloadManager(downloadCallback, 5)

    private var itemList = mutableListOf<DownloadItemProxy>()

    var itemsObserver = MutableLiveData<List<DownloadItemProxy>>()
    var progressObserver = MutableLiveData<Int>()
    var showListPage = MutableLiveData<Boolean>()
    var showPreviewPage = MutableLiveData<Boolean>()
    var dismissDialog = MutableLiveData<Boolean>()

    var onDownloadListener: OnDownloadListener? = null

    var downloadDialogBean: DownloadDialogBean? = null

    init {
        downloadManager
    }

    fun setSavePath(path: String) {
        downloadManager.setSavePath(path)
    }

    fun initDownloadItems() {
        itemList = ArrayList()
        getDownloadList().forEach {
            itemList.add(DownloadItemProxy(it, 0))
        }
        itemsObserver.value = itemList
    }

    fun startDownload() {
        itemList.forEachIndexed { index, downloadItemProxy ->
            downloadManager.downloadFile(downloadItemProxy.item, object : ProgressListener {
                private var lastProgress = 0
                override fun update(
                    bytesRead: Long,
                    contentLength: Long,
                    done: Boolean
                ) {
                    val progress = (100 * 1f * bytesRead / contentLength).toInt()
                    //                    DebugLog.e("progress:" + progress);
                    if (progress - lastProgress > 8 || done) { // 避免更新太过频繁
                        lastProgress = progress
                        val bundle = Bundle()
                        bundle.putInt("index", index)
                        bundle.putInt("progress", progress)
                        val message = Message()
                        message.data = bundle
                        uiHandler.sendMessage(message)
                    }
                }
            })
        }
    }

    private val uiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data
            val index = bundle.getInt("index")
            val progress = bundle.getInt("progress")
            itemList[index].progress = progress
            progressObserver.value = index
        }
    }

    fun getExistedList(): List<DownloadItem> {
        downloadDialogBean?.existedList?.let { return it }
        return listOf()
    }

    fun getDownloadList(): List<DownloadItem> {
        downloadDialogBean?.downloadList?.let { return it }
        return listOf()
    }

    fun addDownloadItems(list: List<DownloadItem>) {
        if (list.isNotEmpty()) {
            downloadDialogBean?.downloadList?.addAll(list)
        }
    }

    fun showListPage() {
        showListPage.value = true
    }

    fun dismiss() {
        dismissDialog.value = true
    }
}