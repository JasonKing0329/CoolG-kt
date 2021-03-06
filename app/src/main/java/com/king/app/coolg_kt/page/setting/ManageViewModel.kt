package com.king.app.coolg_kt.page.setting

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.bean.CheckDownloadBean
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
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
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.gdb.data.entity.*
import com.king.app.gdb.data.entity.match.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 10:57
 */
class ManageViewModel(application: Application): BaseViewModel(application) {

    var dbVersionText: ObservableField<String> = ObservableField()

    private var mLocalData: LocalData? = null

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

    fun closeDatabase() {
        getDatabase().destroy()
    }

    fun saveDataFromLocal(bean: AppCheckBean) {
        loadingObserver.value = true
        saveLocalData()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<LocalData>(getComposite()) {
                override fun onNext(t: LocalData) {
                    loadingObserver.value = false
                    mLocalData = t
                    // 下载前一定要先关闭当前数据库，否则下载直接替换文件后新的数据库文件会有问题（database disk image is malformed）
                    closeDatabase()
                    readyToDownloadObserver.value = bean.appSize
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    private fun saveLocalData(): Observable<LocalData> {
        return Observable.create{

            // 将数据库备份至History文件夹
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
            FileUtil.copyFile(
                File("${AppConfig.APP_DIR_CONF}/${AppConfig.DB_NAME}"),
                File("${AppConfig.APP_DIR_DB_HISTORY}/${sdf.format(Date())}.db")
            )

            // 额外的数据表
            var data = LocalData(
                getDatabase().getFavorDao().getAllFavorRecords(),
                getDatabase().getFavorDao().getAllFavorStars(),
                getDatabase().getFavorDao().getAllFavorRecordOrders(),
                getDatabase().getFavorDao().getAllFavorStarOrders(),
                getDatabase().getStarDao().getAllStarRatings(),
                getDatabase().getPlayOrderDao().getAllPlayOrders(),
                getDatabase().getPlayOrderDao().getAllPlayItems(),
                getDatabase().getPlayOrderDao().getAllPlayDurations(),
                getDatabase().getPlayOrderDao().getVideoCoverOrders(),
                getDatabase().getPlayOrderDao().getVideoCoverStars(),
                getDatabase().getStarDao().getAllTopStarCategory(),
                getDatabase().getStarDao().getAllTopStar(),
                getDatabase().getTagDao().getAllTags(),
                getDatabase().getTagDao().getAllTagRecords(),
                getDatabase().getTagDao().getAllTagStars(),
                getDatabase().getMatchDao().getAllMatches(),
                getDatabase().getMatchDao().getAllMatchPeriods(),
                getDatabase().getMatchDao().getAllMatchItems(),
                getDatabase().getMatchDao().getAllMatchRecords(),
                getDatabase().getMatchDao().getAllMatchScoreStars(),
                getDatabase().getMatchDao().getAllMatchScoreRecords(),
                getDatabase().getMatchDao().getAllMatchRankStars(),
                getDatabase().getMatchDao().getAllMatchRankRecords()
            )
            // 保存star的favor字段
            // 保存star的favor字段
            var stars = getDatabase().getStarDao().getAllBasicStars()
            stars.forEach {star ->
                if (star.favor > 0) {
                    star.name?.let { name ->
                        data.favorMap[name] = star.favor
                    }
                }
            }
            it.onNext(data)
            it.onComplete()
        }
    }

    fun getDownloadDatabaseBean(size: Long, isUploadedDb: Boolean): DownloadDialogBean? {
        val bean = DownloadDialogBean()
        bean.isShowPreview = false
        bean.savePath = AppConfig.APP_DIR_CONF
        val item = DownloadItem()
        if (isUploadedDb) {
            item.flag = Command.TYPE_GDB_DATABASE_UPLOAD
        } else {
            item.flag = Command.TYPE_GDB_DATABASE
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
        loadingObserver.value = true
        updateLocalData(uploadedDb)
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

    /**
     * 如果是更新的upload数据库，直接替换后就完成了；下载的是默认database，保存本地的其他表单
     * @param isUploadedDb
     * @return
     */
    private fun updateLocalData(isUploadedDb: Boolean): Observable<Boolean> {
        return Observable.create {
            File(AppConfig.GDB_DB_JOURNAL).delete()
            // 重新加载数据库
            CoolApplication.instance.reCreateDatabase()
            if (!isUploadedDb) {
                updateStarFavorFiled()
                updateFavorTables()
                updateStarRelated()
                updatePlayList()
                updateTags()
                updateMatches()
                createCountData()
                // 数据插入完毕后务必先关闭数据库
                // 根据调试发现，数据库onOpen后，部分数据可能会写入到gdata.db-wal这个文件中，如果没有执行close，-wal文件会一直存在
                // 而主数据库文件gdata.db中就会缺少部分数据
                closeDatabase()
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    /**
     * star favor
     */
    private fun updateStarFavorFiled() {
        var stars = getDatabase().getStarDao().getAllBasicStars()
        stars.forEach { star ->
            var favor = mLocalData!!.favorMap[star.name]
            favor?.let {
                star.favor = favor
                getDatabase().getStarDao().updateStar(star)
            }
        }
    }

    private fun updateFavorTables() {
        getDatabase().getFavorDao().deleteFavorRecordOrders()
        getDatabase().getFavorDao().deleteFavorRecords()
        getDatabase().getFavorDao().deleteFavorStarOrders()
        getDatabase().getFavorDao().deleteFavorStars()
        getDatabase().getFavorDao().insertFavorRecordOrders(mLocalData!!.favorRecordOrderList)
        getDatabase().getFavorDao().insertFavorRecords(mLocalData!!.favorRecordList)
        getDatabase().getFavorDao().insertFavorStarOrders(mLocalData!!.favorStarOrderList)
        getDatabase().getFavorDao().insertFavorStars(mLocalData!!.favorStarList)
    }

    private fun updateStarRelated() {
        getDatabase().getStarDao().deleteStarRatings()
        getDatabase().getStarDao().deleteTopStarCategories()
        getDatabase().getStarDao().deleteTopStars()
        getDatabase().getStarDao().insertStarRatings(mLocalData!!.starRatingList)
        getDatabase().getStarDao().insertTopStarCategories(mLocalData!!.categoryList)
        getDatabase().getStarDao().insertTopStars(mLocalData!!.categoryStarList)
    }

    private fun updatePlayList() {
        getDatabase().getPlayOrderDao().deletePlayDurations()
        getDatabase().getPlayOrderDao().deletePlayItems()
        getDatabase().getPlayOrderDao().deletePlayOrders()
        getDatabase().getPlayOrderDao().deleteVideoCoverPlayOrders()
        getDatabase().getPlayOrderDao().deleteVideoCoverStars()
        getDatabase().getPlayOrderDao().insertPlayDurations(mLocalData!!.playDurationList)
        getDatabase().getPlayOrderDao().insertPlayItems(mLocalData!!.playItemList)
        getDatabase().getPlayOrderDao().insertPlayOrders(mLocalData!!.playOrderList)
        getDatabase().getPlayOrderDao().insertVideoCoverPlayOrders(mLocalData!!.videoCoverPlayOrders)
        getDatabase().getPlayOrderDao().insertVideoCoverStars(mLocalData!!.videoCoverStars)
    }

    private fun updateTags() {
        getDatabase().getTagDao().deleteTags()
        getDatabase().getTagDao().deleteTagStars()
        getDatabase().getTagDao().deleteTagRecords()
        getDatabase().getTagDao().insertTags(mLocalData!!.tagList)
        getDatabase().getTagDao().insertTagStars(mLocalData!!.tagStarList)
        getDatabase().getTagDao().insertTagRecords(mLocalData!!.tagRecordList)
    }

    private fun updateMatches() {
        getDatabase().getMatchDao().deleteMatches()
        getDatabase().getMatchDao().deleteMatchItems()
        getDatabase().getMatchDao().deleteMatchPeriods()
        getDatabase().getMatchDao().deleteMatchRecords()
        getDatabase().getMatchDao().deleteMatchRankRecords()
        getDatabase().getMatchDao().deleteMatchRankStars()
        getDatabase().getMatchDao().deleteMatchScoreRecords()
        getDatabase().getMatchDao().deleteMatchScoreStars()
        getDatabase().getMatchDao().insertMatches(mLocalData!!.matchList)
        getDatabase().getMatchDao().insertMatchPeriods(mLocalData!!.matchPeriodList)
        getDatabase().getMatchDao().insertMatchItems(mLocalData!!.matchItemList)
        getDatabase().getMatchDao().insertMatchRecords(mLocalData!!.matchRecordList)
        getDatabase().getMatchDao().insertMatchScoreStars(mLocalData!!.matchScoreStarList)
        getDatabase().getMatchDao().insertMatchScoreRecords(mLocalData!!.matchScoreRecordList)
        getDatabase().getMatchDao().insertMatchRankStars(mLocalData!!.matchRankStarList)
        getDatabase().getMatchDao().insertMatchRankRecords(mLocalData!!.matchRankRecordList)
    }

    /**
     * CountStar and CountRecord
     */
    private fun createCountData() {
        var ratings = getDatabase().getStarDao().getAllStarRatingsDesc()
        var countStars = mutableListOf<CountStar>()
        ratings.forEachIndexed { index, starRating ->
            countStars.add(CountStar(starRating.starId, index + 1))
        }
        getDatabase().getStarDao().insertCountStars(countStars)

        var records = getDatabase().getRecordDao().getAllBasicRecordsOrderByScore()
        var countRecords = mutableListOf<CountRecord>()
        records.forEachIndexed { index, record ->
            countRecords.add(CountRecord(record.id, index + 1))
        }
        getDatabase().getRecordDao().insertCountRecords(countRecords)
    }

    data class LocalData (
        var favorRecordList: List<FavorRecord>,
        var favorStarList: List<FavorStar>,
        var favorRecordOrderList: List<FavorRecordOrder>,
        var favorStarOrderList: List<FavorStarOrder>,
        var starRatingList: List<StarRating>,
        var playOrderList: List<PlayOrder>,
        var playItemList: List<PlayItem>,
        var playDurationList: List<PlayDuration>,
        var videoCoverPlayOrders: List<VideoCoverPlayOrder>,
        var videoCoverStars: List<VideoCoverStar>,
        var categoryList: List<TopStarCategory>,
        var categoryStarList: List<TopStar>,
        var tagList: List<Tag>,
        var tagRecordList: List<TagRecord>,
        var tagStarList: List<TagStar>,
        var matchList: List<Match>,
        var matchPeriodList: List<MatchPeriod>,
        var matchItemList: List<MatchItem>,
        var matchRecordList: List<MatchRecord>,
        var matchScoreStarList: List<MatchScoreStar>,
        var matchScoreRecordList: List<MatchScoreRecord>,
        var matchRankStarList: List<MatchRankStar>,
        var matchRankRecordList: List<MatchRankRecord>,
        var favorMap: MutableMap<String, Int> = mutableMapOf()
    )
}