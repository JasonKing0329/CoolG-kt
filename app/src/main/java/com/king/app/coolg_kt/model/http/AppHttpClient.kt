package com.king.app.coolg_kt.model.http

import com.king.app.coolg_kt.utils.DebugLog
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 10:17
 */
class AppHttpClient {

    private var currentBaseUrl: String? = null

    companion object {

        val TIMEOUT = 15000
        val TIMEOUT_ONLINE = 2000

        private var instance: AppHttpClient? = null
        fun getInstance(): AppHttpClient {
            if (instance == null) {
                synchronized(AppHttpClient::class.java) {
                    if (instance == null) {
                        instance = AppHttpClient()
                    }
                }
            }
            return instance!!
        }
    }

    private lateinit var client: OkHttpClient

    private lateinit var appService: AppService

    private lateinit var appServiceCoroutine: AppServiceCoroutine

    private constructor() {
        val builder = OkHttpClient.Builder() // 打印url
            .addInterceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                DebugLog.e(request.url().toString())
                chain.proceed(request)
            }
        builder.addInterceptor(NormalLogInterceptor())
        builder.connectTimeout(TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        builder.addInterceptor(DynamicTimeoutInterceptor())
        client = builder.build()
        createRetrofit()
    }

    @Throws(Exception::class)
    fun createRetrofit() {
        val url: String = getBaseUrl()
        currentBaseUrl = url
        DebugLog.e(url)
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()
        createService(retrofit)
    }

    private fun createService(retrofit: Retrofit) {
        appService = retrofit.create(AppService::class.java)
        appServiceCoroutine = retrofit.create(AppServiceCoroutine::class.java)
    }

    private fun getBaseUrl(): String {
        return UrlProvider.formatUrl(UrlProvider.getBaseUrl())
    }

    fun getAppService(): AppService {
        var url = getBaseUrl()
        // baseUrl变了，重新创建retrofit
        if (url != currentBaseUrl) {
            createRetrofit()
        }
        return appService
    }

    fun getAppServiceCoroutine(): AppServiceCoroutine {
        var url = getBaseUrl()
        // baseUrl变了，重新创建retrofit
        if (url != currentBaseUrl) {
            createRetrofit()
        }
        return appServiceCoroutine
    }

    /**
     * 动态设置接口请求超时时间
     */
    class DynamicTimeoutInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val questUrl = request.url().toString()
            val isOnline = questUrl.endsWith("/online")
            var timeout: Int = TIMEOUT
            if (isOnline) {
                timeout = TIMEOUT_ONLINE
            }
            return chain.withConnectTimeout(timeout, TimeUnit.MILLISECONDS)
                .proceed(request)
        }
    }
}