package com.king.app.coolg_kt.page.tv

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 22:50
 */
class TvViewModel(application: Application): BaseViewModel(application) {

    var isSuperUser = false

    var goToServer = MutableLiveData<Boolean>()

    fun checkUserCode(code: String) {
        isSuperUser = code == "1010520"
    }
}