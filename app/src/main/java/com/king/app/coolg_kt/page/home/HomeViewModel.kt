package com.king.app.coolg_kt.page.home

import android.app.Application
import android.graphics.BitmapFactory
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.request.PathRequest
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.gdb.data.entity.PlayItem
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.RecordStarWrap
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/24 11:21
 */
class HomeViewModel(application: Application): BaseViewModel(application) {

    private val LOAD_NUM = 20

    private var mOffset = 0

    private var recordRepository = RecordRepository()

    var newRecordsObserver = MutableLiveData<Int>()
    var dataLoaded = MutableLiveData<Boolean>()

    var viewList = mutableListOf<Any>()

    var menuStarUrl = ObservableField<String>()
    var menuRecordUrl = ObservableField<String>()
    var menuVideoUrl = ObservableField<String>()
    var menuStudioUrl = ObservableField<String>()

    var dateFormat = SimpleDateFormat("yyyy-MM-dd")

    private var isLoadingMore = false

    private var mRecordAddViewOrder: Record? = null

    fun loadData() {
        mOffset = 0
        viewList.clear()
        loadingObserver.value = true
        recordRepository.getLatestRecords(mOffset, LOAD_NUM)
            .flatMap { toViewList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(list: List<Any>) {
                    loadingObserver.value = false
                    viewList.addAll(list)
                    DebugLog.e("viewList.size=${viewList.size}")
                    dataLoaded.value = true
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    fun loadMore() {
        if (isLoadingMore) {
            return
        }
        isLoadingMore = true

        recordRepository.getLatestRecords(mOffset, LOAD_NUM)
            .flatMap { toViewList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(list: List<Any>) {
                    viewList.addAll(list)
                    DebugLog.e("viewList.size=${viewList.size}")
                    newRecordsObserver.value = list.size
                    isLoadingMore = false
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                    isLoadingMore = false
                }
            })
    }

    /**
     * 返回实际添加的item数，不包含HomeFoot
     */
    private fun toViewList(list: List<RecordWrap>): ObservableSource<List<Any>> {
        return ObservableSource {
            DebugLog.e("start toViewList")

            mOffset += list.size
            var addList = mutableListOf<Any>()
            var lastDate = findLastDate()
            list.forEach {  record ->
                var homeRecord = toHomeRecord(record, lastDate)
                addList.add(homeRecord)
                lastDate = homeRecord.date

                var stars = getDatabase().getRecordDao().getRecordStars(record.bean.id!!)
                    .filter { s -> s.bean.score >= 80 }
                    .sortedByDescending { s -> s.bean.score }
                    // 超出两个只取前两个
                    .take(2)
                stars.forEach { star ->
                    var homeStar = toHomeStar(star, stars.size, lastDate)
                    addList.add(homeStar)
                }
            }

            DebugLog.e("addList.size=${viewList.size}")

            it.onNext(addList)
            it.onComplete()
        }
    }

    private fun toHomeStar(star: RecordStarWrap, totalSize: Int, date: String): HomeStar {
        // image url
        star.imageUrl = ImageProvider.getStarRandomPath(star.star.name, null)

        // as list member
        var homeStar = HomeStar(star, date)
        homeStar.cell = if (totalSize == 1) 2 else 1

        if (homeStar.cell == 2) {
            // 按屏幕宽度缩放高度
            homeStar.imageHeight = calcImageHeight(star.imageUrl, ScreenUtils.getScreenWidth())
        }
        return homeStar
    }

    private fun toHomeRecord(record: RecordWrap, lastDate: String): HomeRecord {
        // image url
        record.imageUrl = ImageProvider.getRecordRandomPath(record.bean.name, null)
        // date
        var date = dateFormat.format(Date(record.bean.lastModifyTime))
        // starText
        var starBuffer = StringBuffer()
        record.starList.forEach { s ->
            starBuffer.append("&").append(s.name)
        }
        return HomeRecord(record, date, date != lastDate)
    }


    private fun calcImageHeight(url: String?, baseWidth: Int): Int {
        // 无图按16:9
        return if (url == null || !File(url).exists()) {
            0
        } else {
            //缩放图片的实际宽高
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(url, options)
            var height = options.outHeight
            val width = options.outWidth
            val ratio = baseWidth.toFloat() / width.toFloat()
            (height * ratio).toInt()
        }
    }

    private fun findLastDate(): String {
        var date = ""
        for (item in viewList.reversed()) {
            if (item is HomeRecord) {
                date = item.date
                break
            }
        }
        return date
    }

    fun createMenuIconUrl() {
        getMenuIconUrl()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<String>>(getComposite()) {
                override fun onNext(t: List<String>) {
                    if (t.isNotEmpty()) {
                        menuStarUrl.set(t[0])
                    }
                    if (t.size > 1) {
                        menuRecordUrl.set(t[1])
                    }
                    if (t.size > 2) {
                        menuStudioUrl.set(t[2])
                    }
                    if (t.size > 3) {
                        menuVideoUrl.set(t[3])
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun getMenuIconUrl(): Observable<List<String>> {
        return Observable.create {
            var stars = getDatabase().getStarDao().getStarByRating(3.8f, 10)
            var urls = mutableListOf<String>()
            for (star in stars) {
                var url = ImageProvider.getStarRandomPath(star.name, null)
                url?.let {
                    urls.add(url)
                }
                if (urls.size == 4) {
                    break
                }
            }
            it.onNext(urls)
            it.onComplete()
        }
    }

    fun saveRecordToAddViewOrder(record: Record) {
        mRecordAddViewOrder = record
    }

    fun insertToPlayList(list: ArrayList<CharSequence>?) {
        if (list == null) {
            return
        }
        mRecordAddViewOrder?.let {
            val request = PathRequest()
            request.path = it.directory
            request.name = it.name
            loadingObserver.value = true
            AppHttpClient.getInstance().getAppService().getVideoPath(request)
                .flatMap { response -> UrlUtil.toVideoUrl(response) }
                .flatMap { url -> insertToPlayerListDb(list, it.id!!, url) }
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                    override fun onNext(t: Boolean) {
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

    private fun insertToPlayerListDb(list: ArrayList<CharSequence>, recordId: Long, url: String): ObservableSource<Boolean> {
        return ObservableSource {
            var list = mutableListOf<PlayItem>()
            list.forEach { id ->
                val orderId: Long = id.toString().toLong()
                // 不存在才插入
                if (getDatabase().getPlayOrderDao().countPlayItem(recordId, orderId) == 0) {
                    var item = PlayItem(null, orderId, recordId, url)
                    list.add(item)
                }
            }
            if (list.isNotEmpty()) {
                getDatabase().getPlayOrderDao().insertPlayItems(list)
            }
            it.onNext(true)
            it.onComplete()
        }
    }
}