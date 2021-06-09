package com.king.app.coolg_kt.page.match.h2h

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.H2hRepository
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.ImageRange
import com.king.app.coolg_kt.page.match.RecordH2hItem
import com.king.app.gdb.data.relation.RecordWrap
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/8 14:47
 */
class RecordH2hViewModel(application: Application): BaseViewModel(application) {

    var listObserver = MutableLiveData<List<RecordH2hItem>>()

    var imageChanged = MutableLiveData<ImageRange>()

    var recordNameText = ObservableField<String>()

    var currentRankText = ObservableField<String>()

    var highRankText = ObservableField<String>()

    var highRankWeekText = ObservableField<String>()

    var cptNumText = ObservableField<String>()

    var recordImage: String? = null

    var rankRepository = RankRepository()

    var h2hRepository = H2hRepository()

    lateinit var recordWrap: RecordWrap

    fun loadInfo(recordId: Long) {
        getDatabase().getRecordDao().getRecord(recordId)?.let {
            recordWrap = it

            // basic info
            recordNameText.set(it.bean.name)
            recordImage = ImageProvider.getRecordRandomPath(it.bean.name, null)

            currentRankText.set(rankRepository.getRecordCurrentRank(recordId).toString())
            val high = getDatabase().getMatchDao().getRecordHighestRank(recordId)
            getDatabase().getMatchDao().getRecordRankFirstTime(recordId, high)?.let { mrr ->
                highRankText.set("$high(P${mrr.period}-W${mrr.orderInPeriod})")
            }
            val highWeeks = getDatabase().getMatchDao().getRecordRankWeeks(recordId, high)
            if (highWeeks > 1) {
                highRankWeekText.set("$highWeeks weeks")
            }
            else {
                highRankWeekText.set("$highWeeks week")
            }

            // players
            loadPlayers()
        }
    }

    private fun loadPlayers() {
        h2hRepository.getRecordCompetitors(recordWrap.bean)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<RecordH2hItem>>(getComposite()) {
                override fun onNext(t: List<RecordH2hItem>) {
                    cptNumText.set("${t.size} competitors")
                    listObserver.value = t
                    loadImages(t)
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message?:""
                }
            })
    }

    /**
     * 加载图片路径属于耗时操作，单独完成
     */
    private fun loadImages(list: List<RecordH2hItem>) {
        loadTimeWaste(list)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<ImageRange>(getComposite()){
                override fun onNext(t: ImageRange) {
                    imageChanged.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun loadTimeWaste(items: List<RecordH2hItem>): Observable<ImageRange> {
        return Observable.create {
            if (items.isNotEmpty()) {
                var count = 0
                var totalNotified = 0
                items.forEach { item ->

                    // 每30条通知一次
                    item.recordImg1 = recordImage
                    var url = ImageProvider.getRecordRandomPath(item.record2.name, null)
                    item.recordImg2 = url

                    count ++
                    if (count % 30 == 0) {
                        it.onNext(ImageRange(count - 30, count))
                        totalNotified = count
                    }
                }
                if (totalNotified != items.size) {
                    it.onNext(ImageRange(totalNotified, items.size - totalNotified))
                }
            }
            it.onComplete()
        }
    }
}