package com.king.app.coolg_kt.model.http;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface DownloadService {

    @Streaming
    @GET("download")
    Observable<ResponseBody> download(@Query("type") String type, @Query("name") String name, @Query("key") String key);

}
