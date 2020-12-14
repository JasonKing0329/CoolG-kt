package com.king.app.coolg_kt.model.http.upload

import com.king.app.coolg_kt.utils.DebugLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 拦截器
 * upload只打印返回信息
 *
 *
 */
class UploadLogInterceptor : Interceptor {
    /**
     * 加同步锁才能保证url,request,response按顺序打印
     * @param chain
     * @return
     * @throws IOException
     */
    @Synchronized
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val logBuffer = StringBuffer()
        return try {
            //记录请求链接
            val url = request.url().toString()
            logBuffer.append("\n").append(LINKS).append(url)

            //过滤post，记录post请求的返回体
            val response = chain.proceed(request)
            val responseBody = response.peekBody(1024 * 1024.toLong())
            val strResponse = responseBody.string()
            logBuffer.append("\n").append(RESPONSE_BODY)
                .append(strResponse)
            DebugLog.e(logBuffer.toString())
            response
        } catch (e: IOException) {
            if (e != null) {
                if (e is SocketTimeoutException) {
                    logBuffer.append("\n").append("[SocketTimeoutException]").append(e.message)
                } else {
                    logBuffer.append("\n").append("[IOException]").append(e.message)
                }
            }
            DebugLog.e(logBuffer.toString())
            throw e
        }
    }

    companion object {
        //过滤请求方式为post
        private const val METHOD = "POST"

        //请求地址
        private const val LINKS = "[url]"

        //请求参数body体
        private const val REQUEST_BODY = "[request]"

        //返回值body体
        private const val RESPONSE_BODY = "[response]"
    }
}