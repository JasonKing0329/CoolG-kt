package com.king.app.coolg_kt.page.setting

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.bean.CheckDownloadBean
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.db.LocalData
import com.king.app.coolg_kt.model.db.LocalToServerEngine
import com.king.app.coolg_kt.model.db.ServerToLocalEngine
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.Command
import com.king.app.coolg_kt.model.http.HttpConstants
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.model.http.bean.request.GdbCheckNewFileBean
import com.king.app.coolg_kt.model.http.bean.request.GdbRequestMoveBean
import com.king.app.coolg_kt.model.http.bean.request.VersionRequest
import com.king.app.coolg_kt.model.http.bean.response.*
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.http.upload.UploadClient
import com.king.app.coolg_kt.model.repository.PropertyRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import io.reactivex.rxjava3.core.ObservableSource
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.util.*

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

    private var useLocalToServer = false
    var localToServerEngine = LocalToServerEngine()
    var serverToLocalEngine = ServerToLocalEngine()

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
        loadingObserver.value = true
        var request = VersionRequest()
        request.type = HttpConstants.UPLOAD_TYPE_DB
        AppHttpClient.getInstance().getAppService().getVersion(request)
            .flatMap { response -> BaseFlatMap.result(response) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<VersionResponse>(getComposite()) {
                override fun onNext(response: VersionResponse) {
                    loadingObserver.value = false
                    // 没有上传过，或服务器版本与本地保存的版本号一致，直接提示将上传覆盖
                    // 没有上传过，或服务器版本与本地保存的版本号一致，直接提示将上传覆盖
                    if (response.versionCode == null || response.versionCode!! == SettingProperty.getUploadVersion()) {
                        warningUpload.setValue("本地database将覆盖服务端database，是否继续？")
                    } else {
                        warningUpload.setValue("服务器database版本与本地上一次更新不一致，操作将执行本地database覆盖服务端database，是否继续？")
                    }
                }

                override fun onError(e: Throwable) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    fun uploadDatabase() {
        loadingObserver.value = true
        var partMap = mutableMapOf<String, RequestBody>()

        val file = File(AppConfig.GDB_DB_FULL_PATH)
        val fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        partMap["file\"; filename=\"${file.name}"] = fileBody

        UploadClient.getInstance().getService().uploadDb(partMap)
            .flatMap { response -> BaseFlatMap.result(response) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<UploadResponse>(getComposite()) {
                override fun onNext(t: UploadResponse) {
                    loadingObserver.value = false
                    messageObserver.value = "Upload success"
                    SettingProperty.setUploadVersion(t.timeStamp)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    fun checkSyncVersion(view: View) {
        // sync无视版本信息
        warningSync.value = true
    }

    fun onReceiveIp(view: View) {

    }

    fun moveStar() {
        requestServeMoveImages(Command.TYPE_STAR)
    }

    fun moveRecord() {
        requestServeMoveImages(Command.TYPE_RECORD)
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

    fun prepareUpgrade(bean: AppCheckBean) {
        if (useLocalToServer) {
            saveDataFromLocal(bean)
        }
        else {
            localToServerEngine.closeDatabase()
            readyToDownloadObserver.value = bean.appSize
        }
    }

    fun saveDataFromLocal(bean: AppCheckBean) {
        loadingObserver.value = true
        localToServerEngine.saveLocalData()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<LocalData>(getComposite()) {
                override fun onNext(t: LocalData) {
                    loadingObserver.value = false
                    // 下载前一定要先关闭当前数据库，否则下载直接替换文件后新的数据库文件会有问题（database disk image is malformed）
                    localToServerEngine.closeDatabase()
                    readyToDownloadObserver.value = bean.appSize
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    fun getDownloadDatabaseBean(size: Long, isUploadedDb: Boolean): DownloadDialogBean? {
        val bean = DownloadDialogBean()
        bean.isShowPreview = false
        val item = DownloadItem()
        if (isUploadedDb) {
            item.flag = Command.TYPE_GDB_DATABASE_UPLOAD
            bean.savePath = AppConfig.APP_DIR_CONF
        } else {
            item.flag = Command.TYPE_GDB_DATABASE
            if (useLocalToServer) {
                bean.savePath = AppConfig.APP_DIR_CONF
            }
            else {
                bean.savePath = AppConfig.APP_DIR_CONF_TEMP
            }
        }
        if (size != 0L) {
            item.size = size
        }
        item.name = AppConfig.DB_NAME
        val list: MutableList<DownloadItem> = ArrayList()
        list.add(item)
        bean.downloadList = list
        return bean
    }

    fun databaseDownloaded(uploadedDb: Boolean) {
        if (useLocalToServer) {
            insertLocalToServer(uploadedDb)
        }
        else {
            transformServerData(uploadedDb)
        }
    }

    fun transformServerData(uploadedDb: Boolean) {
        if (uploadedDb) {
            messageObserver.value = "Update successfully"
            return
        }
        loadingObserver.value = true
        serverToLocalEngine.transformData()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    loadingObserver.value = false
                    messageObserver.value = "Update successfully"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    fun insertLocalToServer(uploadedDb: Boolean) {
        loadingObserver.value = true
        localToServerEngine.updateLocalData(uploadedDb)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean?) {
                    loadingObserver.value = false
                    messageObserver.value = "Update successfully"
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

}