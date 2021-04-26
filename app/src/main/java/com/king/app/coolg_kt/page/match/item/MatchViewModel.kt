package com.king.app.coolg_kt.page.match.item

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.RankRepository
import com.king.app.coolg_kt.page.match.MatchSemiItem
import com.king.app.coolg_kt.page.match.MatchSemiPack
import io.reactivex.rxjava3.core.Observable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/4/26 14:55
 */
class MatchViewModel(application: Application): BaseViewModel(application) {

    var matchId: Long = 0

    var matchImage = ObservableField<String>()
    var matchName = ObservableField<String>()
    var matchLevel = ObservableField<String>()

    var itemsObserver = MutableLiveData<List<MatchSemiPack>>()
    var repository = RankRepository()
    var dateFormat = SimpleDateFormat("yyyy-MM-dd")

    fun loadItems() {
        var match = getDatabase().getMatchDao().getMatch(matchId)
        matchImage.set(ImageProvider.parseCoverUrl(match.imgUrl))
        matchName.set(match.name)
        matchLevel.set("${MatchConstants.MATCH_LEVEL[match.level]} ")

        querySemiItems()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<MatchSemiPack>>(getComposite()) {
                override fun onNext(t: List<MatchSemiPack>) {
                    itemsObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message?:"error"
                }
            })
    }

    private fun querySemiItems(): Observable<List<MatchSemiPack>> {
        return Observable.create {
            var list = mutableListOf<MatchSemiPack>()
            var matches = getDatabase().getMatchDao().getMatchPeriods(matchId)
            matches.forEach { mp ->
                var records = repository.getMatchSemiItems(mp)
                var items = mutableListOf<MatchSemiItem>()
                var pack = MatchSemiPack(mp.id, "P${mp.period}", dateFormat.format(Date(mp.date)), items)
                list.add(pack)
                records.forEach { record ->
                    var rank = if (record.recordSeed?:0 > 0) {
                        "[${record.recordSeed}]/${record.recordRank}"
                    }
                    else {
                        record.recordRank?.toString()?:""
                    }
                    val bean = getDatabase().getRecordDao().getRecordBasic(record.recordId)
                    var item = MatchSemiItem(record.recordId, rank, ImageProvider.getRecordRandomPath(bean?.name, null))
                    items.add(item)
                }
            }
            it.onNext(list)
            it.onComplete()
        }
    }
}