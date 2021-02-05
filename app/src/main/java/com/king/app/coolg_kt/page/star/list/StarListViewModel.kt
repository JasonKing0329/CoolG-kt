package com.king.app.coolg_kt.page.star.list

import android.app.Application
import android.text.TextUtils
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.bean.StarBuilder
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider.getStarRandomPath
import com.king.app.coolg_kt.model.module.StarIndexEmitter
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.relation.StarWrap
import io.reactivex.rxjava3.core.Observable

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 14:52
 */
class StarListViewModel(application: Application) : BaseViewModel(application) {
    private var currentViewMode = 0

    // see AppConstants.STAR_SORT_XXX
    var sortType = AppConstants.STAR_SORT_NAME

    // see GDBProperties.STAR_MODE_XXX
    var starType = DataConstants.STAR_MODE_ALL
    var mStudioId: Long? = null
    private var mFullList: List<StarWrap> = listOf()
    private var mList: MutableList<StarWrap> = mutableListOf()
    val expandMap = mutableMapOf<Long, Boolean>()
    private var mKeyword: String? = null
    private val indexEmitter = StarIndexEmitter()
    private val repository = StarRepository()

    // 防止重复loading
    var isLoading = false
    var indexObserver = MutableLiveData<String>()
    var indexBarObserver = MutableLiveData<Boolean>()
    var circleListObserver = MutableLiveData<List<StarWrap>>()
    var richListObserver = MutableLiveData<MutableList<StarWrap>>()
    var circleUpdateObserver = MutableLiveData<Boolean>()
    var richUpdateObserver = MutableLiveData<Boolean>()
    var indexBarVisibility = ObservableInt()

    init {
        currentViewMode = if (ScreenUtils.isTablet()) {
            SettingProperty.setStarListViewMode(PreferenceValue.STAR_LIST_VIEW_CIRCLE)
            PreferenceValue.STAR_LIST_VIEW_CIRCLE
        } else {
            SettingProperty.setStarListViewMode(PreferenceValue.STAR_LIST_VIEW_RICH)
            PreferenceValue.STAR_LIST_VIEW_RICH
        }
    }

    fun loadStarList() {
        mList.clear()
        isLoading = true
        currentViewMode = SettingProperty.getStarListViewMode()
        loadingObserver.value = true
        queryStars()
            .flatMap { toViewItems(it) }
            .flatMap {
                mFullList = it
                mList.addAll(mFullList)
                createIndexes() 
            }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<String>(getComposite()) {
                override fun onNext(index: String) {
                    indexObserver.value = index
                }

                override fun onError(e: Throwable?) {
                    isLoading = false
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }

                override fun onComplete() {
                    isLoading = false
                    loadingObserver.value = false
                    indexBarObserver.value = true
                    if (sortType == AppConstants.STAR_SORT_RANDOM) {
                        indexBarVisibility.set(View.GONE)
                    } else {
                        indexBarVisibility.set(View.VISIBLE)
                    }
                    if (currentViewMode == PreferenceValue.STAR_LIST_VIEW_CIRCLE) {
                        circleListObserver.setValue(mList)
                    } else {
                        richListObserver.setValue(mList)
                    }
                }
            })
    }

    private fun queryStars(): Observable<List<StarWrap>> {
        val builder = StarBuilder()
            .setStudioId(mStudioId)
            .setType(starType)
            .setSortType(sortType)
        return repository.queryStarsBy(builder)
    }

    private fun toViewItems(list: List<StarWrap>): Observable<List<StarWrap>> {
        return Observable.create {
            list.forEach { star ->
                star.imagePath = getStarRandomPath(star.bean.name, null)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    private fun createIndexes(): Observable<String> {
        return Observable.create {
            indexEmitter.clear()
            when (sortType) {
                AppConstants.STAR_SORT_RECORDS -> indexEmitter.createRecordsIndex(it, mList)
                AppConstants.STAR_SORT_NAME -> indexEmitter.createNameIndex(it, mList)
                AppConstants.STAR_SORT_RANDOM -> it.onNext("")
                else -> indexEmitter.createRatingIndex(it, mList, sortType)
            }
            it.onComplete()
        }
    }

    fun getLetterPosition(letter: String?): Int {
        return indexEmitter!!.playerIndexMap[letter]!!.start
    }

    fun getDetailIndex(position: Int): String? {
        return mList[position].bean.name
    }

    fun setExpandAll(expandAll: Boolean) {
        expandMap.clear()
        mList.forEach {
            expandMap[it.bean.id!!] = expandAll
        }
    }

    fun sortStarList(sortType: Int) {
        this.sortType = sortType
        loadStarList()
    }

    fun isKeywordChanged(text: String?): Boolean {
        return text != mKeyword
    }

    /**
     * filter by inputted text
     * @param text
     */
    fun filter(text: String) {
        filterObservable(filterByText(text), false)
    }

    private fun filterObservable(observable: Observable<Boolean>, showLoading: Boolean) {
        if (showLoading) {
            loadingObserver.value = true
        }
        observable
            .flatMap { createIndexes() }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<String>(getComposite()) {

                override fun onNext(index: String) {
                    indexObserver.value = index
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    if (showLoading) {
                        loadingObserver.value = false
                    }
                }

                override fun onComplete() {
                    if (showLoading) {
                        loadingObserver.value = false
                    }
                    indexBarObserver.value = true
                    if (sortType == AppConstants.STAR_SORT_RANDOM) {
                        indexBarVisibility.set(View.GONE)
                    } else {
                        indexBarVisibility.set(View.VISIBLE)
                    }
                    if (currentViewMode == PreferenceValue.STAR_LIST_VIEW_CIRCLE) {
                        circleUpdateObserver.setValue(true)
                    } else {
                        richUpdateObserver.setValue(true)
                    }
                }
            })
    }

    private fun filterByText(text: String): Observable<Boolean> {
        return Observable.create{
            mList.clear()
            mKeyword = text
            if (TextUtils.isEmpty(mKeyword)) {
                mList.addAll(mFullList)
            }
            else {
                mList.addAll(mFullList.filter { item -> isMatchForKeyword(item, text) })
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    private fun isMatchForKeyword(starProxy: StarWrap, text: String): Boolean {
        var result = starProxy.bean.name?.toLowerCase()?.contains(text.toLowerCase())
        return result?: false
    }
}