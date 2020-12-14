package com.king.app.coolg_kt.model.http.upload

import com.king.app.coolg_kt.model.http.bean.response.BaseResponse
import com.king.app.coolg_kt.model.http.bean.response.UploadResponse
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap

interface UploadService {
    @Multipart
    @POST("uploaddb")
    fun uploadDb(@PartMap params: Map<String, RequestBody>): Observable<BaseResponse<UploadResponse>>
}