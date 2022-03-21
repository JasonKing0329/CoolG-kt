package com.king.app.coolg_kt.model.http

import com.king.app.coolg_kt.model.http.bean.response.AppCheckBean
import com.king.app.coolg_kt.model.http.bean.response.BgResponse
import com.king.app.coolg_kt.model.http.bean.response.GdbRespBean
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Administrator on 2016/9/5.
 */
interface AppServiceCoroutine {

    @GET("online")
    suspend fun isServerOnline(): GdbRespBean

    @GET("bg")
    suspend fun getBgFiles(): BgResponse

    @GET("checkNew")
    suspend fun checkAppUpdate(
        @Query("type") type: String,
        @Query("version") version: String
    ): AppCheckBean

}