package com.king.app.coolg_kt.page.match.item

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.module.BasicAndTimeWaste
import com.king.app.coolg_kt.model.module.TimeWasteTask
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.page.match.WallItem
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/9 14:08
 */
class ChampionWallViewModel(application: Application): BaseViewModel(application) {

    var titlesObserver = MutableLiveData<List<WallItem>>()
    var itemsObserver = MutableLiveData<List<WallItem>>()
    var rangeChangedObserver = MutableLiveData<TimeWasteRange>()

    fun loadGsItems() {
        loadItems(MatchConstants.MATCH_LEVEL_GS)
    }

    fun loadGm1000Items() {
        loadItems(MatchConstants.MATCH_LEVEL_GM1000)
    }

    private fun loadItems(level: Int) {
        BasicAndTimeWaste<WallItem>()
            .basic(loadLevelData(level))
            .timeWaste(timeWaste(), 30)
            .composite(getComposite())
            .subscribe(
                object : SimpleObserver<List<WallItem>>(getComposite()) {
                    override fun onNext(t: List<WallItem>) {
                        itemsObserver.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                },
                object : SimpleObserver<TimeWasteRange>(getComposite()) {
                    override fun onNext(t: TimeWasteRange) {
                        rangeChangedObserver.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:""
                    }
                }
            )
    }

    private fun loadLevelData(level: Int): Observable<List<WallItem>> {
        return Observable.create {
            // titles
            val titles = mutableListOf<WallItem>()
            val matches = getDatabase().getMatchDao().getMatchByLevel(level)
            // 空占位
            titles.add(WallItem(true, ""))
            matches.forEach { match ->
                titles.add(WallItem(true, match.name))
            }
            titlesObserver.postValue(titles)

            // items
            val result = mutableListOf<WallItem>()
            val champions = getDatabase().getMatchDao().getMatchChampionsByLevel(level, MatchConstants.ROUND_ID_F)
            var lastPeriod = 0
            champions.forEach { item ->
                if (item.period != lastPeriod) {
                    lastPeriod = item.period
                    result.add(WallItem(true, "P$lastPeriod"))
                }
                result.add(WallItem(false, null, item.winnerId, item.matchPeriodId))
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun timeWaste(): TimeWasteTask<WallItem> {
        return object : TimeWasteTask<WallItem> {
            override fun handle(index: Int, data: WallItem) {
                data.recordId?.let { recordId ->
                    getDatabase().getRecordDao().getRecordBasic(recordId)?.let { record ->
                        data.imageUrl = findUrlBefore(index, recordId)
                        if (data.imageUrl == null) {
                            data.imageUrl = ImageProvider.getRecordRandomPath(record.name, null)
                        }
                    }
                }
            }
        }
    }

    private fun findUrlBefore(position: Int, recordId: Long): String? {
        for (i in 0 until position) {
            val item = itemsObserver.value!![i]
            if (item.recordId == recordId) {
                return item.imageUrl
            }
        }
        return null
    }
}