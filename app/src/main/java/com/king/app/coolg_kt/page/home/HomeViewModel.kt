package com.king.app.coolg_kt.page.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.ObservableSource
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

    var recordRepository = RecordRepository()

    var newRecordsObserver = MutableLiveData<Int>()
    var dataLoaded = MutableLiveData<Boolean>()

    var viewList = mutableListOf<Any>()

    var dateFormat = SimpleDateFormat("yyyy-MM-dd")
    fun loadData() {
        mOffset = 0
        viewList.clear()
        loadingObserver.value = true
        recordRepository.getLatestRecords(mOffset, LOAD_NUM)
            .flatMap { toViewList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Int>(getComposite()) {
                override fun onNext(count: Int) {
                    loadingObserver.value = false
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
        recordRepository.getLatestRecords(mOffset, LOAD_NUM)
            .flatMap { toViewList(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Int>(getComposite()) {
                override fun onNext(count: Int) {
                    newRecordsObserver.value = count
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toViewList(list: List<RecordWrap>): ObservableSource<Int> {
        return ObservableSource {
            mOffset += list.size
            var lastDate = findLastDate()
            var totalCount = 0
            list.forEach {  record ->
                var homeRecord = toHomeRecord(record, lastDate)
                viewList.add(homeRecord)
                lastDate = homeRecord.date
                totalCount ++

                var stars = getDatabase().getRecordDao().getRecordStars(record.bean.id!!)
                    .filter { s -> s.bean.score >= 80 }
                    .sortedByDescending { s -> s.bean.score }
                    // 超出两个只取前两个
                    .take(2)
                stars.forEach { star ->
                    // image url
                    star.imageUrl = ImageProvider.getStarRandomPath(star.star.name, null)

                    // as list member
                    var homeStar = HomeStar(star)
                    homeStar.cell = if (stars.size == 1) 2 else 1
                    viewList.add(homeStar)
                    totalCount ++
                }
            }
            it.onNext(totalCount)
            it.onComplete()
        }
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
}