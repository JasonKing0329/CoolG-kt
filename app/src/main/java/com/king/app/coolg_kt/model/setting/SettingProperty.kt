package com.king.app.coolg_kt.model.setting

import com.google.gson.Gson
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.model.bean.HistoryRelation
import com.king.app.coolg_kt.model.bean.PlayList
import com.king.app.coolg_kt.page.match.HomeUrls
import com.king.app.coolg_kt.page.match.RankFilterRange
import com.king.app.coolg_kt.page.match.draw.DrawStrategy
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.page.star.random.RandomData
import com.king.app.coolg_kt.page.tv.TvPlayTimes
import com.king.app.coolg_kt.page.tv.TvServers

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 16:07
 */
class SettingProperty: BaseProperty() {

    companion object {

        fun isEnablePassword(): Boolean = getBoolean("pref_user_check")

        fun isEnableFingerPrint(): Boolean = getBoolean("pref_safety_fingerprint")

        fun getDemoImageVersion(): String {
            return getString("pref_demo_image_version")
        }

        fun setDemoImageVersion(version: String) {
            setString("pref_demo_image_version", version)
        }

        /**
         * shaprePreference文件版本(com.jing.app.jjgallery_preferences.xml)
         */
        fun getPrefVersion(): String {
            return getString("pref_version")
        }

        fun isNoImageMode(): Boolean {
            return getBoolean("pref_gdb_no_image")
        }

        fun isDemoImageMode(): Boolean {
            return getBoolean("pref_demo_image")
        }

        fun setServerUrl(url: String) {
            return setString("pref_http_server", url)
        }

        fun getServerUrl(): String {
            return getString("pref_http_server")
        }

        fun getUploadVersion(): String {
            return getString("upload_version")
        }

        fun setUploadVersion(version: String) {
            setString("upload_version", version!!)
        }

        fun getTagSortType(): Int {
            return getInt("pref_tag_sort")
        }

        fun setTagSortType(type: Int) {
            setInt("pref_tag_sort", type)
        }

        fun isRecordSortDesc(): Boolean {
            return getBoolean("pref_gdb_record_order_desc")
        }

        fun setRecordSortDesc(desc: Boolean) {
            setBoolean("pref_gdb_record_order_desc", desc)
        }

        fun getRecordSortType(): Int {
            return getInt("pref_gdb_record_order")
        }

        fun setRecordSortType(type: Int) {
            setInt("pref_gdb_record_order", type)
        }

        fun getStarRecordsSortType(): Int {
            return getInt("pref_gdb_star_order")
        }

        fun setStarRecordsSortType(random: Int) {
            setInt("pref_gdb_star_order", random)
        }

        fun isStarRecordsSortDesc(): Boolean {
            return getBoolean("pref_gdb_star_order_desc")
        }

        fun setStarRecordsSortDesc(random: Boolean) {
            setBoolean("pref_gdb_star_order_desc", random)
        }

        fun getStudioListType(): Int {
            return getInt("studio_list_type")
        }

        fun setStudioListType(type: Int) {
            setInt("studio_list_type", type)
        }

        fun getStudioListSortType(): Int {
            return getInt("studio_list_sort_type")
        }

        fun setStudioListSortType(type: Int) {
            setInt("studio_list_sort_type", type)
        }

        fun getVideoPlayOrderViewType(): Int {
            return getInt("pref_video_play_order_view_type", AppConstants.VIEW_TYPE_GRID)
        }

        fun setVideoPlayOrderViewType(type: Int) {
            setInt("pref_video_play_order_view_type", type)
        }

        fun getStarListViewMode(): Int {
            return getInt("pref_star_list_view_mode")
        }

        fun setStarListViewMode(random: Int) {
            setInt("pref_star_list_view_mode", random)
        }

        fun getVideoServerSortType(): Int {
            return getInt("pref_video_server_sort", 0)
        }

        fun setVideoServerSortType(type: Int) {
            setInt("pref_video_server_sort", type)
        }

        fun getVideoStarOrderViewType(): Int {
            return getInt("pref_video_star_order_view_type")
        }

        fun setVideoStarOrderViewType(type: Int) {
            setInt("pref_video_star_order_view_type", type)
        }

        fun getRecordListTagType(): Int {
            return getInt("record_list_tag_type")
        }

        fun setRecordListTagType(type: Int) {
            setInt("record_list_tag_type", type)
        }

        fun getForwardUnit(): Int {
            return getInt("video_forward_unit", 0)
        }

        fun setForwardUnit(type: Int) {
            setInt("video_forward_unit", type)
        }

        fun isRememberTvPlayTime(): Boolean {
            return getBoolean("remember_tv_play_time")
        }

        fun setRememberTvPlayTime(type: Boolean) {
            setBoolean("remember_tv_play_time", type)
        }

        fun isAutoPlayNextTv(): Boolean {
            return getBoolean("tv_auto_play_next")
        }

        fun setAutoPlayNextTv(type: Boolean) {
            setBoolean("tv_auto_play_next", type)
        }

        fun getSocketServerUrl(): String {
            return getString("socket_url")
        }

        fun setSocketServerUrl(version: String) {
            setString("socket_url", version)
        }

        fun setPlayList(bean: PlayList?) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: Exception) {
            }
            setString("pref_play_list", sql)
        }

        fun getPlayList(): PlayList {
            val json = getString("pref_play_list")
            var bean: PlayList? = null
            try {
                bean = Gson().fromJson(json, PlayList::class.java)
            } catch (e: Exception) {
            }
            if (bean == null) {
                bean = PlayList()
                bean.list = mutableListOf()
            }
            return bean
        }

        fun getVideoRecBean(): RecommendBean {
            val sql = getString("pref_video_rec_sql")
            try {
                return Gson().fromJson(sql, RecommendBean::class.java)
            } catch (e: java.lang.Exception) {
            }
            return RecommendBean()
        }

        fun setVideoRecBean(bean: RecommendBean) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: java.lang.Exception) {
            }
            setString("pref_video_rec_sql", sql)
        }

        fun getStarRandomData(): RandomData {
            val sql = getString("pref_star_random_data")
            var bean: RandomData? = null
            try {
                bean = Gson().fromJson(sql, RandomData::class.java)
            } catch (e: java.lang.Exception) {
            }
            if (bean == null) {
                bean = RandomData()
            }
            return bean
        }

        fun setStarRandomData(bean: RandomData) {
            try {
                var sql = Gson().toJson(bean)
                setString("pref_star_random_data", sql)
            } catch (e: Exception) {
            }
        }

        fun getMatchHomeUrls(): HomeUrls {
            val sql = getString("match_home_urls")
            try {
                return Gson().fromJson(sql, HomeUrls::class.java)
            } catch (e: java.lang.Exception) {
            }
            return HomeUrls()
        }

        fun setMatchHomeUrls(bean: HomeUrls) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: java.lang.Exception) {
            }
            setString("match_home_urls", sql)
        }

        fun getTvServers(): TvServers {
            val sql = getString("tv_servers")
            try {
                return Gson().fromJson(sql, TvServers::class.java)
            } catch (e: java.lang.Exception) {
            }
            return TvServers()
        }

        fun setTvServers(bean: TvServers) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: java.lang.Exception) {
            }
            setString("tv_servers", sql)
        }

        fun getTvRemembers(): TvPlayTimes {
            val sql = getString("tv_remembers")
            try {
                return Gson().fromJson(sql, TvPlayTimes::class.java)
            } catch (e: java.lang.Exception) {
            }
            return TvPlayTimes()
        }

        fun setTvRemembers(bean: TvPlayTimes) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: java.lang.Exception) {
            }
            setString("tv_remembers", sql)
        }

        fun getDrawStrategy(): DrawStrategy {
            val sql = getString("draw_strategy")
            try {
                return Gson().fromJson(sql, DrawStrategy::class.java)
            } catch (e: java.lang.Exception) {
            }
            return DrawStrategy()
        }

        fun setDrawStrategy(bean: DrawStrategy) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: java.lang.Exception) {
            }
            setString("draw_strategy", sql)
        }

        fun getRankFilterRange(): RankFilterRange {
            val sql = getString("rank_filter_range")
            try {
                return Gson().fromJson(sql, RankFilterRange::class.java)
            } catch (e: java.lang.Exception) {
            }
            return RankFilterRange(0, 0)
        }

        fun setRankFilterRange(bean: RankFilterRange) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: java.lang.Exception) {
            }
            setString("rank_filter_range", sql)
        }

        fun getHistoryRelations(): HistoryRelation {
            val sql = getString("history_relation")
            try {
                return Gson().fromJson(sql, HistoryRelation::class.java)
            } catch (e: java.lang.Exception) {
            }
            return HistoryRelation(listOf())
        }

        fun setHistoryRelations(bean: HistoryRelation) {
            var sql: String? = null
            try {
                sql = Gson().toJson(bean)
            } catch (e: java.lang.Exception) {
            }
            setString("history_relation", sql)
        }
    }

}