package com.king.app.jgallery.model.setting

import com.google.gson.Gson
import com.king.app.coolg_kt.model.setting.BaseProperty
import java.lang.Exception

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 16:07
 */
class SettingProperty: BaseProperty() {

    companion object {

        fun isEnableFingerPrint(): Boolean = getBoolean("pref_safety_fingerprint")

    }

}