package com.king.app.coolg_kt.page.tv

import android.app.Application
import android.text.TextUtils
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
        serverList.removeAll { TextUtils.isEmpty(it.serverName) }
        // 从本地加载出来的先一律设置为离线
        serverList.forEach { it.isOnline = false}
        serverList.add(0, manuelServer())
        serversObserver.value = serverList
    }

    private fun manuelServer(): ServerBody {
        val server = ServerBody()
        server.serverName = "Manuel"
        server.isOnline = false
        server.ip = SettingProperty.getServerUrl()
        server.isManuel = true
        return server
    }

    fun connectToServer(serverBody: ServerBody) {

        loadingObserver.value = true;

        val fullUrl = if (serverBody.isManuel) {
            serverBody.ip
        }
        else {
            "${serverBody.ip}:${serverBody.port}/${serverBody.extraUrl}"
        }
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
            .subscribe(object : SimpleObserver<Boolean>(getComposite()){
                override fun onNext(notify: Boolean) {
                    if (notify) {
                        serversObserver.value = serverList
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }
            })
    }

    private fun distinctServer(serverBody: ServerBody): ObservableSource<Boolean> {
        return ObservableSource {
            if (!TextUtils.isEmpty(serverBody.serverName)) {
                serverBody.isOnline = true
                val server = serverList.firstOrNull { server -> server.serverName == serverBody.serverName }
                var notify = false
                if (server == null) {
                    serverList.add(serverBody)
                    SettingProperty.setTvServers(TvServers(serverList))
                    notify = true
                }
                else {
                    // server ip发生变化，更新
                    if (server.ip != serverBody.ip) {
                        server.ip = serverBody.ip
                        server.port = serverBody.port
                        server.extraUrl = serverBody.extraUrl
                        SettingProperty.setTvServers(TvServers(serverList))
                        server.isOnline = true
                        notify = true
                    }
                    else {
                        // 未上线，更新为已上线
                        if (!server.isOnline) {
                            notify = true
                            server.isOnline = true
                        }
                    }
                }
                if (notify) {
                    it.onNext(true)
                    it.onComplete()
                }
            }
        }
    }

    // 本机模拟器测试用（udp端口问题，模拟器无法接受服务器广播）
//    init {
//        var server = TvServers()
//        var body = ServerBody()
//        body.serverName = "PC-Aiden"
//        body.ip = "192.168.2.151"
//        body.port = 8080
//        body.extraUrl = "JJGalleryServer"
//        body.identity = UdpReceiver.UDP_SERVER_IDENTITY
//        var list = server.list.toMutableList()
//        list.add(body)
//        server.list = list
//        SettingProperty.setTvServers(server)
//    }

    override fun onDestroy() {
        udpReceiver.destroy()
        super.onDestroy()
    }

    fun updateServerIp(name: String) {
        SettingProperty.setServerUrl(name)
        loadServers()
    }

    fun getManuelServer(): String? {
        return SettingProperty.getServerUrl()
    }

}