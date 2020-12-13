package com.king.app.coolg_kt.model.http.bean.response

class BaseResponse<T> {
    var result = 0
    var message: String? = null
    var errorCode = 0
    var data: T? = null
}