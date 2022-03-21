package com.king.app.coolg_kt.page.tv

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.Command
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.model.http.bean.response.AppCheckBean
import com.king.app.coolg_kt.model.http.bean.response.UploadResponse
import com.king.app.coolg_kt.model.http.download.DownLoadFile
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.log.UploadLogModel
import com.king.app.coolg_kt.utils.AppUtil
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.coolg_kt.utils.UrlUtil
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.*

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 22:50
 */
class TvViewModel(application: Application): BaseViewModel(application) {

    var isSuperUser = false

    var goToServer = MutableLiveData<Boolean>()

    var bgFilePath = MutableLiveData<String>()

    var bgObserver = MutableLiveData<List<String>>()

    var newVersionFound = MutableLiveData<String>()

    var appCheckBean: AppCheckBean? = null

    fun checkUserCode(code: String) {
        isSuperUser = code == "1010520"
    }

    fun localBg() {
        val folder = File(AppConfig.APP_DIR_TV_BG)
        val files = folder.listFiles { pathname -> FileUtil.isImageFile(pathname.name) }
        if (files.isNotEmpty()) {
            bgFilePath.value = files[0].path
        }
    }

    fun getBg() {
        launchFlowThread(
            flow { emit(AppHttpClient.getInstance().getAppServiceCoroutine().getBgFiles()) }
                .map { toUrls(it.imageList) },
            withLoading = true
        ) {
            bgObserver.value = it
        }
    }

    private fun toUrls(list: List<String>): List<String> {
        val result = mutableListOf<String>()
        list.mapTo(result) { UrlUtil.toVideoUrl(it) }
        return result
    }

    fun downloadBg(url: String) {
        // 删除当前文件
        val folder = File(AppConfig.APP_DIR_TV_BG)
        folder.listFiles().forEach {
            it.delete()
        }

        // 下载新文件
        var index = url.lastIndexOf("/")
        var name = url.substring(index + 1)
        val target = "${AppConfig.APP_DIR_TV_BG}/$name"
        DownLoadFile().downLoad(target, url)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    messageObserver.value = "success"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message?:"null"
                }
            })
    }

    /**
     * {"isAppUpdate":true,"appVersion":"8.1","appName":"JJGallery-4.7.2-release-20170812224854.apk","appSize":10618166,"isGdbDatabaseUpdate":false,"gdbDabaseSize":0}
     */
    fun checkAppUpdate() {
        launchSingleThread(
            { AppHttpClient.getInstance().getAppServiceCoroutine().checkAppUpdate(Command.TYPE_APP, AppUtil.getAppVersionName()) },
            withLoading = true
        ) {
            if (it.isAppUpdate) {
                appCheckBean = it
                newVersionFound.value = "发现新版本${it.appVersion}，是否更新？"
            }
        }
    }

    fun getDownloadRequest(): DownloadDialogBean {
        val bean = DownloadDialogBean()
        bean.isShowPreview = false
        bean.savePath = AppConfig.APP_DIR_CONF_APP
        val item = DownloadItem()
        item.flag = Command.TYPE_APP
        if (appCheckBean!!.appSize != 0L) {
            item.size = appCheckBean!!.appSize
        }
        item.name = appCheckBean!!.appName!!
        val list: MutableList<DownloadItem> = ArrayList()
        list.add(item)
        bean.downloadList = list
        return bean
    }

    fun uploadLog() {
        loadingObserver.value = true
        UploadLogModel().uploadLog()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<UploadResponse>(getComposite()) {
                override fun onNext(t: UploadResponse?) {
                    loadingObserver.value = false
                    messageObserver.value = "上传成功"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message?:""
                }
            })
    }

}