package com.king.app.coolg_kt.page.record

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.bean.response.OpenFileResponse
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.OrderRepository
import com.king.app.coolg_kt.model.repository.PlayRepository
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.model.socket.*
import com.king.app.coolg_kt.page.video.player.PlayListInstance
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.coolg_kt.utils.videos
import com.king.app.gdb.data.entity.FavorRecord
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.PlayOrder
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 14:38
 */
open class RecordViewModel(application: Application): BaseViewModel(application) {

    var videoUrlObserver: MutableLiveData<String> = MutableLiveData()

    private val repository = RecordRepository()
    private var playRepository = PlayRepository()
    private var orderRepository = OrderRepository()

    var recordObserver: MutableLiveData<RecordWrap> = MutableLiveData()
    var imagesObserver: MutableLiveData<List<String>> = MutableLiveData()
    var ordersObserver: MutableLiveData<List<FavorRecordOrder>> = MutableLiveData()
    var playOrdersObserver: MutableLiveData<List<VideoPlayList>> = MutableLiveData()
    var studioObserver: MutableLiveData<String> = MutableLiveData()
    var canEdit: MutableLiveData<Boolean> = MutableLiveData()

    lateinit var mRecord: RecordWrap

    private var mSingleImagePath: String? = null

    var mVideoCover: String? = null

    var mPlayUrl: String? = null

    var mUrlToSetCover: String? = null

    var bitmapObserver: MutableLiveData<Bitmap> = MutableLiveData()

    open fun loadRecord(recordId: Long) {
        DebugLog.e("recordId=$recordId")
        repository.getRecord(recordId)?.apply {
            mRecord = this
            recordObserver.value = mRecord
            launchSingle(
                block = {
                    loadImages(mRecord)
                }
            ) {
                imagesObserver.value = it
                checkPlayable()
//                testPlayUrl()
            }
        }
    }

    private fun loadImages(record: RecordWrap): List<String> {
        var list = mutableListOf<String>()
        if (ImageProvider.hasRecordFolder(record.bean.name)) {
            list.addAll(ImageProvider.getRecordPathList(record.bean.name))
            if (list.size > 1) {
                mVideoCover = list[abs(Random().nextInt()) % list.size]
            } else if (list.size == 1) {
                mSingleImagePath = list[0]
                mVideoCover = mSingleImagePath
            }
        } else {
            val path = ImageProvider.getRecordRandomPath(record.bean.name, null)
            mSingleImagePath = path
            mVideoCover = mSingleImagePath
            path?.let {
                list.add(it)
            }
        }
        return list
    }

    fun openOnServer() {
        loadingObserver.value = true
        AppHttpClient.getInstance().getAppService().openFileOnServer(getPathRequest())
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<OpenFileResponse>(getComposite()) {
                override fun onNext(response: OpenFileResponse) {
                    loadingObserver.value = false
                    if (response.isSuccess) {
                        messageObserver.setValue("打开成功")
                    } else {
                        messageObserver.setValue(response.errorMessage)
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    private fun getPathRequest(): PathRequest {
        val request = PathRequest()
        request.path = (mRecord.bean.directory)
        request.name = (mRecord.bean.name)
        // test code
//        request.setPath("E:\\temp\\coolg\\server_root\\f_3");
//        request.setName("large");
        return request
    }

    /**
     * 测试用
     */
    private fun testPlayUrl() {
        mPlayUrl = videos[5]
        videoUrlObserver.value = mPlayUrl
    }

    fun checkPlayable() {
        AppHttpClient.getInstance().getAppService().getVideoPath(getPathRequest())
            .flatMap { UrlUtil.toVideoUrl(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<String>(getComposite()) {
                override fun onNext(t: String) {
                    mPlayUrl = t
                    DebugLog.e("will play url: $mPlayUrl")
                    videoUrlObserver.value = mPlayUrl
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    fun canPlay(): Boolean {
        return mPlayUrl != null
    }

    fun addToJzvdPlayList() {
        // 将视频url添加到临时播放列表的末尾
        PlayListInstance.getInstance().addRecord(mRecord.bean, mPlayUrl)
        PlayListInstance.getInstance().setPlayIndexAsLast()
    }

    fun loadRecordOrders() {
        orderRepository.getRecordOrders(mRecord.bean.id!!)
            .flatMap{ list -> findStudio(list) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<FavorRecordOrder>>(getComposite()) {
                override fun onNext(t: List<FavorRecordOrder>) {
                    ordersObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })

    }

    fun loadRecordPlayOrders() {
        playRepository.getRecordOrders(mRecord.bean.id!!)
            .flatMap { toPlayOrders(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<VideoPlayList>>(getComposite()) {
                override fun onNext(t: List<VideoPlayList>) {
                    playOrdersObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })

    }

    private fun toPlayOrders(list: List<PlayOrder>): ObservableSource<List<VideoPlayList>> {
        return ObservableSource {
            val result: MutableList<VideoPlayList> = ArrayList()
            for (order in list) {
                val pl = VideoPlayList()
                pl.playOrder = (order)
                pl.name = (order.name)
                pl.imageUrl = (ImageProvider.parseCoverUrl(order.coverUrl))
                result.add(pl)
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    fun deleteOrderOfRecord(orderId: Long) {
        getDatabase().getFavorDao().deleteRecordFromOrder(mRecord.bean.id!!, orderId)
    }

    fun deletePlayOrderOfRecord(orderId: Long) {
        getDatabase().getPlayOrderDao().deleteRecordFromOrder(orderId)
    }

    fun loadVideoBitmap() {
        mPlayUrl?.let {
            getVideoBitmap(it)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<Bitmap>(getComposite()) {
                    override fun onNext(t: Bitmap) {
                        bitmapObserver.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }
                })
        }
    }

    private fun getVideoBitmap(url: String): Observable<Bitmap> {
        return Observable.create {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(url, HashMap())
            val bitmap = retriever.frameAtTime
            it.onNext(bitmap)
            it.onComplete()
        }
    }

    private fun findStudio(list: List<FavorRecordOrder>): ObservableSource<List<FavorRecordOrder>> {
        return ObservableSource {
            var studio = getDatabase().getFavorDao().getStudioByRecord(mRecord.bean.id!!)
            studioObserver.postValue(studio?.name?:"")
            it.onNext(list)
        }
    }

    fun addToOrder(orderId: Long) {
        orderRepository.addFavorRecord(orderId, mRecord.bean.id!!)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<FavorRecord>(getComposite()) {
                override fun onNext(t: FavorRecord) {
                    messageObserver.value = "Add successfully"
                    loadRecordOrders()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun addToStudio(orderId: Long) {
        orderRepository.addRecordToStudio(orderId, mRecord.bean)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean) {
                    messageObserver.value = "Add successfully"
                    loadRecordOrders()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun addToPlay(list: ArrayList<CharSequence>?) {
        playRepository.insertPlayItem(mRecord.bean.id!!, mPlayUrl, list)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean) {
                    messageObserver.value = "Add successfully"
                    loadRecordPlayOrders()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }
    var socketModel = SocketClientModel()

    fun playInSocketServer(inputIp: String) {
        SettingProperty.setSocketServerUrl(inputIp)
        var request = ClientRequest(
            ClientIdentity(SocketParams.IDENTITY_APP, "phone"),
            SocketParams.PLAY_VIDEO, Gson().toJson(PlayVideoRequest(mRecord!!.bean!!.name!!, mPlayUrl?:"")))
        socketModel.sendRequest(inputIp, request,
            object : SimpleObserver<SocketResponse>(getComposite()) {
                override fun onNext(t: SocketResponse) {
                    messageObserver.value = t.msg
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message?:""
                }
            })
    }

    override fun onDestroy() {
        socketModel.close()
        super.onDestroy()
    }

    fun checkEdit() {
        launchSingleThread(
            block = {
                AppHttpClient.getInstance().getAppServiceCoroutine().isServerOnline()
            },
            withLoading = true
        ) {
            if (it.isOnline) {
                canEdit.value = true
            }
        }
    }

    /**
     * 修改成功，重新加载record
     */
    fun onRecordModified() {
        loadRecord(mRecord.bean.id!!)
    }
}