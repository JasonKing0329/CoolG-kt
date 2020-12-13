package com.king.app.coolg_kt.model.http;

import com.king.app.coolg_kt.utils.DebugLog;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * 拦截器
 * 获取请求地址，请求参数，返回值 写入本地日志
 * <p>
 */
public final class NormalLogInterceptor implements Interceptor {

    //过滤请求方式为post
    private static final String METHOD = "POST";
    //请求地址
    private static final String LINKS = "[url]";
    //请求参数body体
    private static final String REQUEST_BODY = "[request]";
    //返回值body体
    private static final String RESPONSE_BODY = "[response]";

    public NormalLogInterceptor() {
    }

    /**
     * 加同步锁才能保证url,request,response按顺序打印
     * @param chain
     * @return
     * @throws IOException
     */
    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        StringBuffer logBuffer = new StringBuffer();

        try {
            //记录请求链接
            String url = request.url().toString();
            logBuffer.append("\n").append(LINKS).append(url);

            //过滤post，记录post请求的请求体
            if (METHOD.equals(request.method())) {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                String strRequest = buffer.readUtf8();
                logBuffer.append("\n").append(REQUEST_BODY).append(strRequest);
                buffer.close();
            }

            //过滤post，记录post请求的返回体
            Response response = chain.proceed(request);
            ResponseBody responseBody = response.peekBody(1024 * 1024);
            String strResponse = responseBody.string();
            logBuffer.append("\n").append(RESPONSE_BODY).append(strResponse);

            DebugLog.e(logBuffer.toString());
            return response;
        } catch (IOException e) {
            if (e != null) {
                if (e instanceof SocketTimeoutException) {
                    logBuffer.append("\n").append("[SocketTimeoutException]").append(e.getMessage());
                }
                else {
                    logBuffer.append("\n").append("[IOException]").append(e.getMessage());
                }
            }
            DebugLog.e(logBuffer.toString());
            throw e;
        }
    }
}