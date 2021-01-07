package com.king.app.coolg_kt.page.studio.phone

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.gdb.data.entity.FavorRecordOrder
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 10:51
 */
class StudioPageViewModel(application: Application): BaseViewModel(application) {

    val pageDataObserver = MutableLiveData<List<Any>>()

    var mStudio: FavorRecordOrder? = null

    fun loadPageData(studioId: Long) {
        loadingObserver.value = true
        mStudio = getDatabase().getFavorDao().getFavorRecordOrderBy(studioId)
        createPageData(studioId)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<Any>>(getComposite()) {
                override fun onNext(t: List<Any>) {
                    loadingObserver.value = false
                    pageDataObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

            })
    }

    private fun createPageData(studioId: Long): Observable<List<Any>> {
        return Observable.create {
            var list = mutableListOf<Any>()
            // head
            var total = getDatabase().getStarDao().getStudioStars(studioId).size
            list.add(PageStarHead(total))
            // stars
            var stars = getDatabase().getStarDao().getStudioTopStars(studioId, 9)
            stars.forEach { star ->
                star.imagePath = ImageProvider.getStarRandomPath(star.bean.name, null)
                list.add(star)
            }
            // recent records
            list.add(PageRecordHead("Latest Videos", AppConstants.STUDIO_RECORD_HEAD_RECENT))
            var recentRecords = getDatabase().getRecordDao().getStudioRecentRecords(studioId, 8)
            recentRecords.forEach { record ->
                record.imageUrl = ImageProvider.getRecordRandomPath(record.bean.name, null)
                list.add(record)
            }
            // top records
            list.add(PageRecordHead("Top Videos", AppConstants.STUDIO_RECORD_HEAD_TOP))
            var topRecords = getDatabase().getRecordDao().getStudioTopRecords(studioId, 8)
            topRecords.forEach { record ->
                record.imageUrl = ImageProvider.getRecordRandomPath(record.bean.name, null)
                list.add(record)
            }

            it.onNext(list)
            it.onComplete()
        }
    }
}