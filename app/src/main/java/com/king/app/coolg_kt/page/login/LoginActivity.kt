package com.king.app.coolg_kt.page.login

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Observer
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityLoginBinding
import com.king.app.coolg_kt.model.fingerprint.FingerprintHelper
import com.king.app.coolg_kt.model.fingerprint.OnFingerResultListener
import com.king.app.coolg_kt.utils.AppUtil
import com.king.app.jgallery.model.setting.SettingProperty
import com.tbruyelle.rxpermissions3.RxPermissions

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override fun getContentView(): Int = R.layout.activity_login

    override fun createViewModel(): LoginViewModel = generateViewModel(LoginViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
//        mBinding.btnSetting.setOnClickListener { startActivity(Intent().setClass(this, SettingsActivity::class.java)) }
        mModel!!.loginObserver.observe(this, Observer { success -> superUser() })
        mModel!!.fingerprintObserver.observe(this, Observer { check -> checkFingerprint() })
    }

    override fun initData() {
        if (AppUtil.isAndroidP()) {
            AppUtil.closeAndroidPDialog()
        }

        RxPermissions(this)
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe({ isGrant ->
                if (SettingProperty.isEnableFingerPrint()) {
                    checkFingerprint()
                }
                else {
                    initCreate()
                }
            }, { throwable ->
                throwable.printStackTrace()
                finish()
            })
    }

    private fun initCreate() {
        mModel.initCreate()
    }

    private fun checkFingerprint() {
        var helper = FingerprintHelper()
        helper.onFingerResultListener = object : OnFingerResultListener {
            override fun fingerResult(result: Boolean) {
                initCreate()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            helper.startBiometricPromptIn28(this)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            helper.startBiometricPromptIn23(supportFragmentManager)
        }
        else {
            initCreate()
        }
    }

    private fun superUser() {
//        startActivity(Intent().setClass(this, HomeActivity::class.java))
//        finish()
    }

}
