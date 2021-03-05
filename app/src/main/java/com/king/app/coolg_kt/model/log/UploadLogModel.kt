package com.king.app.coolg_kt.model.log

import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.http.bean.response.BaseFlatMap
import com.king.app.coolg_kt.model.http.bean.response.UploadResponse
import com.king.app.coolg_kt.model.http.upload.UploadClient
import com.king.app.coolg_kt.model.setting.BaseProperty
import com.king.app.coolg_kt.utils.ZipUtils
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/3/5 17:30
 */
class UploadLogModel {

    fun uploadLog(): Observable<UploadResponse> {
        return zipLogFile()
            .flatMap { file ->
                var partMap = mutableMapOf<String, RequestBody>()
                val fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                partMap["file\"; filename=\"${file.name}"] = fileBody
                UploadClient.getInstance().getService().uploadDb(partMap)
            }
            .flatMap { response -> BaseFlatMap.result(response) }
    }

    private fun zipLogFile(): Observable<File> {
        return Observable.create {
            var files = mutableListOf<File>()
            // preference
            var pref = BaseProperty.getPrefPath()
            var prefFile = File(pref)
            if (prefFile.exists()) {
                files.add(prefFile)
            }
            // logs
            var f = File(AppConfig.APP_DIR_LOG)
            val fs = f.listFiles { pathname: File ->
                pathname.name.endsWith(".trace")
            }.toList()
            if (fs.isNotEmpty()) {
                files.addAll(fs)
            }
            // to zip
            var zip = File(f, "LOGS.zip")
            if (zip.exists()) {
                zip.delete()
            }
            ZipUtils.zipFiles(files, zip)
            it.onNext(zip)
            it.onComplete()
        }
    }
}