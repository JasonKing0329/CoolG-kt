package com.king.app.coolg_kt.page.star.phone

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.bean.StarBuilder
import com.king.app.coolg_kt.model.bean.StarTypeWrap
import com.king.app.coolg_kt.model.bean.StudioStarWrap
import com.king.app.coolg_kt.model.repository.OrderRepository
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.relation.StudioStarCountWrap
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 17:04
 */
class StarListTitleViewModel(application: Application) : BaseViewModel(application) {
    var mViewMode: Int = SettingProperty.getStarListViewMode()
    // 如果是random，每次都有效。否则，只有当排序模式变化才重新排序
    var sortMode: Int = AppConstants.STAR_SORT_NAME
        set(sortMode) {
            // 如果是random，每次都有效。否则，只有当排序模式变化才重新排序
            if (sortMode == AppConstants.STAR_SORT_RANDOM || sortMode != field) {
                field = sortMode
                sortTypeObserver.value = field
            }
        }

    var menuViewModeObserver = MutableLiveData<String>()
    var sortTypeObserver = MutableLiveData<Int>()

    var typesObserver: MutableLiveData<List<StarTypeWrap>> = MutableLiveData()

    var studiosObserver: MutableLiveData<List<StudioStarWrap>> = MutableLiveData()
    private val orderRepository = OrderRepository()
    private val starRepository = StarRepository()

    fun loadTags() {
        launchFlow(
            flow { emit(loadTypes()) }
                .map {
                    typesObserver.value = it
                    loadStudios()
                },
            withLoading = false
        ) {
            studiosObserver.value = it
        }
    }

    private fun loadTypes(): List<StarTypeWrap> {
        val result = mutableListOf<StarTypeWrap>()
        StarBuilder().apply {
            type = DataConstants.STAR_MODE_ALL
            var count = starRepository.countStarWith(this)
            result.add(StarTypeWrap(type, count, "All"))

            type = DataConstants.STAR_MODE_TOP
            count = starRepository.countStarWith(this)
            result.add(StarTypeWrap(type, count, "Top"))

            type = DataConstants.STAR_MODE_BOTTOM
            count = starRepository.countStarWith(this)
            result.add(StarTypeWrap(type, count, "Bottom"))

            type = DataConstants.STAR_MODE_HALF
            count = starRepository.countStarWith(this)
            result.add(StarTypeWrap(type, count, "Vers"))
        }
        return result
    }

    fun findStudioPosition(studioId: Long): Int {
        return studiosObserver.value?.indexOfFirst { it.studio.id == studioId }?:-1
    }

    private fun loadStudios(): List<StudioStarWrap> {
        val studios = orderRepository.getStudioWithStarCount().sortedByDescending { it.bean.number }.toMutableList()
        val all = FavorRecordOrder(0, "All", null, 0, 0, null, null)
        studios.add(0, StudioStarCountWrap(all, typesObserver.value?.firstOrNull()?.starCount?:0))
        return studios.map {
            StudioStarWrap(it.bean, it.count, it.bean.name?:"")
        }
    }

    fun toggleViewMode(resources: Resources) {
        val title = if (mViewMode == PreferenceValue.STAR_LIST_VIEW_CIRCLE) {
            mViewMode = PreferenceValue.STAR_LIST_VIEW_RICH
            SettingProperty.setStarListViewMode(PreferenceValue.STAR_LIST_VIEW_RICH)
            resources.getString(R.string.menu_view_mode_circle)
        } else {
            SettingProperty.setStarListViewMode(PreferenceValue.STAR_LIST_VIEW_CIRCLE)
            mViewMode = PreferenceValue.STAR_LIST_VIEW_CIRCLE
            resources.getString(R.string.menu_view_mode_rich)
        }
        menuViewModeObserver.value = title
    }

    fun nextFavorStar(): Star? {
        try {
            return getDatabase().getStarDao().getStarByRating(StarRatingUtil.RATING_VALUE_CP, 1)[0]
        } catch (e: Exception) {
        }
        return null
    }

}