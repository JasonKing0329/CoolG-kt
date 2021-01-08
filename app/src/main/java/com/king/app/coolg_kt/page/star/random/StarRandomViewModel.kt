package com.king.app.coolg_kt.page.star.random

import android.app.Application
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider.getStarRandomPath
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.ColorUtil
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.relation.StarWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import kotlin.math.abs

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/9/7 10:25
 */
class StarRandomViewModel(application: Application) : BaseViewModel(application) {
    private val mCandidates: MutableList<StarWrap> = mutableListOf()
    private val mSelectedList: MutableList<StarWrap> = mutableListOf()
    private var mRandomList: MutableList<StarWrap>? = null
    private var randomDisposable: Disposable? = null
    var starObserver: MutableLiveData<StarWrap> = MutableLiveData()
    var candidatesObserver: MutableLiveData<MutableList<StarWrap>> = MutableLiveData()
    var selectedObserver: MutableLiveData<MutableList<StarWrap>> = MutableLiveData()
    var btnControlRes: ObservableInt = ObservableInt(R.drawable.ic_play_circle_filled_black_36dp)
    var starName: ObservableField<String> = ObservableField()
    private val random = Random()
    var randomRule = RandomRule()
    private val starRepository = StarRepository()
    private var maxHeight: Int
    private var maxWidth: Int
    private var mCurrentStar: StarWrap? = null

    init {
        maxWidth = ScreenUtils.getScreenWidth() - ScreenUtils.dp2px(16f) * 2
        maxHeight = (ScreenUtils.getScreenHeight() - application!!.resources
            .getDimensionPixelSize(R.dimen.star_random_img_top)
                - application.resources.getDimensionPixelSize(R.dimen.star_random_img_bottom))
    }

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
    }

    fun getCurrentStar(): StarWrap? {
        return mCurrentStar
    }

    fun onClickStart(view: View) {
        if (randomDisposable == null) {
            startRandom()
            btnControlRes!!.set(R.drawable.ic_pause_circle_filled_black_36dp)
        } else {
            stopRandom()
            btnControlRes!!.set(R.drawable.ic_play_circle_filled_black_36dp)
        }
        ColorUtil.updateIconColor(view as ImageView?, iconColor)
    }

    fun resetRandomList() {
        mRandomList = null
    }

    private fun startRandom() {
        prepareRandomList()
            .flatMap{ list ->
                mRandomList = list
                randomStar(list)
            }
            .flatMap { star -> toStarProxy(star) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<StarWrap>(getComposite()) {
                override fun onSubscribe(d: Disposable?) {
                    randomDisposable = d
                }

                override fun onNext(starProxy: StarWrap) {
                    mCurrentStar = starProxy
                    starName.set(starProxy.bean.name)
                    starObserver.setValue(starProxy)
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.setValue(e?.message)
                }
            })
    }

    fun stopRandom() {
        randomDisposable?.dispose()
        randomDisposable = null
    }

    private fun prepareRandomList(): Observable<MutableList<StarWrap>> {
        return Observable.create { e ->
            var list: MutableList<StarWrap>
            if (mRandomList == null) {
                // 优先运用candidates
                list = mCandidates
                // 没有才根据rule
                if (list.isEmpty()) {
                    var conditions: Array<String>? = null
                    try {
                        conditions = randomRule.sqlRating?.split(",".toRegex())?.toTypedArray()
                    } catch (ee: Exception) {
                    }
                    list = starRepository.queryStar(randomRule.starType, conditions).toMutableList()
                }
            } else {
                list = mRandomList!!
            }
            if (list.isEmpty()) {
                e.onError(Exception("No random data"))
            } else {
                e.onNext(list)
                e.onComplete()
            }
        }
    }

    private fun randomStar(randomList: List<StarWrap>): Observable<StarWrap> {
        return Observable.create { e ->
            try {
                while (randomDisposable != null) {
                    val index = abs(random.nextInt()) % randomList.size
                    e.onNext(randomList[index])
                    Thread.sleep(150)
                }
            } catch (ee: InterruptedException) {
                e.onComplete()
            }
        }
    }

    private fun toStarProxy(star: StarWrap): ObservableSource<StarWrap> {
        return ObservableSource { observer ->
            star.imagePath = getStarRandomPath(star.bean.name, null)
            calcImageSize(star)
            observer.onNext(star)
            observer.onComplete()
        }
    }

    private fun calcImageSize(bean: StarWrap) {
        if (bean.imagePath != null) {
            //缩放图片的实际宽高
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(bean.imagePath, options)
            var height = options.outHeight
            var width = options.outWidth
            // 超出才缩放
            if (height > maxHeight || width > maxWidth) {
                val r = width.toFloat() / height.toFloat()
                val rm = maxWidth.toFloat() / maxHeight.toFloat()
                // 按宽缩放
                if (r > rm) {
                    val ratio = maxWidth.toFloat() / width.toFloat()
                    bean.width = maxWidth
                    height = (height * ratio).toInt()
                    bean.height = height
                } else {
                    val ratio = maxHeight.toFloat() / height.toFloat()
                    bean.height = maxHeight
                    width = (width * ratio).toInt()
                    bean.width = width
                }
            } else {
                bean.width = width
                bean.height = height
            }
        }
    }

    fun setCandidates(list: ArrayList<CharSequence>?) {
        list?.let {
            it.forEach { item ->
                val starId = item.toString().toLong()
                val star = getDatabase().getStarDao().getStarWrap(starId)
                star?.let { wrap->
                    if (!isRepeatStar(wrap)) {
                        mCandidates.add(wrap)
                    }
                }
            }
            candidatesObserver.setValue(mCandidates)
            resetRandomList()
        }
    }

    private fun isRepeatStar(star: StarWrap): Boolean {
        for (s in mCandidates) {
            if (s.bean.id == star.bean.id) {
                return true
            }
        }
        return false
    }

    fun deleteCandidate(star: StarWrap) {
        mCandidates.remove(star)
        candidatesObserver.value = mCandidates
    }

    fun clearCandidates() {
        mCandidates.clear()
        candidatesObserver.value = mCandidates
    }

    val iconColor: Int
        get() = getApplication<Application>().resources.getColor(R.color.red_f1303d)

    fun markCurrentStar() {
        mCurrentStar?.let {
            mSelectedList.add(it)
            selectedObserver.value = mSelectedList
            if (randomRule.isExcludeFromMarked) {
                mRandomList?.remove(it)
            }
        }
    }

    fun deleteSelected(star: StarWrap) {
        mSelectedList.remove(star)
        selectedObserver.value = mSelectedList
        // 还要添加回随机列表
        if (randomRule.isExcludeFromMarked) {
            // 过滤重复项
            if (!isRepeatStar(star)) {
                mRandomList?.add(star)
            }
        }
    }

    fun clearAll() {
        mCandidates.clear()
        mSelectedList.clear()
        randomRule = RandomRule()
        mCurrentStar = null
        candidatesObserver.value = mCandidates
        selectedObserver.value = mSelectedList
    }

    override fun onDestroy() {
        val randomData = RandomData()
        randomData.name = "auto"
        randomData.randomRule = randomRule!!
        randomData.candidateList = mutableListOf()
        randomData.markedList = mutableListOf()
        for (star in mCandidates) {
            randomData.candidateList.add(star.bean.id!!)
        }
        for (star in mSelectedList) {
            randomData.markedList.add(star.bean.id!!)
        }
        SettingProperty.setStarRandomData(randomData)
        super.onDestroy()
    }

    /**
     * 初始化上一次的数据
     */
    fun loadDefaultData() {
        val data = SettingProperty.getStarRandomData()
        randomRule = data.randomRule
        if (randomRule == null) {
            randomRule = RandomRule()
        }
        for (id in data.markedList) {
            try {
                val star = getDatabase().getStarDao().getStarWrap(id)
                star?.let { wrap-> mSelectedList.add(wrap) }
            } catch (e: Exception) {
            }
        }
        selectedObserver.setValue(mSelectedList)
        for (id in data.candidateList) {
            try {
                val star = getDatabase().getStarDao().getStarWrap(id)
                star?.let { wrap-> mCandidates.add(wrap) }
            } catch (e: Exception) {
            }
        }
        candidatesObserver.setValue(mCandidates)
        prepareRandomList()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<MutableList<StarWrap>>(getComposite()) {
                override fun onNext(stars: MutableList<StarWrap>) {
                    mRandomList = stars
                    var removeList = mutableListOf<StarWrap>()
                    for (mark in mSelectedList) {
                        for (star in stars) {
                            if (star.bean.id == mark.bean.id) {
                                removeList.add(star)
                                break
                            }
                        }
                    }
                    removeList.forEach { mRandomList?.remove(it) }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }
}