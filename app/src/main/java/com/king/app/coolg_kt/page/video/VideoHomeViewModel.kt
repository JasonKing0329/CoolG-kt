package com.king.app.coolg_kt.page.video

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.bean.VideoGuy
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.PlayRepository
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.page.video.player.PlayListInstance
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.coolg_kt.view.widget.video.UrlCallback
import com.king.app.gdb.data.entity.*
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.functions.Function
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/22 15:25
 */
class VideoHomeViewModel(application: Application) : BaseViewModel(application) {
    var recommendObserver: MutableLiveData<MutableList<PlayItemViewBean>> = MutableLiveData()
    var recentVideosObserver: MutableLiveData<MutableList<Any>> = MutableLiveData()
    var getPlayUrlFailed: MutableLiveData<Boolean> = MutableLiveData()
    var videoPlayOnReadyObserver: MutableLiveData<Boolean> = MutableLiveData()
    var playRepository = PlayRepository()
    private val recordRepository = RecordRepository()
    private val LOAD_NUM = 20
    private var mOffset = 0
    private var mItemToAddOrder: PlayItemViewBean? = null
    fun buildPage() {
        loadHeadData()
        loadRecommend()
        loadRecentVideos()
    }

    fun loadRecommend() {
        val bean = SettingProperty.getVideoRecBean()
        bean.isOnline = true
        bean.number = 5
        recordRepository.getRecordsBy(bean)
            .flatMap { toPlayItems(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<PlayItemViewBean>>(getComposite()) {

                override fun onNext(list: MutableList<PlayItemViewBean>) {
                    recommendObserver.value = list
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toPlayItems(records: List<RecordWrap>): ObservableSource<MutableList<PlayItemViewBean>> {
        return ObservableSource {
            val list = mutableListOf<PlayItemViewBean>()
            records.forEach { record ->
                val bean = PlayItemViewBean()
                bean.record = record
                bean.cover = ImageProvider.getRecordRandomPath(record.bean.name, null)
                bean.name = parseVideoName(record)
                list.add(bean)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun parseVideoName(record: RecordWrap): String {
        val starBuffer = StringBuffer()
        record.starList.forEach {
            starBuffer.append("&").append(it.name)
        }
        var starText = starBuffer.toString()
        if (starText.length > 1) {
            starText = starText.substring(1)
        }
        if (TextUtils.isEmpty(starText)) {
            starText = record.bean.name?:""
        }
        return starText
    }

    fun loadHeadData() {
        headData
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<VideoHeadData>(getComposite()) {

                override fun onNext(videoHeadData: VideoHeadData) {
                    headDataObserver.value = videoHeadData
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private val headData: Observable<VideoHeadData>
        private get() = Observable.create {
            val data = VideoHeadData()
            data.setGuyList(coverGuys)
            data.setPlayLists(coverPlayLists)
            getPadCovers(data)
            it.onNext(data)
            it.onComplete()
        }

    /**
     * 子类实现
     * @param data
     */
    fun getPadCovers(data: VideoHeadData?) {}

    // 随机加载最多4个
    private val coverGuys: MutableList<VideoGuy>
        private get() {
            val guys = mutableListOf<VideoGuy>()
            // 随机加载最多4个
            val list = getDatabase().getPlayOrderDao().getRandomStarOrders(4)
            for (cs in list) {
                val guy = VideoGuy()
                guy.star = cs.star
                guy.imageUrl = ImageProvider.getStarRandomPath(cs.star?.name, null)
                guys.add(guy)
            }
            return guys
        }

    fun updateVideoCoverPlayList(list: ArrayList<CharSequence>) {
        updateCoverPlayList(list)
            .flatMap { loadCoverPlayLists() }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<VideoPlayList>>(getComposite()) {
                override fun onNext(lists: List<VideoPlayList>) {
                    headDataObserver.value?.setPlayLists(lists)
                    headDataObserver.setValue(headDataObserver.value)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.setValue(e?.message)
                }
            })
    }

    private fun updateCoverPlayList(list: ArrayList<CharSequence>): Observable<Boolean> {
        return Observable.create {
            getDatabase().getPlayOrderDao().deleteVideoCoverPlayOrders()
            val insertList = mutableListOf<VideoCoverPlayOrder>()
            for (str in list) {
                val orderId = str.toString().toLong()
                val order = VideoCoverPlayOrder(null, orderId)
                order.orderId = orderId
                insertList.add(order)
            }
            getDatabase().getPlayOrderDao().insertVideoCoverPlayOrders(insertList)
            it.onNext(true)
            it.onComplete()
        }
    }

    private fun loadCoverPlayLists(): ObservableSource<List<VideoPlayList>> {
        return ObservableSource {
            it.onNext(coverPlayLists)
            it.onComplete()
        }
    }

    private val coverPlayLists: MutableList<VideoPlayList>
        private get() {
            val lists = mutableListOf<VideoPlayList>()
            val list = getDatabase().getPlayOrderDao().getVideoCoverOrderWraps()
            for (cs in list) {
                val playList = VideoPlayList()
                playList.name = cs.playOrder?.name
                playList.imageUrl = ImageProvider.parseCoverUrl(cs.playOrder?.coverUrl)
                playList.playOrder = cs.playOrder
                lists.add(playList)
            }
            return lists
        }

    fun getRecentPlayUrl(position: Int, callback: UrlCallback) {
        getPlayUrl(position, callback, recentVideosObserver)
    }

    fun getRecommendPlayUrl(position: Int, callback: UrlCallback) {
        getPlayUrl(position, callback, recommendObserver)
    }

    fun getPlayUrl(position: Int, callback: UrlCallback, liveData: MutableLiveData<MutableList<PlayItemViewBean>>) {
        liveData.value?.let {
            val bean = it[position]
            val request = PathRequest()
            request.name = bean.record?.bean?.name
            request.path = bean.record?.bean?.directory
            loadingObserver.value = true
            AppHttpClient.getInstance().getAppService().getVideoPath(request)
                .flatMap { response -> UrlUtil.toVideoUrl(response) }
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<String>(getComposite()) {
                    override fun onNext(url: String) {
                        loadingObserver.setValue(false)
                        it[position].playUrl = url
                        callback.onReceiveUrl(url)
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadingObserver.value = false
                        messageObserver.value = e?.message
                        getPlayUrlFailed.value = true
                    }
                })
        }
    }

    /**
     * load home data except recommend
     */
    fun loadRecentVideos() {
        mOffset = 0
        loadingObserver.value = true
        recordRepository.getLatestPlayableRecords(mOffset, LOAD_NUM)
            .flatMap{ list -> toPlayItems(list) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<PlayItemViewBean>>(getComposite()) {
                override fun onNext(list: MutableList<PlayItemViewBean>) {
                    loadingObserver.value = false
                    mOffset += list.size
                    recentVideosObserver.value = list
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    fun loadMore() {
        recordRepository.getLatestPlayableRecords(mOffset, LOAD_NUM)
            .flatMap { list -> toPlayItems(list) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<PlayItemViewBean>>(getComposite()) {
                override fun onNext(list: MutableList<PlayItemViewBean>) {
                    recentVideosObserver.value?.addAll(list)
                    mOffset += list.size
                    recentVideosObserver.value = recentVideosObserver.value
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun saveItemToAddOrder(bean: PlayItemViewBean) {
        mItemToAddOrder = bean
    }

    fun insertToPlayList(list: ArrayList<CharSequence>) {
        mItemToAddOrder?.let {
            val observable: Observable<Boolean>
            loadingObserver.setValue(true)
            if (it.playUrl?.isEmpty() == true) {
                val request = PathRequest()
                request.path = it.record?.bean?.directory
                request.name = it.record?.bean?.name
                observable = AppHttpClient.getInstance().getAppService().getVideoPath(request)
                    .flatMap { response -> UrlUtil.toVideoUrl(response) }
                    .flatMap { url -> insertToPlayerListDb(list, it.record?.bean?.id!!, it.playUrl) }
            } else {
                observable = insertToPlayerListDb(list, it.record?.bean?.id!!, it.playUrl)
            }
            observable
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<Boolean>(getComposite()) {

                    override fun onNext(pass: Boolean) {
                        loadingObserver.value = false
                        messageObserver.value = "success"
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadingObserver.value = false
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    private fun insertToPlayerListDb(
        orderIds: ArrayList<CharSequence>,
        recordId: Long,
        url: String?
    ): Observable<Boolean> {
        return Observable.create {
            if (orderIds.isNotEmpty()) {
                var list = mutableListOf<PlayItem>()
                for (id in orderIds) {
                    val orderId = id.toString().toLong()
                    if (playRepository.isExist(orderId, recordId)) {
                        continue
                    }
                    val item = PlayItem(null, orderId, recordId, url)
                    list.add(item)
                }
                getDatabase().getPlayOrderDao().insertPlayItems(list)
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    fun updateRecommend(bean: RecommendBean) {
        SettingProperty.setVideoRecBean(bean)
        loadRecommend()
    }

    fun playItem(item: PlayItemViewBean) {
        // 将视频url添加到临时播放列表的末尾
        PlayListInstance.getInstance().addPlayItemViewBean(item)
        PlayListInstance.getInstance().setPlayIndexAsLast()
        videoPlayOnReadyObserver.value = true
    }
}