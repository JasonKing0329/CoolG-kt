package com.king.app.coolg_kt.model.http.upload

import com.king.app.coolg_kt.model.http.UrlProvider
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
class UploadClient {

    private val TIMEOUT = 15000

    companion object {
        private var instance: UploadClient? = null
        fun getInstance(): UploadClient {
            if (instance == null) {
                synchronized(UploadClient::class.java) {
                    if (instance == null) {
                        instance = UploadClient()
                    }
                }
            }
            return instance!!
        }
    }

    private lateinit var client: OkHttpClient

    private lateinit var uploadService: UploadService

    private constructor() {
        val builder = OkHttpClient.Builder() // 打印url
            .addInterceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                DebugLog.e(request.url().toString())
                chain.proceed(request)
            }
        builder.addInterceptor(UploadLogInterceptor())
        builder.connectTimeout(TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
        client = builder.build()
        createRetrofit()
    }

    @Throws(Exception::class)
    fun createRetrofit() {
        val url: String = UrlProvider.formatUrl(UrlProvider.getBaseUrl())
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
        uploadService = retrofit.create(UploadService::class.java)
    }

    fun getService(): UploadService {
        return uploadService
    }
}