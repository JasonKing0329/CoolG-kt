package com.king.app.coolg_kt.page.match.list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:08
 */
class MatchListViewModel(application: Application): BaseViewModel(application) {

    var matchesObserver = MutableLiveData<List<Match>>()

    fun loadMatches() {
        var list = getDatabase().getMatchDao().getAllMatchesByOrder()
        list.forEach {
            if (SettingProperty.isDemoImageMode()) {
                it.imgUrl = ImageProvider.getRandomDemoImage(-1, null)?:""
            }
        }
        matchesObserver.value = list
    }

    fun deleteMatch(bean: Match) {
        getDatabase().getMatchDao().deleteMatch(bean)
    }
}