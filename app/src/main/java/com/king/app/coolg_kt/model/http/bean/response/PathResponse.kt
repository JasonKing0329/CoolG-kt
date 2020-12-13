package com.king.app.coolg_kt.model.http.bean.response

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/14 13:50
 */
class PathResponse {
    /**
     * 格式如
     * folder/.../XXX.mp4
     * 拼接在BaseHttpClient.getBaseUrl()之后
     */
    var path: String? = null
    var isAvailable = false

}