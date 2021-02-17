package com.king.app.coolg_kt.page.tv

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.bean.response.GdbRespBean
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.model.udp.ServerBody
import com.king.app.coolg_kt.model.udp.UdpReceiver
import io.reactivex.rxjava3.core.ObservableSource

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 17:46
 */
class ServerViewModel(application: Application): BaseViewModel(application) {

    var serversObserver = MutableLiveData<List<ServerBody>>()

    var serverList = mutableListOf<ServerBody>()

    var udpReceiver = UdpReceiver()

    var connectSuccess = MutableLiveData<Boolean>()

    fun loadServers() {
        serverList = SettingProperty.getTvServers().list.toMutableList()
        // 从本地加载出来的先一律设置为离线
        serverList.forEach { it.isOnline = false}
        serversObserver.value = serverList
    }

    fun connectToServer(serverBody: ServerBody) {

        loadingObserver.value = true;

        val fullUrl = "${serverBody.ip}:${serverBody.port}/${serverBody.extraUrl}"
        SettingProperty.setServerUrl(fullUrl)

        AppHttpClient.getInstance().getAppService().isServerOnline()
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<GdbRespBean>(getComposite()) {
                override fun onNext(bean: GdbRespBean) {
                    loadingObserver.value = false;
                    if (bean.isOnline) {
                        messageObserver.value = "Connect success"
                        connectSuccess.value = true
                    }
                    else {
                        messageObserver.value = "Server is not online"
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    loadingObserver.value = false
                    messageObserver.value = e?.message
                }
            })
    }

    fun onReceiveIp() {
        udpReceiver.observeServer()
            .flatMap { distinctServer(it) }
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<ServerBody>(getComposite()){
                override fun onNext(t: ServerBody) {
                    serversObserver.value = serverList
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun distinctServer(serverBody: ServerBody): ObservableSource<ServerBody> {
        return ObservableSource {
            serverBody.isOnline = true
            val server = serverList.firstOrNull { server -> server.serverName == serverBody.serverName }
            if (server == null) {
                serverList.add(serverBody)
            }
            else {
                // server ip发生变化，更新
                if (server.ip != serverBody.ip) {
                    server.isOnline = true
                    server.ip = serverBody.ip
                    server.port = serverBody.port
                    server.extraUrl = serverBody.extraUrl
                }
                else {
                    // 已上线，不需要更新
                    if (server.isOnline) {
                        throw Throwable("server is existed")
                    }
                    // 未上线，更新为已上线
                    else {
                        server.isOnline = true
                    }
                }
            }
            SettingProperty.setTvServers(TvServers(serverList))
            it.onNext(serverBody)
            it.onComplete()
        }
    }

    override fun onDestroy() {
        udpReceiver.destroy()
        super.onDestroy()
    }

}