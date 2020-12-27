package com.king.app.coolg_kt.model.http.upload

import com.king.app.coolg_kt.model.http.bean.response.BaseResponse
import com.king.app.coolg_kt.model.http.bean.response.UploadResponse
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap

interface UploadService {

    /**
     * RequestBody是abstract类型的，retrofit在kotlin语言下回报出如下异常：
     * Parameter type must not include a type variable or wildcard
     * 需要添加@JvmSuppressWildcards的注解
     */
    @Multipart
    @POST("uploaddb")
    fun uploadDb(@PartMap params: Map<String, @JvmSuppressWildcards RequestBody>): Observable<BaseResponse<UploadResponse>>
}