package com.king.app.coolg_kt.page.star.list

import android.app.Application
import android.view.View
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.bean.StarBuilder
import com.king.app.coolg_kt.model.image.ImageProvider.getStarRandomPath
import com.king.app.coolg_kt.model.module.StarIndexProvider
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.relation.StarWrap
import kotlinx.coroutines.Job

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 14:52
 */
class StarListViewModel(application: Application) : BaseViewModel(application) {
    private var currentViewMode = 0

    // see AppConstants.STAR_SORT_XXX
    var mSortMode: Int = AppConstants.STAR_SORT_NAME
        set(value) {
            field = value
            loadStarList()
        }

    // see GDBProperties.STAR_MODE_XXX
    var mStarType = DataConstants.STAR_MODE_ALL
    var mStudioId: Long? = null
    private var mList: List<StarWrap> = listOf()
    val expandMap = mutableMapOf<Long, Boolean>()
    private var mKeyword: String? = null
    private val indexProvider = StarIndexProvider()
    private val repository = StarRepository()

    private var indexList = mutableListOf<String>()

    private var loadStarJob: Job? = null

    // 防止重复loading
    var isLoading = false

    var circleListObserver = MutableLiveData<List<StarWrap>>()
    var richListObserver = MutableLiveData<List<StarWrap>>()
    var imageChanged = MutableLiveData<TimeWasteRange>()
    var circleUpdateObserver = MutableLiveData<Boolean>()
    var richUpdateObserver = MutableLiveData<Boolean>()
    var indexObserver: MutableLiveData<List<String>> = MutableLiveData()
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
        isLoading = true
        currentViewMode = SettingProperty.getStarListViewMode()

        loadStarJob?.cancel()
        loadStarJob = basicAndTimeWaste(
            blockBasic = {
                // load stars
                var builder = StarBuilder().apply {
                    studioId = mStudioId
                    like = mKeyword
                    sortType = mSortMode
                    type = mStarType
                }
                val stars = repository.queryStarWith(builder)
                // create index
                indexList.clear()
                indexProvider.clear()
                when (mSortMode) {
                    AppConstants.STAR_SORT_RECORDS -> indexProvider.createRecordsIndex(indexList, stars)
                    AppConstants.STAR_SORT_NAME -> indexProvider.createNameIndex(indexList, stars)
                    AppConstants.STAR_SORT_RATING -> indexProvider.createRatingIndex(indexList, stars, mSortMode)
                }
                stars
            },
            onCompleteBasic = {
                mList = it
                if (currentViewMode == PreferenceValue.STAR_LIST_VIEW_CIRCLE) {
                    circleListObserver.setValue(it)
                } else {
                    richListObserver.setValue(it)
                }
                indexObserver.value = indexList
                val visibility = when (mSortMode) {
                    AppConstants.STAR_SORT_RECORDS, AppConstants.STAR_SORT_NAME, AppConstants.STAR_SORT_RATING -> View.VISIBLE
                    else -> View.GONE
                }
                indexBarVisibility.set(visibility)

            },
            blockWaste = { _, it ->  handleStar(it) },
            wasteNotifyCount = 20,
            onWasteRangeChanged = { start, count -> imageChanged.value = TimeWasteRange(start, count) },
            withBasicLoading = true
        )
    }

    private fun handleStar(star: StarWrap) {
        star.imagePath = getStarRandomPath(star.bean.name, null)
    }

    fun getLetterPosition(letter: String?): Int {
        kotlin.runCatching {
            return indexProvider.playerIndexMap[letter]?.start?:0
        }.let {
            return 0
        }
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

    fun onKeywordChanged(key: String) {
        if (key != mKeyword) {
            mKeyword = key
            loadStarList()
        }
    }
}