package com.king.app.coolg_kt.model.socket

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/10 16:20
 */
data class ClientIdentity (
    var app: String,
    var deviceType: String
)
data class SocketResponse (
    var result: Int,
    var msg: String
)
data class ClientRequest (
    var identity: ClientIdentity,
    var action: String,
    var data: String
)