package com.king.app.coolg_kt.model.http

import com.king.app.coolg_kt.utils.DebugLog
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 10:17
 */
class AppHttpClient {

    private val TIMEOUT = 15000

    private var currentBaseUrl: String? = null

    companion object {
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
}