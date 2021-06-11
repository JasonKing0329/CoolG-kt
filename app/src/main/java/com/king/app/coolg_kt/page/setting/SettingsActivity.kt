package com.king.app.coolg_kt.page.setting

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.RootActivity
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.socket.PlayVideoRequest
import com.king.app.coolg_kt.page.tv.socket.ServerService
import com.king.app.coolg_kt.page.tv.socket.SocketListener
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.IpUtil

class SettingsActivity : RootActivity() {

    override fun getStatusBarColor(): Int {
        return resources.getColor(R.color.colorPrimary)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        DebugLog.e("wifi ip " + IpUtil.getLocalIp(this))
        DebugLog.e("getHostIP " + IpUtil.getHostIP())
        DebugLog.e("getLocalIpV4Address " + IpUtil.getLocalIpV4Address())
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                if ("pref_http_server" == key) {
                    AppHttpClient.getInstance().createRetrofit()
                }
            }
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            when(preference?.key) {
                "pref_http_update" -> Toast.makeText(context, "Check Update", Toast.LENGTH_SHORT).show()
            }
            return super.onPreferenceTreeClick(preference)
        }
    }

}