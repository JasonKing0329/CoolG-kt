package com.king.app.coolg_kt.model.http;

import com.king.app.coolg_kt.model.http.bean.request.FolderRequest;
import com.king.app.coolg_kt.model.http.bean.request.GdbCheckNewFileBean;
import com.king.app.coolg_kt.model.http.bean.request.GdbRequestMoveBean;
import com.king.app.coolg_kt.model.http.bean.request.GetStarRatingsRequest;
import com.king.app.coolg_kt.model.http.bean.request.PathRequest;
import com.king.app.coolg_kt.model.http.bean.request.UploadStarRatingRequest;
import com.king.app.coolg_kt.model.http.bean.request.VersionRequest;
import com.king.app.coolg_kt.model.http.bean.response.AppCheckBean;
import com.king.app.coolg_kt.model.http.bean.response.BaseResponse;
import com.king.app.coolg_kt.model.http.bean.response.FolderResponse;
import com.king.app.coolg_kt.model.http.bean.response.GdbMoveResponse;
import com.king.app.coolg_kt.model.http.bean.response.GdbRespBean;
import com.king.app.coolg_kt.model.http.bean.response.GetStarRatingResponse;
import com.king.app.coolg_kt.model.http.bean.response.OpenFileResponse;
import com.king.app.coolg_kt.model.http.bean.response.PathResponse;
import com.king.app.coolg_kt.model.http.bean.response.VersionResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2016/9/5.
 */
public interface AppService {
    @GET("online")
    Observable<GdbRespBean> isServerOnline();

    @GET("checkNew")
    Observable<AppCheckBean> checkAppUpdate(@Query("type") String type, @Query("version") String version);

    @GET("checkNew")
    Observable<AppCheckBean> checkGdbDatabaseUpdate(@Query("type") String type, @Query("version") String version);

    @GET("checkNew")
    Observable<GdbCheckNewFileBean> checkNewFile(@Query("type") String type);

    @POST("requestMove")
    Observable<GdbMoveResponse> requestMoveImages(@Body GdbRequestMoveBean data);

    @POST("surfFolder")
    Observable<FolderResponse> requestSurf(@Body FolderRequest data);

    @POST("uploadStarRatings")
    Observable<BaseResponse> uploadStarRatings(@Body UploadStarRatingRequest data);

    @POST("getStarRatings")
    Observable<BaseResponse<GetStarRatingResponse>> getStarRatings(@Body GetStarRatingsRequest data);

    @POST("videoPath")
    Observable<PathResponse> getVideoPath(@Body PathRequest data);

    @POST("openFile")
    Observable<OpenFileResponse> openFileOnServer(@Body PathRequest data);

    @POST("version")
    Observable<BaseResponse<VersionResponse>> getVersion(@Body VersionRequest data);
}
