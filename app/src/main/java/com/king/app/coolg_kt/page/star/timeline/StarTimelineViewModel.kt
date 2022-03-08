package com.king.app.coolg_kt.page.star.timeline

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.HideTimelineStars
import com.king.app.coolg_kt.model.bean.TimelineStar
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.module.BasicAndTimeWaste
import com.king.app.coolg_kt.model.module.TimeWasteTask
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.Star
import io.reactivex.rxjava3.core.Observable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/3/8 13:16
 */
class StarTimelineViewModel(application: Application): BaseViewModel(application) {

    var timelineItems = MutableLiveData<List<TimelineStar>>()
    var imageChanged = MutableLiveData<TimeWasteRange>()

    val repository = StarRepository()

    val dateFormat = SimpleDateFormat("yyyy-MM")

    var hideStars = HideTimelineStars(mutableListOf())

    var isShowHiddenStar = false

    fun loadItems() {
        BasicAndTimeWaste<TimelineStar>()
            .basic(formatStars())
            .timeWaste(imageWast, 20)
            .composite(getComposite())
            .subscribe(
                object : SimpleObserver<List<TimelineStar>>(getComposite()) {
                    override fun onNext(t: List<TimelineStar>) {
                        timelineItems.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message?:"error"
                    }
                },
                object : SimpleObserver<TimeWasteRange>(getComposite()) {
                    override fun onNext(t: TimeWasteRange) {
                        imageChanged.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                    }
                },
            )
    }

    private fun formatStars(): Observable<List<TimelineStar>> {
        return Observable.create {
            val result = mutableListOf<TimelineStar>()
            hideStars = SettingProperty.getHideTimelineStars()
            var lastDate = ""
            // getDebutStars已按时间升序排序
            repository.getDebutStars().forEach { item ->
                val isHidden = hideStars.idList.contains(item.starId)
                if (!isHidden || isShowHiddenStar) {
                    // 显示的时间取到月，重复的不显示
                    var date = dateFormat.format(Date(item.debut))
                    if (date.equals(lastDate)) {
                        date = ""
                    }
                    else {
                        lastDate = date
                    }
                    val type = defineStarType(item.star)
                    result.add(TimelineStar(item.star, type, item.debut, " $date ", isHidden))
                }
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun defineStarType(star: Star): Int {
        val topRate = star.betop.toDouble() / (star.betop + star.bebottom).toDouble() * 100
        val bottomRate = star.bebottom.toDouble() / (star.betop + star.bebottom).toDouble() * 100
        return if (topRate > 70) {
            DataConstants.VALUE_RELATION_TOP
        }
        else if (bottomRate > 70) {
            DataConstants.VALUE_RELATION_BOTTOM
        }
        else {
            DataConstants.VALUE_RELATION_MIX
        }
    }

    private var imageWast = object : TimeWasteTask<TimelineStar> {
        override fun handle(index: Int, data: TimelineStar) {
            data.imageUrl = ImageProvider.getStarRandomPath(data.star.name, null)
        }
    }

    fun updateHidden(position: Int, starId: Long, hide: Boolean) {
        if (hide) {
            hideStars.idList.add(starId)
        }
        else {
            hideStars.idList.remove(starId)
        }
        SettingProperty.setHideTimelineStars(hideStars)

        timelineItems.value?.get(position)?.isHidden = hide
        if (hide && !isShowHiddenStar) {
            val list = timelineItems.value?.toMutableList()
            list?.removeAt(position)
            timelineItems.value = list
        }
        else {
            timelineItems.value = timelineItems.value
        }
    }

    fun toggleHiddenItems() {
        isShowHiddenStar = !isShowHiddenStar
        loadItems()
    }
}