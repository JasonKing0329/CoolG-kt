package com.king.app.coolg_kt.page.setting

import android.app.Application
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.response.AppCheckBean
import com.king.app.coolg_kt.model.http.bean.response.GdbRespBean
import com.king.app.coolg_kt.model.http.observer.SimpleObserver

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 10:57
 */
class ManageViewModel(application: Application): BaseViewModel(application) {

    var dbVersionText: ObservableField<String> = ObservableField()

    var imagesObserver: MutableLiveData<DownloadDialogBean> = MutableLiveData()
    var gdbCheckObserver: MutableLiveData<AppCheckBean> = MutableLiveData()
    var readyToDownloadObserver: MutableLiveData<Long> = MutableLiveData()

    var warningSync: MutableLiveData<Boolean> = MutableLiveData()
    var warningUpload: MutableLiveData<String> = MutableLiveData()

    fun onClickStar(view: View) {

    }

    fun onClickRecord(view: View) {

    }

    fun onCheckServer(view: View) {
        loadingObserver.value = true;
        AppHttpClient.getInstance().getAppService().isServerOnline()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<GdbRespBean>(getComposite()) {
                override fun onNext(bean: GdbRespBean) {
                    loadingObserver.value = false;
                    if (bean.isOnline) {
                        messageObserver.setValue("Connect success");
                    }
                    else {
                        messageObserver.setValue("Server is not online");
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace();
                    loadingObserver.value = false;
                    messageObserver.value = e?.message;
                }
            })
    }

    fun onCheckDb(view: View) {

    }

    fun prepareUpload(view: View) {

    }

    fun checkSyncVersion(view: View) {

    }

    fun onReceiveIp(view: View) {

    }

    fun moveStar() {

    }

    fun moveRecord() {

    }

    fun uploadDatabase() {

    }
}