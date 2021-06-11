package com.king.app.coolg_kt.model.socket

import com.google.gson.Gson
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.utils.DebugLog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.net.Socket
import java.util.*


/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/11 14:04
 */
class SocketClientModel {

    private var gson = Gson()

    private var socket: Socket? = null

    private var composite = CompositeDisposable()

    fun close() {
        composite.dispose()
        kotlin.runCatching {
            socket?.close()
        }
    }

    fun sendRequest(url: String, clientRequest: ClientRequest, observer: SimpleObserver<SocketResponse>) {
        socketRequest(url, clientRequest)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(object : SimpleObserver<SocketResponse>(composite) {
                override fun onNext(t: SocketResponse?) {
                    observer.onNext(t)
                }

                override fun onError(e: Throwable?) {
                    observer.onError(e)
                }
            })
    }

    private fun socketRequest(url: String, request: ClientRequest): Observable<SocketResponse> {
        return Observable.create {
            socket = Socket(url, SocketParams.PORT)
            sendMessage(gson.toJson(request))
            socket!!.shutdownOutput();

            val scanner = Scanner(socket!!.getInputStream())
            while (scanner.hasNextLine()) {
                val info = scanner.nextLine()
                DebugLog.e("response：$info")
                kotlin.runCatching {
                    var response = gson.fromJson<SocketResponse>(info, SocketResponse::class.java)
                    it.onNext(response)
                }.let { result ->
                    result.onFailure { e ->
                        it.onError(e)
                    }
                }
            }
            it.onComplete()
        }
    }

    private fun sendMessage(msg: String) {
        DebugLog.e(msg)
        socket?.let {
            kotlin.runCatching {
                it.getOutputStream().write(msg.toByteArray())
                it.getOutputStream().flush()
            }
        }
    }

}