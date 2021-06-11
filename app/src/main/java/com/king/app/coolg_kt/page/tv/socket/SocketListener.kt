package com.king.app.coolg_kt.page.tv.socket

import com.king.app.coolg_kt.model.socket.PlayVideoRequest

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/11 11:56
 */
interface SocketListener {
    fun onPlayVideo(bean: PlayVideoRequest)
    fun onPortOpened()
}