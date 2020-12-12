package com.king.app.coolg_kt.model.setting

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

    }

}