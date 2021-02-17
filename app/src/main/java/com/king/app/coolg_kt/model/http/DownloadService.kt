package com.king.app.coolg_kt.model.http

import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadService {
    @Streaming
    @GET("download")
    fun download(
        @Query("type") type: String,
        @Query("name") name: String,
        @Query("key") key: String?
    ): Observable<ResponseBody>

    @Streaming
    @GET()
    fun download(@Url path: String): Observable<ResponseBody>

}