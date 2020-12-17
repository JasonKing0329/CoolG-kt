package com.king.app.coolg_kt.model.setting

import com.google.gson.Gson
import com.king.app.coolg_kt.model.bean.PlayList
import com.king.app.coolg_kt.page.record.popup.RecommendBean

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 16:07
 */
class SettingProperty: BaseProperty() {

    companion object {

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

    }

}