package com.king.app.coolg_kt.model.http

import android.text.TextUtils
import com.king.app.coolg_kt.model.setting.SettingProperty

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 10:49
 */
object UrlProvider {

    fun formatUrl(ip: String): String {
        var ip = ip
        if (!ip.startsWith("http://")) {
            ip = "http://$ip"
        }
        if (!ip.endsWith("/")) {
            ip = "$ip/"
        }
        return ip
    }

    fun getBaseUrl(): String {
        val ip: String = SettingProperty.getServerUrl()
        return if (TextUtils.isEmpty(ip)) {
            "http://www.baidu.com/"
        } else ip
    }

}