package com.king.app.coolg_kt.page.video.order

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.bean.VideoGuy
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.repository.PlayRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.entity.VideoCoverStar
import com.king.app.gdb.data.relation.VideoCoverStarWrap
import io.reactivex.rxjava3.core.Observable
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/4 16:10
 */
class PopularStarViewModel(application: Application): BaseViewModel(application) {

    private val SORT_BY_NAME = 0

    private val SORT_BY_VIDEO = 1

    private var mSortType = SORT_BY_NAME

    var mViewType = SettingProperty.getVideoStarOrderViewType()

    var starsObserver: MutableLiveData<List<VideoGuy>> = MutableLiveData()

    var playRepository = PlayRepository()

    fun loadStars() {
        var list = getDatabase().getPlayOrderDao().getVideoCoverStarWraps()
        loadingObserver.value = true
        toVideoGuys(list)
            .flatMap { sortGuys(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<List<VideoGuy>>(getComposite()) {
                override fun onNext(t: List<VideoGuy>?) {
                    loadingObserver.value = false
                    starsObserver.value = t
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    private fun toVideoGuys(list: List<VideoCoverStarWrap>): Observable<List<VideoGuy>> {
        return Observable.create {
            var result = mutableListOf<VideoGuy>()
            var baseWidth = when {
                ScreenUtils.isTablet() -> ScreenUtils.getScreenWidth() / 3
                mViewType == PreferenceValue.VIEW_TYPE_GRID -> ScreenUtils.getScreenWidth() / 2
                else -> ScreenUtils.getScreenWidth()
            }
            list.forEach { bean ->
                bean?.star?.let { star ->
                    var guy = VideoGuy()
                    guy.star = star
                    guy.videos = getDatabase().getRecordDao().countStarOnlineRecords(star.id!!)
                    guy.imageUrl = ImageProvider.getStarRandomPath(star.name, null)
                    calcImageSize(guy, baseWidth)
                    result.add(guy)
                }
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    private fun sortGuys(list: List<VideoGuy>): Observable<List<VideoGuy>> {
        return Observable.create {
            var result = if (mSortType == SORT_BY_VIDEO) {
                list.sortedByDescending { guy -> guy.videos }
            }
            else {
                list.sortedBy { guy -> guy.star?.name }
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    fun sortByVideo() {
        if (mSortType != SORT_BY_VIDEO) {
            mSortType = SORT_BY_VIDEO
            sort()
        }
    }

    fun sortByName() {
        if (mSortType != SORT_BY_NAME) {
            mSortType = SORT_BY_NAME
            sort()
        }
    }

    fun sort() {
        starsObserver.value?.let {
            sortGuys(it)
                .compose(applySchedulers())
                .subscribe(object : SimpleObserver<List<VideoGuy>>(getComposite()) {
                    override fun onNext(t: List<VideoGuy>?) {
                        loadingObserver.value = false
                        starsObserver.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        loadingObserver.value = false
                        messageObserver.value = e?.message
                    }
                })
        }
    }

    private fun calcImageSize(bean: VideoGuy, baseWidth: Int) {
        // 无图按16:9
        if (bean.imageUrl == null) {
            bean.width = baseWidth
            bean.height = baseWidth * 9 / 16
        } else {
            //缩放图片的实际宽高
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(bean.imageUrl, options)
            var height = options.outHeight
            val width = options.outWidth
            val ratio = baseWidth.toFloat() / width.toFloat()
            bean.width = baseWidth
            bean.height = (height * ratio).toInt()
        }
    }

    fun executeDelete() {
        starsObserver.value
            ?.filter { it.isChecked && it.star != null }
            ?.forEach { getDatabase().getPlayOrderDao().deleteVideoCoverStar(it.star!!.id!!) }
    }

    fun insertVideoCoverStar(list: ArrayList<CharSequence>) {
        insertCoverGuys(list)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<Boolean>(getComposite()) {
                override fun onNext(t: Boolean) {
                    loadStars()
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    private fun insertCoverGuys(list: ArrayList<CharSequence>): Observable<Boolean> {
        return Observable.create {
            val insertList = mutableListOf<VideoCoverStar>()
            list.forEach { str ->
                val starId = str.toString().toLong()
                // insert if not exist
                var bean = getDatabase().getPlayOrderDao().getVideoCoverStar(starId)
                if (bean == null) {
                    val coverStar = VideoCoverStar(starId)
                    insertList.add(coverStar)
                }
            }
            it.onNext(true)
            it.onComplete()
        }
    }

}