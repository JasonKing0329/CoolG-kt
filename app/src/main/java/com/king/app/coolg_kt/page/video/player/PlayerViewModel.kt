package com.king.app.coolg_kt.page.video.player

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.PlayList
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/15 16:36
 */
class PlayerViewModel(application: Application) : BaseViewModel(application) {

    interface RetryObserver {
        fun retry()
    }

    var playModeText: ObservableField<String> = ObservableField()
    var playListText: ObservableField<String> = ObservableField()

    var closeListObserver: MutableLiveData<Boolean> = MutableLiveData()
    var stopVideoObserver: MutableLiveData<Boolean> = MutableLiveData()
    /**
     * 列表已播放完毕，询问是否从第一个item开始从头播放
     */
    var askIfLoop = MutableLiveData<Boolean>()

    /**
     * 获取url失败，提示是否重新获取
     */
    var retryLoadUrl = MutableLiveData<RetryObserver>()

    /**
     * 播放列表数据
     */
    var itemsObserver = MutableLiveData<MutableList<PlayList.PlayItem>>()

    private var mPlayList = mutableListOf<PlayList.PlayItem>()

    /**
     * 播放列表focus位置，mPlayIndex
     */
    var focusToIndex: MutableLiveData<Int> = MutableLiveData()

    /**
     * 仅仅setup视频，点击start按钮才正式开始播放
     */
    var onlySetupVideo: MutableLiveData<PlayList.PlayItem> = MutableLiveData()

    /**
     * 视频url已准备好，开始播放，mPlayBean
     */
    var playVideo: MutableLiveData<PlayList.PlayItem> = MutableLiveData()

    /**
     * 初始化自动播放
     */
    var initAutoPlay = false

    /**
     * 当前播放的play item
     */
    private var mPlayBean: PlayList.PlayItem? = null

    /**
     * 当前播放的位置（ui list中）
     */
    private var mPlayIndex = -1

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

    init {
        updatePlayListText()
        updatePlayModeText()
    }

    val startSeek: Int
        get() = mPlayBean?.playTime?:0

    fun resetPlayInDb() {
        mPlayBean?.let {
            it.playTime = 0
            PlayListInstance.getInstance().updatePlayItem(it)
        }
    }

    private fun updatePlayModeText() {
        if (isRandomPlay) {
            playModeText.set("随机")
        } else {
            playModeText.set("顺序")
        }
    }

    private fun updatePlayListText() {
        playListText.set("Play List(${mPlayList.size})")
    }

    // 加载播放列表
    private fun loadPlayList(): Observable<MutableList<PlayList.PlayItem>> {
        return Observable.create { e ->
            val playList = PlayListInstance.getInstance().playList
            // 播放模式：随机/顺序
            isRandomPlay = playList.playMode == 1
            updatePlayModeText()
            // 播放列表图片
            playList.list.forEach {
                val record = getDatabase().getRecordDao().getRecord(it.recordId)
                record?.let { rec ->
                    it.imageUrl = ImageProvider.getRecordRandomPath(rec.bean.name, null)
                }
            }
            e.onNext(playList.list)
            e.onComplete()
        }
    }

    fun loadPlayItems() {
        loadingObserver.value = true
        loadPlayList()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<PlayList.PlayItem>>(getComposite()) {
                override fun onNext(t: MutableList<PlayList.PlayItem>) {
                    loadingObserver.value = false
                    mPlayList = t
                    itemsObserver.value = t
                    updatePlayListText()
                    if (t.size > 0) {
                        mPlayBean = t.last()
                        mPlayIndex = t.lastIndex
                        // 初始化自动播放，播放最末视频
                        if (initAutoPlay) {
                            playItem(mPlayBean!!, mPlayIndex)
                        }
                        // 只定位到最末，不播放
                        else {
                            focusToIndex.value = mPlayIndex
                            onlySetupVideo.value = mPlayBean
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    /**
     * 播放item
     */
    fun playItem(bean: PlayList.PlayItem, index: Int) {
        mPlayBean = bean
        mPlayIndex = index
        focusToIndex.value = index
        if (bean.url == null) {
            loadPlayUrl(bean, index)
        }
        else {
            playVideo.value = bean
        }
    }

    /**
     * 重新获取当前item的url
     */
    fun reloadPlayUrl() {
        mPlayBean?.let {
            loadPlayUrl(it, mPlayIndex)
        }
    }

    /**
     * 获取item的播放地址
     */
    private fun loadPlayUrl(item: PlayList.PlayItem, index: Int) {
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
                        if (s.isEmpty()) {
                            messageObserver.value = "The play url of video is empty!"
                        }
                        else {
                            item.url = s
                            playItem(item, index)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        loadingObserver.value = false
                        e?.printStackTrace()
                        retryLoadUrl.value = object : RetryObserver {
                            override fun retry() {
                                loadPlayUrl(item, index)
                            }
                        }
                    }
                })
        }
    }

    private fun clearViewList() {
        mPlayIndex = -1
        mPlayList.clear()
        randomPlay.remains.clear()
    }

    /**
     * 清除播放列表
     */
    fun clearAll() {
        // 从UI中删除
        clearViewList()
        // 从播放列表持久化删除
        PlayListInstance.getInstance().clearPlayList()
        itemsObserver.value = mPlayList

        updatePlayListText()
    }

    /**
     * 切换播放模式：随机/顺序
     */
    fun switchPlayMode() {
        isRandomPlay = !isRandomPlay
        updatePlayModeText()
        PlayListInstance.getInstance().updatePlayMode(if (isRandomPlay) 1 else 0)
    }

    /**
     * 切换播放模式
     */
    fun setIsCustomRandomPlay(enable: Boolean) {
        if (isCustomRandomPlay == enable) {
            return
        }
        isCustomRandomPlay = enable
        // 自定义随机播放
        if (isCustomRandomPlay) {
            // 生成随机item，并通知列表变化，不自动播放
            randomVideo(false)
        }
        // 恢复正常播放列表
        else {
            itemsObserver.value = mPlayList
            if (mPlayList.size > 0) {
                // 定位到最后一个item
                focusToIndex.value = mPlayList.lastIndex
                PlayListInstance.getInstance().updatePlayIndex(mPlayList.lastIndex)
            }
        }
    }

    /**
     * 生成自定义随机item
     * @param autoPlay 生成后是否自动播放
     */
    private fun randomVideo(autoPlay: Boolean) {
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
                    // 自定义随机模式下，列表始终只显示一个
                    var list = mutableListOf<PlayList.PlayItem>()
                    list.add(playItem)
                    itemsObserver.value = list
                    mPlayIndex = 0
                    if (autoPlay) {
                        playItem(playItem, mPlayIndex)
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    /**
     * 将自定义随机item加入到全部播放列表中
     */
    private fun addRandomItemToPlayList(recordList: List<RecordWrap>): ObservableSource<PlayList.PlayItem> {
        return ObservableSource<PlayList.PlayItem> { observer ->
            if (recordList.isNotEmpty()) {
                var record = recordList[0]
                // 加入到播放列表中
                val item = PlayListInstance.getInstance().addRecord(record.bean, null)
                // 加载图片
                item.imageUrl = ImageProvider.getRecordRandomPath(record.bean.name, null)
                // 加入到ui list中
                mPlayList.add(item)
                observer.onNext(item)
            }
            observer.onComplete()
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

    /**
     * 播放下一个
     */
    fun playNext() {
        // 自定义随机模式下，自动播放随机生成item
        if (isCustomRandomPlay) {
            randomVideo(true)
        } else {
            playNextInList()
        }
    }

    /**
     * 播放上一个
     * 只播放playlist中的上一个
     */
    fun playPrevious() {
        if (mPlayList.isEmpty()) {
            messageObserver.value = "No video"
            return
        }
        // 没有上一个了，直接提示没有了，不再从尾部往上
        if (mPlayIndex == 0) {
            messageObserver.value = "No more videos"
            return
        }
        mPlayIndex --
        kotlin.runCatching {
            mPlayBean = mPlayList[mPlayIndex]
            playItem(mPlayBean!!, mPlayIndex)
        }.let {
            if (it.isFailure) {
                messageObserver.value = it.toString()
            }
        }
    }

    /**
     * 播放全部播放列表中的下一个
     */
    private fun playNextInList() {
        if (mPlayList.isEmpty()) {
            messageObserver.value = "No video"
            return
        }
        // 随机播放
        if (isRandomPlay) {
            mPlayIndex = randomPosition()
        }
        // 顺序播放
        else {
            // 没有下一个了，询问是否从头再来
            if (mPlayIndex + 1 >= mPlayList.size) {
                askIfLoop.value = true
                return
            }
            mPlayIndex ++;
        }
        kotlin.runCatching {
            mPlayBean = mPlayList[mPlayIndex]
            playItem(mPlayBean!!, mPlayIndex)
        }.let {
            if (it.isFailure) {
                messageObserver.value = it.toString()
            }
        }
    }

    /**
     * 从头播放
     */
    fun playFromBegin() {
        kotlin.runCatching {
            mPlayIndex = 0
            mPlayBean = mPlayList[mPlayIndex]
            playItem(mPlayBean!!, mPlayIndex)
        }
    }

    /**
     * 重新产生全部随机位置
     */
    private fun fillRandomRemains() {
        for (i in mPlayList.indices) {
            randomPlay.remains.add(i)
        }
        randomPlay.remains.shuffle()
    }

    /**
     * 生成随机播放位置
     */
    private fun randomPosition(): Int {
        // 没有剩余随机内容了，重新填满随机列表
        if (randomPlay.remains.isEmpty()) {
            fillRandomRemains()
        }
        // 取第一个并删除
        var position = randomPlay.remains[0]
        randomPlay.remains.removeAt(0)
        return position
    }

    /**
     * 更新视频时长
     */
    fun updateDuration(duration: Long) {
        mPlayBean?.let {
            it.duration = duration.toInt()
            PlayListInstance.getInstance().updatePlayItem(it)
        }
    }

    /**
     * 更新播放时间
     */
    fun updatePlayPosition(currentPosition: Long) {
        mPlayBean?.playTime = currentPosition.toInt()
    }

    /**
     * 将播放时间持久化
     */
    fun updatePlayToDb() {
        mPlayBean?.let {
            PlayListInstance.getInstance().updatePlayItem(it)
        }
    }

    /**
     * 删除播放条目
     */
    fun deletePlayItem(position: Int, item: PlayList.PlayItem) {
        PlayListInstance.getInstance().deleteItem(item)
        mPlayList.removeAt(position)
        if (mPlayIndex >= position) {
            if (mPlayIndex == position) {
                stopVideoObserver.value = true
            }
            mPlayIndex--
            focusToIndex.value = mPlayIndex
            PlayListInstance.getInstance().updatePlayIndex(mPlayIndex)
        }
        updatePlayListText()
    }

    /**
     * 修改RecommendBean直接存储，playNext里会重新加载生效
     */
    fun updateRecommend(bean: RecommendBean) {
        SettingProperty.setVideoRecBean(bean)
    }
}