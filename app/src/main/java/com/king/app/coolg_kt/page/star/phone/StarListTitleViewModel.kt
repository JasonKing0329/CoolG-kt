package com.king.app.coolg_kt.page.star.phone

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.entity.Star

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

    fun getStudioName(studioId: Long): String {
        val studio = getDatabase().getFavorDao().getFavorRecordOrderBy(studioId)
        return studio?.name?:"Unknown Studio"
    }

}