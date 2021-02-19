package com.king.app.coolg_kt.page.tv.player

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.data.FileBean
import com.king.app.coolg_kt.model.http.bean.request.SubtitleRequest
import com.king.app.coolg_kt.model.http.bean.response.SubtitleResponse
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.tv.PlayTime
import java.lang.Exception

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/15 16:36
 */
class SystemPlayerViewModel(application: Application) : BaseViewModel(application) {

    var subtitles = MutableLiveData<List<FileBean>>()

    fun searchSubtitle(filePath: String) {
        val request = SubtitleRequest()
        request.filePath = filePath
        AppHttpClient.getInstance().getAppService().searchSubtitle(request)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<SubtitleResponse>(getComposite()){
                override fun onNext(t: SubtitleResponse) {
                    t.fileList?.let {
                        if (it.size > 0) {
                            subtitles.value = it
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    fun toArrays(list: List<FileBean>): Array<String> {
        val beans = mutableListOf<String>()
        list.forEach {
            val name = "${it.name}.${it.extra}"
            beans.add(name)
        }
        return beans.toTypedArray()
    }

    fun updatePlayTime(url: String, time: Int) {
        kotlin.runCatching {
            val bean = SettingProperty.getTvRemembers()
            val exist = bean.list.firstOrNull{ it.url == url }
            if (exist == null) {
                // 最多记录5个
                if (bean.list.size == 5) {
                    bean.list.removeAt(0)
                }
                bean.list.add(PlayTime(url, time))
            }
            else {
                exist.time = time
                // 已存在的调整到最后一个
                bean.list.remove(exist)
                bean.list.add(exist)
            }
            SettingProperty.setTvRemembers(bean)
        }
    }

    fun findRememberTime(url: String): Int {
        return try {
            val bean = SettingProperty.getTvRemembers()
            val exist = bean.list.firstOrNull{ it.url == url }
            exist?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }
}