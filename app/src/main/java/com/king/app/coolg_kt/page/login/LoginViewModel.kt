package com.king.app.coolg_kt.page.login

import android.app.Application
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.FileUtil
import com.king.app.coolg_kt.utils.MD5Util
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FilenameFilter

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/21 14:05
 */
class LoginViewModel(application: Application) : BaseViewModel(application) {

    var etPwdText: ObservableField<String> = ObservableField()

    var groupLoginVisibility: ObservableInt = ObservableInt(View.INVISIBLE)

    var fingerprintObserver = MutableLiveData<Boolean>()

    var passwordCheck = MutableLiveData<Boolean>()

    var loginObserver = MutableLiveData<Boolean>()

    var extendObserver = MutableLiveData<Boolean>()

    private var mPwd: String? = null

    val pwdTextWatcher: TextWatcher
        get() = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mPwd = s.toString()
            }

            override fun afterTextChanged(s: Editable) {

            }
        }

    open fun onClickLogin(view: View) {
        checkPassword(mPwd)
    }

    fun initCreate() {
        prepare()
    }

    private fun prepare() {
        showLoading(true)
        prepareData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<Boolean> {
                override fun onSubscribe(d: Disposable) {
                    addDisposable(d)
                }

                override fun onNext(hasExtendPref: Boolean) {
                    showLoading(false)

                    if (SettingProperty.isEnableFingerPrint()) {
                        fingerprintObserver.setValue(true)
                    } else {
                        passwordCheck.setValue(true)
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    showLoading(false)
                }

                override fun onComplete() {

                }
            })
    }

    private fun prepareData(): Observable<Boolean> {
        return Observable.create { e ->
            // 创建base目录

            // 创建base目录
            AppConfig.DIRS.forEach {
                val file = File(it)
                if (!file.exists()) {
                    file.mkdir()
                }
            }

            // copy demo images
            if (AppConfig.DEMO_IMAGE_VERSION != SettingProperty.getDemoImageVersion()) {
                SettingProperty.setDemoImageVersion(AppConfig.DEMO_IMAGE_VERSION)
                FileUtil.deleteFilesUnderFolder(File(AppConfig.GDB_IMG_DEMO))
                FileUtil.copyAssets("img", AppConfig.GDB_IMG_DEMO)
            }

            CoolApplication.instance.createDatabase()

            // 检查扩展配置
            val hasPref = checkExtendConf()

            e.onNext(hasPref)
        }
    }

    /**
     * 检查配置目录是否存在默认配置文件
     * @return
     */
    private fun checkExtendConf(): Boolean {
        val files = File(AppConfig.APP_DIR_CONF_PREF_DEF).listFiles(FilenameFilter { dir, name -> name.startsWith(AppConfig.PREF_NAME) })
        files.forEach {
            kotlin.runCatching {
                val arr = it.name.split("__".toRegex()).toTypedArray()
                val version = arr[1].split("\\.".toRegex()).toTypedArray()[0]
                val curVersion: String = SettingProperty.getPrefVersion()
                DebugLog.e("checkExtendConf version:$version curVersion:$curVersion")
                if (version != curVersion) {
                    AppConfig.DISK_PREF_DEFAULT_PATH = it.path
                    return true
                }
            }
        }
        return false
    }

    private fun checkPassword(pwd: String?) {
        if ("38D08341D686315F" == MD5Util.get16MD5Capital(pwd)) {
            loginObserver.setValue(true)
        } else {
            loginObserver.value = false
            messageObserver.setValue("密码错误")
        }
    }

}
