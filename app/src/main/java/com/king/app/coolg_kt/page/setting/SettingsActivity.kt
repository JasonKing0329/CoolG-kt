package com.king.app.coolg_kt.page.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.RootActivity
import com.king.app.coolg_kt.model.http.AppHttpClient

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
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(object : SharedPreferences.OnSharedPreferenceChangeListener {
                override fun onSharedPreferenceChanged(
                    sharedPreferences: SharedPreferences?,
                    key: String?
                ) {
                    if ("pref_http_server" == key) {
                        AppHttpClient.getInstance().createRetrofit()
                    }
                }

            })
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            when(preference?.key) {
                "pref_http_update" -> Toast.makeText(context, "Check Update", Toast.LENGTH_SHORT).show()
            }
            return super.onPreferenceTreeClick(preference)
        }
    }

}