package com.king.app.coolg_kt.model.http

import com.king.app.coolg_kt.model.http.bean.request.*
import com.king.app.coolg_kt.model.http.bean.response.*
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Administrator on 2016/9/5.
 */
interface AppService {

    @GET("online")
    fun isServerOnline(): Observable<GdbRespBean>

    @GET("checkNew")
    fun checkAppUpdate(
        @Query("type") type: String,
        @Query("version") version: String
    ): Observable<AppCheckBean>

    @GET("checkNew")
    fun checkGdbDatabaseUpdate(
        @Query("type") type: String,
        @Query("version") version: String
    ): Observable<AppCheckBean>

    @GET("checkNew")
    fun checkNewFile(@Query("type") type: String): Observable<GdbCheckNewFileBean>

    @POST("requestMove")
    fun requestMoveImages(@Body data: GdbRequestMoveBean): Observable<GdbMoveResponse>

    @POST("surfFolder")
    fun requestSurf(@Body data: FolderRequest): Observable<FolderResponse>

    @POST("subtitle")
    fun searchSubtitle(@Body data: SubtitleRequest): Observable<SubtitleResponse>

    @POST("uploadStarRatings")
    fun uploadStarRatings(@Body data: UploadStarRatingRequest): Observable<BaseResponse<*>>

    @POST("getStarRatings")
    fun getStarRatings(@Body data: GetStarRatingsRequest): Observable<BaseResponse<GetStarRatingResponse>>

    @POST("videoPath")
    fun getVideoPath(@Body data: PathRequest): Observable<PathResponse>

    @POST("openFile")
    fun openFileOnServer(@Body data: PathRequest): Observable<OpenFileResponse>

    @POST("version")
    fun getVersion(@Body data: VersionRequest): Observable<BaseResponse<VersionResponse>>
}