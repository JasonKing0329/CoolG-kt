package com.king.app.coolg_kt.page.tv

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.response.BgResponse
import com.king.app.coolg_kt.model.http.download.DownLoadFile
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.coolg_kt.utils.UrlUtil
import io.reactivex.rxjava3.core.ObservableSource
import java.io.File
import java.io.FileFilter

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
        loadingObserver.value = true
        AppHttpClient.getInstance().getAppService().getBgFiles()
            .flatMap { toUrls(it.imageList) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<String>>(getComposite()){
                override fun onNext(t: List<String>) {
                    loadingObserver.value = false
                    bgObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    loadingObserver.value = false
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toUrls(list: List<String>): ObservableSource<List<String>> {
        return ObservableSource {
            val result = mutableListOf<String>()
            list.forEach { url ->
                result.add(UrlUtil.toVideoUrl(url))
            }
            it.onNext(result)
            it.onComplete()
        }
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
}