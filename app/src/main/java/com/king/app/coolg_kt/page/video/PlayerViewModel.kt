package com.king.app.coolg_kt.page.video

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.PlayList
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider.getRecordRandomPath
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.util.*
import kotlin.collections.ArrayList

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/15 16:36
 */
class PlayerViewModel(application: Application) : BaseViewModel(application) {

    var itemsObserver: MutableLiveData<MutableList<PlayList.PlayItem>> = MutableLiveData()
    var playVideo: MutableLiveData<PlayList.PlayItem> = MutableLiveData()
    var prepareVideo: MutableLiveData<PlayList.PlayItem> = MutableLiveData()
    var closeListObserver: MutableLiveData<Boolean> = MutableLiveData()
    var stopVideoObserver: MutableLiveData<Boolean> = MutableLiveData()
    var videoUrlIsReady: MutableLiveData<PlayList.PlayItem> = MutableLiveData()
    var playIndexObserver: MutableLiveData<Int> = MutableLiveData()
    var playModeText: ObservableField<String> = ObservableField()
    var playListText: ObservableField<String> = ObservableField()
    private var mPlayList: MutableList<PlayList.PlayItem> = ArrayList()
    private var mPlayBean: PlayList.PlayItem? = null
    var playIndex = 0

    /**
     * 播放列表的播放模式：顺序、随机
     */
    private var isRandomPlay = false

    /**
     * 自定义条件随机播放模式（随机产生符合条件的record，并且会被加入到播放列表中）
     */
    private var isCustomRandomPlay = false
    private val randomPlay = RandomPlay()
    private val random = Random()
    private val recordRepository = RecordRepository()

    private fun updatePlayModeText() {
        if (isRandomPlay) {
            playModeText.set("随机")
        } else {
            playModeText.set("顺序")
        }
    }

    // 加载图片地址
    private fun getObservable(): Observable<MutableList<PlayList.PlayItem>> {
        return Observable.create { e ->
            val playList = PlayListInstance.getInstance().playList
            isRandomPlay = playList.playMode == 1
            updatePlayModeText()
            playList.list.forEach {
                val record = getDatabase().getRecordDao().getRecord(it.recordId)
                record?.let { rec ->
                    it.imageUrl = getRecordRandomPath(rec.bean.name, null)
                }
            }
            e.onNext(playList.list)
            e.onComplete()
        }
    }

    fun loadPlayItems(autoPlay: Boolean) {
        loadingObserver.value = true
        getObservable()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<PlayList.PlayItem>>(getComposite()) {

                override fun onNext(playItems: MutableList<PlayList.PlayItem>) {
                    loadingObserver.value = false
                    itemsObserver.value = playItems
                    mPlayList = playItems
                    updatePlayListText()
                    if (mPlayList.isEmpty()) {
                        messageObserver.setValue("No video")
                    } else {
                        val startIndex: Int = PlayListInstance.getInstance().playList.playIndex
                        randomPlay.current = startIndex
                        if (autoPlay) {
                            playVideoAt(startIndex)
                        } else {
                            prepareVideoAt(startIndex)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.setValue(true)
                    messageObserver.setValue(e?.message)
                }
            })
    }

    fun getVideoName(bean: PlayList.PlayItem): String {
        return bean.name?:bean.url?:""
    }

    fun clearViewList() {
        mPlayList.clear()
        randomPlay.remains.clear()
    }

    fun clearAll() {
        // 从UI中删除
        clearViewList()
        // 从播放列表持久化删除
        PlayListInstance.getInstance().clearPlayList()
        itemsObserver.value = mPlayList
    }

    fun switchPlayMode() {
        isRandomPlay = !isRandomPlay
        updatePlayModeText()
        PlayListInstance.getInstance().updatePlayMode(if (isRandomPlay) 1 else 0)
    }

    fun setIsCustomRandomPlay(enable: Boolean) {
        isCustomRandomPlay = enable
        if (isCustomRandomPlay) {
            sendRandomList()
        } else {
            if (mPlayList.size > 0) {
                playIndex = mPlayList.size - 1
                playIndexObserver.value = playIndex
                PlayListInstance.getInstance().updatePlayIndex(playIndex)
            }
            itemsObserver.setValue(mPlayList)
        }
    }

    private fun sendRandomList() {
        mPlayBean?.let {
            val list = mutableListOf<PlayList.PlayItem>()
            list.add(it)
            itemsObserver.setValue(list)
        }
    }

    fun updateDuration(duration: Long) {
        mPlayBean?.let {
            it.duration = duration.toInt()
            PlayListInstance.getInstance().updatePlayItem(it)
        }
    }

    /**
     * 随机规则
     * list中随机完一轮才随机新的序列号
     * 保存上一个及下一个随机的序号
     * next-->
     * >-1，last = current, current = next, next = -1
     * -1--> last = current, next = -1
     * remains > 0, current = new random
     * remains = 0, 新一轮随机，remains充满，current = new random
     * last-->
     * >-1, next = current, current = last, last = -1
     * -1--> next = current, last = -1
     * remains > 0, current = new random
     * remains = 0, 新一轮随机，remains充满，current = new random
     */
    private inner class RandomPlay {
        var current = 0
        var last = -1
        var next = -1
        var remains: MutableList<Int> = mutableListOf()
    }

    fun playNext() {
        if (isCustomRandomPlay) {
            val bean: RecommendBean = SettingProperty.getVideoRecBean()
            bean.isOnline = true
            bean.number = 1
            recordRepository.getRecordsBy(bean)
                .flatMap { list -> addRandomItemToPlayList(list) }
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<PlayList.PlayItem>(getComposite()) {

                    override fun onNext(playItem: PlayList.PlayItem) {
                        updatePlayListText()
                        mPlayBean = playItem
                        val record = getDatabase().getRecordDao().getRecord(playItem.recordId)
                        record?.let {
                            playItem.imageUrl = getRecordRandomPath(it.bean.name, null)
                        }
                        sendRandomList()
                        playVideoAt(0)
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }
                })
        } else {
            playNextInList()
        }
    }

    private fun addRandomItemToPlayList(recordList: List<RecordWrap>): ObservableSource<PlayList.PlayItem> {
        return ObservableSource<PlayList.PlayItem> { observer ->
            val item = PlayListInstance.getInstance().addRecord(recordList[0].bean, null)
            // 加入到播放列表中
            mPlayList.add(item)
            observer.onNext(item)
            observer.onComplete()
        }
    }

    private fun playNextInList() {
        if (mPlayList.isEmpty()) {
            messageObserver.value = "No video"
            return
        }
        if (isRandomPlay) {
            randomPlay.last = randomPlay.current
            if (randomPlay.next > -1) {
                randomPlay.current = randomPlay.next
            } else {
                randomPlay.current = randomPosition
            }
            playIndex = randomPlay.current
            randomPlay.next = -1
        } else {
            if (playIndex + 1 >= mPlayList.size) {
                messageObserver.setValue("No more videos")
                return
            }
            if (mPlayBean == null) {
                playIndex = 0
            } else {
                playIndex++
            }
        }
        playVideoAt(playIndex)
    }

    private val randomPosition: Int
        private get() {
            if (randomPlay.remains.isEmpty()) {
                randomPlay.remains = ArrayList()
                for (i in mPlayList.indices) {
                    randomPlay.remains.add(i)
                }
            }
            return if (randomPlay.remains.size == 1) {
                randomPlay.remains[0]
            } else {
                val index = Math.abs(random.nextInt()) % randomPlay.remains.size
                val position = randomPlay.remains!![index]
                randomPlay.remains!!.removeAt(index)
                position
            }
        }

    fun playPrevious() {
        if (mPlayList.isEmpty()) {
            messageObserver.value = "No video"
            return
        }
        if (isRandomPlay) {
            randomPlay.next = randomPlay.current
            if (randomPlay.last > -1) {
                randomPlay.current = randomPlay.last
            } else {
                randomPlay.current = randomPosition
            }
            playIndex = randomPlay.current
            randomPlay.last = -1
        } else {
            if (mPlayBean == null) {
                playIndex = 0
            } else {
                playIndex--
            }
            if (playIndex < 0) {
                messageObserver.value = "No more videos"
                return
            }
        }
        playVideoAt(playIndex)
    }

    private fun prepareVideoAt(position: Int) {
        playIndex = position
        itemsObserver.value?.let {
            if (playIndex > it.size) {
                playIndex = it.size - 1
            }
            if (playIndex < 0) {
                return
            }
            mPlayBean = it[playIndex]
            DebugLog.e("play " + mPlayBean!!.url)
            prepareVideo.value = mPlayBean
            playIndexObserver.value = playIndex
            PlayListInstance.getInstance().updatePlayIndex(playIndex)
        }
    }

    fun playVideoAt(position: Int) {
        prepareVideoAt(position)
        playVideo.value = mPlayBean
    }

    fun loadPlayUrl(item: PlayList.PlayItem) {
        var record = getDatabase().getRecordDao().getRecord(item.recordId)
        record?.let {
            val request = PathRequest()
            request.name = it.bean.name
            request.path = it.bean.directory
            loadingObserver.value = true
            AppHttpClient.getInstance().getAppService().getVideoPath(request)
                .flatMap { response -> UrlUtil.toVideoUrl(response) }
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<String>(compositeDisposable) {
                    override fun onNext(s: String) {
                        loadingObserver.value = false
                        item.url = s
                        videoUrlIsReady.value = item
                    }

                    override fun onError(e: Throwable?) {
                        loadingObserver.value = false
                        e?.printStackTrace()
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    val startSeek: Int
        get() = mPlayBean?.playTime?:0

    fun updatePlayPosition(currentPosition: Long) {
        mPlayBean?.playTime = currentPosition.toInt()
    }

    fun resetPlayInDb() {
        mPlayBean?.let {
            it.playTime = 0
            PlayListInstance.getInstance().updatePlayItem(it)
        }
    }

    fun updatePlayToDb() {
        mPlayBean?.let {
            PlayListInstance.getInstance().updatePlayItem(it)
        }
    }

    fun deletePlayItem(position: Int, item: PlayList.PlayItem) {
        PlayListInstance.getInstance().deleteItem(item)
        mPlayList.removeAt(position)
        if (position == playIndex) {
            stopVideoObserver.value = true
        }
        playIndex--
        if (playIndex >= 0) {
            playIndexObserver.value = playIndex
            PlayListInstance.getInstance().updatePlayIndex(playIndex)
        }
        updatePlayListText()
    }

    private fun updatePlayListText() {
        playListText!!.set("""Play List(${mPlayList.size})""")
    }

    fun updateRecommend(bean: RecommendBean) {
        SettingProperty.setVideoRecBean(bean)
    }
}