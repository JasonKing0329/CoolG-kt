package com.king.app.coolg_kt.page.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.lifecycle.Observer
import com.king.app.coolg_kt.BuildConfig
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityLoginBinding
import com.king.app.coolg_kt.model.fingerprint.FingerprintHelper
import com.king.app.coolg_kt.model.fingerprint.OnFingerResultListener
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.home.phone.PhoneHomeActivity
import com.king.app.coolg_kt.page.match.MatchHomeActivity
import com.king.app.coolg_kt.page.setting.ManageActivity
import com.king.app.coolg_kt.page.setting.SettingsActivity
import com.king.app.coolg_kt.page.tv.TvActivity
import com.king.app.coolg_kt.utils.AppUtil
import com.tbruyelle.rxpermissions3.RxPermissions

class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>() {

    companion object {
        val EXTRA_SUPER_USER = "super_user"

        fun startAsSuperUser(context: Context) {
            var intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(EXTRA_SUPER_USER, true)
            context.startActivity(intent)
        }
    }

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

    private fun isStartSuperUser(): Boolean {
        return intent.getBooleanExtra(EXTRA_SUPER_USER, false)
    }

    private fun initCreate() {
        mModel.passwordCheck.observe(this, Observer { showPasswordCheck() })
        mModel.initCreate(isStartSuperUser())
    }

    private fun checkFingerprint() {
        var helper = FingerprintHelper()
        helper.onFingerResultListener = object : OnFingerResultListener {
            override fun fingerResult(result: Boolean) {
                if (result) {
                    superUser()
                }
            }

            override fun onCancel() {
                finish()
            }

            override fun retry() {
                initCreate()
            }

            override fun unSupport() {
                showMessageShort("指纹硬件无法使用")
                showPasswordCheck()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            helper.startBiometricPromptIn28(this)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            helper.startBiometricPromptIn23(supportFragmentManager)
        }
        else {
            showPasswordCheck()
        }
    }

    private fun showPasswordCheck() {
        if (SettingProperty.isEnablePassword()) {
            mBinding.groupLogin.visibility = View.VISIBLE
        }
        else {
            superUser()
        }
    }

    private fun superUser() {
        if ("tv" == BuildConfig.DEVICE_TYPE) {
            goToTv()
            finish()
        }
        else {
            mBinding.groupLogin.visibility = View.GONE
            mBinding.groupPass.visibility = View.VISIBLE
            mBinding.groupPass.startAnimation(appearNextStep())
            mBinding.tvHome.setOnClickListener { v ->
                goToHome()
                finish()
            }
            mBinding.tvSetting.setOnClickListener { v -> goToSetting() }
            mBinding.tvManage.setOnClickListener { v -> goToManage() }
            mBinding.tvTv.setOnClickListener { v -> goToTv() }
        }
    }

    private fun goToHome() {
        startActivity(Intent().setClass(this, PhoneHomeActivity::class.java))
    }

    private fun goToManage() {
        startActivity(Intent().setClass(this, ManageActivity::class.java))
    }

    private fun goToSetting() {
        startActivity(Intent().setClass(this, SettingsActivity::class.java))
    }

    private fun goToTv() {
        startActivity(Intent().setClass(this, TvActivity::class.java))
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
