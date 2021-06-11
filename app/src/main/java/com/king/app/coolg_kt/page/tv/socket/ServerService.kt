package com.king.app.coolg_kt.page.tv.socket

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.Gson
import com.king.app.coolg_kt.model.socket.ClientRequest
import com.king.app.coolg_kt.model.socket.PlayVideoRequest
import com.king.app.coolg_kt.model.socket.SocketParams
import com.king.app.coolg_kt.model.socket.SocketResponse
import com.king.app.coolg_kt.utils.DebugLog
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/10 15:36
 */
class ServerService: Service() {

    var server = ServerSocket(SocketParams.PORT)

    var serverThread: ServerThread? = null

    var clientThread: ClientThread? = null

    var gson = Gson()

    var socketListener: SocketListener? = null

    override fun onBind(intent: Intent?): IBinder? {
        return SocketBinder()
    }

    internal inner class SocketBinder: Binder() {

        val service: ServerService
            get() = this@ServerService

        fun setSocketListener(listener: SocketListener) {
            socketListener = listener
        }
    }

    fun start() {
        DebugLog.e()
        socketListener?.onPortOpened()
        ServerThread().start()
    }

    fun close() {
        DebugLog.e()
        clientThread?.close()
        clientThread?.interrupt()
        serverThread?.interrupt()
    }

    inner class ServerThread: Thread() {

        override fun run() {
            kotlin.runCatching {
                while (true) {
                    DebugLog.e("wait for socket")
                    var socket = server.accept()
                    DebugLog.e("accept socket")
                    clientThread = ClientThread(socket)
                    clientThread!!.start()
                    sleep(1000)
                }
            }
        }
    }

    inner class ClientThread(var socket: Socket): Thread() {

        init {
            val address = socket.inetAddress
            DebugLog.e("client ip：" + address.hostAddress)
        }

        override fun run() {
            parseClient()
        }

        /**
         * 解析客户端身份
         */
        private fun parseClient() {
            var scanner: Scanner? = null
            kotlin.runCatching {
                scanner = Scanner(socket.getInputStream())
                while (scanner!!.hasNextLine()) {
                    var json = scanner!!.nextLine();
                    DebugLog.e("parseClient：$json")
                    var request = gson.fromJson<ClientRequest>(json, ClientRequest::class.java)
                    if (request.identity.app == SocketParams.IDENTITY_APP) {
                        sendVerifySuccess()

                        handleAction(request)
                        break
                    }
                }
            }.let {
                it.onFailure { e ->
                    e.printStackTrace()
                }
            }
            scanner?.close()
            close()
        }

        private fun sendVerifySuccess() {
            sendMessage("Identity verify success")
        }

        private fun sendMessage(msg: String) {
            var pw: PrintWriter? = null
            try {
                pw = PrintWriter(socket.getOutputStream())
                var text = gson.toJson(SocketResponse(1, msg))
                DebugLog.e(text)
                pw.write(text)
                pw.flush()
                socket.shutdownOutput()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pw?.close()
            }
        }

        fun close() {
            socket.close()
            DebugLog.e("socket.close")
        }
    }

    private fun handleAction(request: ClientRequest) {
        when(request.action) {
            SocketParams.PLAY_VIDEO -> {
                var data = gson.fromJson<PlayVideoRequest>(request.data, PlayVideoRequest::class.java)
                onPlayVideo(data)
            }
        }
    }

    private fun onPlayVideo(bean: PlayVideoRequest) {
        DebugLog.e()
        socketListener?.onPlayVideo(bean)
    }
}