package com.king.app.coolg_kt.page.setting

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.bean.CheckDownloadBean
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.Command
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.model.http.bean.request.GdbCheckNewFileBean
import com.king.app.coolg_kt.model.http.bean.request.GdbRequestMoveBean
import com.king.app.coolg_kt.model.http.bean.response.AppCheckBean
import com.king.app.coolg_kt.model.http.bean.response.GdbMoveResponse
import com.king.app.coolg_kt.model.http.bean.response.GdbRespBean
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.repository.PropertyRepository
import io.reactivex.rxjava3.core.ObservableSource
import java.io.File

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 10:57
 */
class ManageViewModel(application: Application): BaseViewModel(application) {

    var dbVersionText: ObservableField<String> = ObservableField()

    var imagesObserver: MutableLiveData<DownloadDialogBean> = MutableLiveData()
    var gdbCheckObserver: MutableLiveData<AppCheckBean> = MutableLiveData()
    var readyToDownloadObserver: MutableLiveData<Long> = MutableLiveData()

    var warningSync: MutableLiveData<Boolean> = MutableLiveData()
    var warningUpload: MutableLiveData<String> = MutableLiveData()

    init {
        dbVersionText.set("Local v" + PropertyRepository().getVersion())
    }

    fun onClickStar(view: View) {
        loadingObserver.value = true;
        AppHttpClient.getInstance().getAppService().checkNewFile(Command.TYPE_STAR)
            .flatMap { parseCheckStarBean(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<CheckDownloadBean>(getComposite()){
                override fun onNext(bean: CheckDownloadBean) {
                    loadingObserver.value = false;
                    if (bean.hasNew) {
                        var dialogBean = DownloadDialogBean();
                        dialogBean.downloadList = bean.downloadList
                        dialogBean.existedList = bean.repeatList
                        dialogBean.savePath = bean.targetPath
                        dialogBean.isShowPreview = true
                        imagesObserver.setValue(dialogBean);
                    }
                    else {
                        messageObserver.setValue("No images found");
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace();
                    loadingObserver.value = false;
                    messageObserver.value = e?.message;
                }

            })
    }

    fun onClickRecord(view: View) {
        loadingObserver.value = true;
        AppHttpClient.getInstance().getAppService().checkNewFile(Command.TYPE_RECORD)
            .flatMap { parseCheckRecordBean(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<CheckDownloadBean>(getComposite()){
                override fun onNext(bean: CheckDownloadBean) {
                    loadingObserver.value = false;
                    if (bean.hasNew) {
                        var dialogBean = DownloadDialogBean();
                        dialogBean.downloadList = bean.downloadList
                        dialogBean.existedList = bean.repeatList
                        dialogBean.savePath = bean.targetPath
                        dialogBean.isShowPreview = true
                        imagesObserver.setValue(dialogBean);
                    }
                    else {
                        messageObserver.setValue("No images found");
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace();
                    loadingObserver.value = false;
                    messageObserver.value = e?.message;
                }

            })
    }

    fun onCheckServer(view: View) {
        loadingObserver.value = true;
        AppHttpClient.getInstance().getAppService().isServerOnline()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<GdbRespBean>(getComposite()) {
                override fun onNext(bean: GdbRespBean) {
                    loadingObserver.value = false;
                    if (bean.isOnline) {
                        messageObserver.setValue("Connect success");
                    }
                    else {
                        messageObserver.setValue("Server is not online");
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace();
                    loadingObserver.value = false;
                    messageObserver.value = e?.message;
                }
            })
    }

    fun onCheckDb(view: View) {
        loadingObserver.value = true
        var version = PropertyRepository().getVersion()
        AppHttpClient.getInstance().getAppService().checkGdbDatabaseUpdate(Command.TYPE_GDB_DATABASE, version)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<AppCheckBean>(getComposite()) {
                override fun onNext(t: AppCheckBean) {
                    loadingObserver.value = false
                    if (t.isGdbDatabaseUpdate) {
                        gdbCheckObserver.setValue(t)
                    }
                    else{
                        messageObserver.value = "Database is already updated to the latest version"
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    fun prepareUpload(view: View) {

    }

    fun checkSyncVersion(view: View) {

    }

    fun onReceiveIp(view: View) {

    }

    fun moveStar() {
        requestServeMoveImages(Command.TYPE_STAR)
    }

    fun moveRecord() {
        requestServeMoveImages(Command.TYPE_RECORD)
    }

    fun uploadDatabase() {

    }

    private fun parseCheckStarBean(bean: GdbCheckNewFileBean): ObservableSource<CheckDownloadBean> {
        return ObservableSource {
            val repeatList = mutableListOf<DownloadItem>()
            val toDownloadList = pickStarToDownload(bean.starItems, repeatList)
            val cdb = CheckDownloadBean()
            cdb.hasNew = bean.isStarExisted
            cdb.downloadList = toDownloadList
            cdb.repeatList = repeatList
            cdb.targetPath = AppConfig.GDB_IMG_STAR
            it.onNext(cdb)
            it.onComplete()
        }
    }

    /**
     * 检查已有图片的star，将其过滤掉
     *
     * @param downloadList 服务端提供的下载列表
     * @param existedList  已存在的下载内容，不能为null
     * @return 未存在的下载内容
     */
    private fun pickStarToDownload(
        downloadList: MutableList<DownloadItem>,
        existedList: MutableList<DownloadItem>
    ): MutableList<DownloadItem> {
        val list: MutableList<DownloadItem> = mutableListOf()
        for (item in downloadList) {
            // name 格式为 XXX.png
            val name: String = item.name.substring(0, item.name.lastIndexOf("."))
            var path: String
            // 服务端文件处于一级目录
            if (item.key == null) {
                // 检查本地一级目录是否存在
                path = "${AppConfig.GDB_IMG_STAR}/$name.png"
                if (!File(path).exists()) {
                    // 检查本地二级目录是否存在
                    path = "${AppConfig.GDB_IMG_STAR}/$name/$name.png"
                }
            } else {
                // 只检查本地二级目录是否存在
                path = "${AppConfig.GDB_IMG_STAR}/${item.key}/$name.png"
            }

            // 检查本地一级目录是否存在
            if (File(path).exists()) {
                item.path = path
                existedList.add(item)
            } else {
                list.add(item)
            }
        }
        return list
    }

    private fun parseCheckRecordBean(bean: GdbCheckNewFileBean): ObservableSource<CheckDownloadBean> {
        return ObservableSource {
            val repeatList = mutableListOf<DownloadItem>()
            val toDownloadList = pickRecordToDownload(bean.recordItems, repeatList)
            val cdb = CheckDownloadBean()
            cdb.hasNew = bean.isRecordExisted
            cdb.downloadList = toDownloadList
            cdb.repeatList = repeatList
            cdb.targetPath = AppConfig.GDB_IMG_RECORD
            it.onNext(cdb)
            it.onComplete()
        }
    }

    /**
     * 检查已有图片的star，将其过滤掉
     *
     * @param downloadList 服务端提供的下载列表
     * @param existedList  已存在的下载内容，不能为null
     * @return 未存在的下载内容
     */
    private fun pickRecordToDownload(
        downloadList: MutableList<DownloadItem>,
        existedList: MutableList<DownloadItem>
    ): MutableList<DownloadItem> {
        val list: MutableList<DownloadItem> = mutableListOf()
        for (item in downloadList) {
            // name 格式为 XXX.png
            val name: String = item.name.substring(0, item.name.lastIndexOf("."))
            var path: String
            // 服务端文件处于一级目录
            if (item.key == null) {
                // 检查本地一级目录是否存在
                path = "${AppConfig.GDB_IMG_RECORD}/$name.png"
                if (!File(path).exists()) {
                    // 检查本地二级目录是否存在
                    path = "${AppConfig.GDB_IMG_RECORD}/$name/$name.png"
                }
            } else {
                // 只检查本地二级目录是否存在
                path = "${AppConfig.GDB_IMG_RECORD}/${item.key}/$name.png"
            }

            // 检查本地一级目录是否存在
            if (File(path).exists()) {
                item.path = path
                existedList.add(item)
            } else {
                list.add(item)
            }
        }
        return list
    }

    /**
     * 通知服务器移动下载源文件
     *
     * @param type
     */
    private fun requestServeMoveImages(type: String) {
        loadingObserver.value = true
        val bean = GdbRequestMoveBean()
        bean.type = type
        AppHttpClient.getInstance().getAppService().requestMoveImages(bean)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<GdbMoveResponse>(getComposite()) {

                override fun onNext(bean: GdbMoveResponse) {
                    loadingObserver.value = false
                    if (bean.isSuccess) {
                        messageObserver.setValue("Move success")
                    } else {
                        messageObserver.setValue("Move failed")
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    fun saveDataFromLocal(bean: AppCheckBean) {

    }

}