package com.king.app.coolg_kt.page.login

import android.Manifest
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.lifecycle.Observer
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityLoginBinding
import com.king.app.coolg_kt.model.fingerprint.FingerprintHelper
import com.king.app.coolg_kt.model.fingerprint.OnFingerResultListener
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.page.setting.ManageActivity
import com.king.app.coolg_kt.page.setting.SettingsActivity
import com.king.app.coolg_kt.utils.AppUtil
import com.tbruyelle.rxpermissions3.RxPermissions

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    override fun getContentView(): Int = R.layout.activity_login

    override fun createViewModel(): LoginViewModel = generateViewModel(LoginViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.btnSetting.setOnClickListener { startActivity(Intent().setClass(this, SettingsActivity::class.java)) }
        mModel.loginObserver.observe(this, Observer { success -> superUser() })
        mModel.fingerprintObserver.observe(this, Observer { check -> checkFingerprint() })
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
                initCreate()
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
                if (result) {
                    superUser()
                }
                else {
                    initCreate()
                }
            }

            override fun onCancel() {
                finish()
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
        mBinding.groupLogin.visibility = View.GONE
        mBinding.groupPass.visibility = View.VISIBLE
        mBinding.groupPass.startAnimation(appearNextStep())
        mBinding.tvHome.setOnClickListener { v ->
            goToHome()
            finish()
        }
        mBinding.tvSetting.setOnClickListener { v -> goToSetting() }
        mBinding.tvManage.setOnClickListener { v -> goToManage() }

    }

    private fun goToHome() {
        startActivity(Intent().setClass(this, PhoneRecordListActivity::class.java))
    }

    private fun goToManage() {
        startActivity(Intent().setClass(this, ManageActivity::class.java))
    }

    private fun goToSetting() {
        startActivity(Intent().setClass(this, SettingsActivity::class.java))
    }

    private fun appearNextStep(): Animation? {
        val set = AnimationSet(true)
        set.duration = 500
        val alpha = AlphaAnimation(0f, 1f)
        set.addAnimation(alpha)
        val scale = ScaleAnimation(0f, 1f, 0f, 1f, 0.5f, 0.5f)
        set.addAnimation(scale)
        return set
    }

}
