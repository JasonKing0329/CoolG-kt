package com.king.app.coolg_kt.model.http.progress;

import com.king.app.coolg_kt.utils.DebugLog;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ProgressInterceptor implements Interceptor {

    ProgressListener mProgressListener;

    public ProgressInterceptor(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        DebugLog.e("Content-Disposition= " + originalResponse.header("Content-Disposition"));
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), mProgressListener))
                .build();
    }
}
