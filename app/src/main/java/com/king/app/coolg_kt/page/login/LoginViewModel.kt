package com.king.app.coolg_kt.page.login

import android.app.Application
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.utils.MD5Util
import com.king.app.jgallery.model.setting.SettingProperty
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

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
                        groupLoginVisibility.set(View.VISIBLE)
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
//
//            // 每次进入导出一次数据库
//            DataExporter.execute()
//
//            // 创建base目录
//            for (path in AppConfig.appDirectories) {
//                val file = File(path)
//                if (!file.exists()) {
//                    file.mkdir()
//                }
//            }

            e.onNext(true)
        }
    }

    fun checkPassword(pwd: String?) {
        if ("38D08341D686315F" == MD5Util.get16MD5Capital(pwd)) {
            loginObserver.setValue(true)
        } else {
            loginObserver.value = false
            messageObserver.setValue("密码错误")
        }
    }

}
