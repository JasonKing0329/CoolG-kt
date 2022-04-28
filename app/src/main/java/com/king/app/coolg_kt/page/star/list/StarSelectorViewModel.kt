package com.king.app.coolg_kt.page.star.list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.SelectStar
import com.king.app.coolg_kt.model.bean.StarBuilder
import com.king.app.coolg_kt.model.bean.StudioStarWrap
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.module.StarIndexEmitter
import com.king.app.coolg_kt.model.module.StarIndexProvider
import com.king.app.coolg_kt.model.repository.OrderRepository
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.page.match.TimeWasteRange
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.relation.StarWrap
import com.king.app.gdb.data.relation.StudioStarCountWrap
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Job
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 16:38
 */
class StarSelectorViewModel(application: Application): BaseViewModel(application) {

    var starsObserver: MutableLiveData<List<SelectStar>> = MutableLiveData()

    var studiosObserver: MutableLiveData<List<StudioStarWrap>> = MutableLiveData()

    var indexObserver: MutableLiveData<List<String>> = MutableLiveData()

    var imageChanged: MutableLiveData<TimeWasteRange> = MutableLiveData()

    private val indexProvider = StarIndexProvider()

    var checkMap = mutableMapOf<Long, Boolean>()

    private var mSelectedStar: SelectStar? = null

    var bSingleSelect = false

    var mLimitMax = 0

    private val repository = StarRepository()
    private val orderRepository = OrderRepository()

    private var loadJob: Job? = null

    var mStudioId: Long? = null

    var mKeyword: String? = null

    private var indexList = mutableListOf<String>()

    var sortMode: Int = AppConstants.STAR_SORT_NAME
    set(value) {
        field = value
        loadStar()
    }

    fun loadStar() {
        loadJob?.cancel()
        loadJob = basicAndTimeWaste(
            blockBasic = {
                // load stars
                var builder = StarBuilder().apply {
                    studioId = mStudioId
                    like = mKeyword
                    sortType = sortMode
                }
                val stars = repository.queryStarWith(builder)
                // create index
                indexList.clear()
                indexProvider.clear()
                when (sortMode) {
                    AppConstants.STAR_SORT_RECORDS -> indexProvider.createRecordsIndex(indexList, stars)
                    AppConstants.STAR_SORT_NAME -> indexProvider.createNameIndex(indexList, stars)
                    AppConstants.STAR_SORT_RATING -> indexProvider.createRatingIndex(indexList, stars, sortMode)
                }
                toViewItems(stars)
            },
            onCompleteBasic = {
                starsObserver.value = it
                indexObserver.value = indexList
            },
            blockWaste = {_, item -> handleWaste(item) },
            onWasteRangeChanged = {start, count -> imageChanged.value = TimeWasteRange(start, count) },
            wasteNotifyCount = 20,
            withBasicLoading = true
        )
    }

    fun loadStudios() {
        val countAll = repository.countStarWith(StarBuilder())
        val studios = orderRepository.getStudioWithStarCount().sortedByDescending { it.bean.number }.toMutableList()
        val all = FavorRecordOrder(null, "All", null, 0, 0, null, null)
        studios.add(0, StudioStarCountWrap(all, countAll))
        studiosObserver.value = studios.map { StudioStarWrap(it.bean, it.count, "${it.bean.name}") }
    }

    private fun handleWaste(item: SelectStar) {
        item.star?.imagePath = ImageProvider.getStarRandomPath(item.star?.bean?.name, null)
    }

    private fun toViewItems(list: List<StarWrap>): List<SelectStar> {
        var result = mutableListOf<SelectStar>()
        list.forEach { star ->
            var bean = SelectStar()
            bean.star = star
            bean.observer = selectObserver
            bean.isChecked = isChecked(bean)
            result.add(bean)
        }
        return result
    }

    private var selectObserver = object : SelectObserver<SelectStar> {
        override fun onSelect(data: SelectStar) {
            onSelectStar(data)
        }
    }

    private fun isChecked(data: SelectStar): Boolean {
        data.star?.bean?.id?.let {
            return checkMap[it] == true
        }
        return false
    }

    private fun checkStar(data: SelectStar, check: Boolean) {
        if (check) {
            data.star?.bean?.id?.apply {
                checkMap[this] = true
            }
        }
        else {
            checkMap.remove(data.star?.bean?.id)
        }
    }

    private fun onSelectStar(data: SelectStar) {
        when {
            bSingleSelect -> {
                mSelectedStar?.let {
                    checkStar(it, false)
                    it.isChecked = false
                }
                checkStar(data, true)
                data.isChecked = true
                mSelectedStar = data
            }
            mLimitMax > 0 -> {
                var count = starsObserver.value?.filter { isChecked(it) }?.size?:0
                val targetCheck: Boolean = !isChecked(data)
                if (targetCheck && count >= mLimitMax) {
                    messageObserver.value = "You can select at most $mLimitMax"
                    return
                }
                checkStar(data, targetCheck)
                data.isChecked = targetCheck
            }
            else -> {
                val result = !isChecked(data)
                checkStar(data, result)
                data.isChecked = result
            }
        }
    }

    fun getSelectedItems(): ArrayList<CharSequence> {
        var result = arrayListOf<CharSequence>()
        return checkMap.keys.mapTo(result) { it.toString() }
    }

    fun getLetterPosition(letter: String?): Int {
        kotlin.runCatching {
            return indexProvider.playerIndexMap[letter]?.start?:0
        }.let {
            return 0
        }
    }

    fun changeStudio(id: Long?) {
        if (mStudioId != id) {
            mStudioId = id
            loadStar()
        }
    }

    fun onKeywordChanged(key: String) {
        if (key != mKeyword) {
            mKeyword = key
            loadStar()
        }
    }
}