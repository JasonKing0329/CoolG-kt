package com.king.app.coolg_kt.model.setting

import com.google.gson.Gson
import com.king.app.coolg_kt.utils.BannerHelper

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/16 10:07
 */
class ViewProperty: BaseProperty() {

    companion object {

        fun getHomeBannerParams(): BannerHelper.BannerParams {
            val json = getString("banner_params_home")
            val gson = Gson()
            var bean: BannerHelper.BannerParams? = null
            try {
                bean = gson.fromJson(json, BannerHelper.BannerParams::class.java)
            } catch (e: Exception) {
            }
            if (bean == null) {
                bean = BannerHelper.BannerParams()
            }
            return bean
        }

        fun setHomeBannerParams(bean: BannerHelper.BannerParams) {
            setString("banner_params_home", Gson().toJson(bean))
        }

        fun getVideoHomeBannerParams(): BannerHelper.BannerParams {
            val json = getString("banner_params_video_home")
            val gson = Gson()
            var bean: BannerHelper.BannerParams? = null
            try {
                bean = gson.fromJson(json, BannerHelper.BannerParams::class.java)
            } catch (e: Exception) {
            }
            if (bean == null) {
                bean = BannerHelper.BannerParams()
            }
            return bean
        }

        fun setVideoHomeBannerParams(bean: BannerHelper.BannerParams) {
            setString("banner_params_video_home", Gson().toJson(bean))
        }

        fun getRecordBannerParams(): BannerHelper.BannerParams {
            val json = getString("banner_params_record")
            val gson = Gson()
            var bean: BannerHelper.BannerParams? = null
            try {
                bean = gson.fromJson(json, BannerHelper.BannerParams::class.java)
            } catch (e: Exception) {
            }
            if (bean == null) {
                bean = BannerHelper.BannerParams()
            }
            return bean
        }

        fun setRecordBannerParams(bean: BannerHelper.BannerParams) {
            setString("banner_params_record", Gson().toJson(bean))
        }

        fun getStarBannerParams(): BannerHelper.BannerParams {
            val json = getString("banner_params_star")
            val gson = Gson()
            var bean: BannerHelper.BannerParams? = null
            try {
                bean = gson.fromJson(json, BannerHelper.BannerParams::class.java)
            } catch (e: Exception) {
            }
            if (bean == null) {
                bean = BannerHelper.BannerParams()
            }
            return bean
        }

        fun setStarBannerParams(bean: BannerHelper.BannerParams) {
            setString("banner_params_star", Gson().toJson(bean))
        }

    }

}