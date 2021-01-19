package com.king.app.coolg_kt.page.match

import android.app.Application
import androidx.databinding.ObservableField
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.setting.SettingProperty

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/19 9:40
 */
class MatchHomeViewModel(application: Application): BaseViewModel(application) {

    var matchUrl = ObservableField<String>()
    var seasonUrl = ObservableField<String>()
    var rankUrl = ObservableField<String>()
    var h2hUrl = ObservableField<String>()
    var finalUrl = ObservableField<String>()

    init {
        if (SettingProperty.isDemoImageMode()) {
            matchUrl.set(ImageProvider.getRandomDemoImage(-1, null))
            seasonUrl.set(ImageProvider.getRandomDemoImage(-1, null))
            rankUrl.set(ImageProvider.getRandomDemoImage(-1, null))
            h2hUrl.set(ImageProvider.getRandomDemoImage(-1, null))
            finalUrl.set(ImageProvider.getRandomDemoImage(-1, null))
        }
        else {
            val urls = SettingProperty.getMatchHomeUrls()
            matchUrl.set(urls.matchUrl)
            seasonUrl.set(urls.seasonUrl)
            rankUrl.set(urls.rankUrl)
            h2hUrl.set(urls.h2hUrl)
            finalUrl.set(urls.finalUrl)
        }
    }

    fun updateMatchUrl(coverPath: String) {
        matchUrl.set(coverPath)
        val urls = SettingProperty.getMatchHomeUrls()
        urls.matchUrl = coverPath
        SettingProperty.setMatchHomeUrls(urls)
    }

    fun updateSeasonUrl(coverPath: String) {
        seasonUrl.set(coverPath)
        val urls = SettingProperty.getMatchHomeUrls()
        urls.seasonUrl = coverPath
        SettingProperty.setMatchHomeUrls(urls)
    }

    fun updateRankUrl(coverPath: String) {
        rankUrl.set(coverPath)
        val urls = SettingProperty.getMatchHomeUrls()
        urls.rankUrl = coverPath
        SettingProperty.setMatchHomeUrls(urls)
    }

    fun updateH2hUrl(coverPath: String) {
        h2hUrl.set(coverPath)
        val urls = SettingProperty.getMatchHomeUrls()
        urls.h2hUrl = coverPath
        SettingProperty.setMatchHomeUrls(urls)
    }

    fun updateFinalUrl(coverPath: String) {
        finalUrl.set(coverPath)
        val urls = SettingProperty.getMatchHomeUrls()
        urls.finalUrl = coverPath
        SettingProperty.setMatchHomeUrls(urls)
    }
}