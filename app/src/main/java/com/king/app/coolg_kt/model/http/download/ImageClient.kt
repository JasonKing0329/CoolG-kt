package com.king.app.coolg_kt.model.http.download

import com.king.app.coolg_kt.model.http.AppHttpClient
import com.king.app.coolg_kt.model.http.DownloadService
import com.king.app.coolg_kt.utils.DebugLog
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/17 13:44
 */
class ImageClient {

    private var client: OkHttpClient? = null

    private var downloadService: DownloadService? = null

    companion object {
        private var instance: ImageClient? = null
        fun getInstance(): ImageClient {
            if (instance == null) {
                synchronized(AppHttpClient::class.java) {
                    if (instance == null) {
                        instance =
                            ImageClient()
                    }
                }
            }
            return instance!!
        }
    }

    private constructor() {
        val builder = OkHttpClient.Builder() // 打印url
            .addInterceptor { chain ->
                val request = chain.request()
                DebugLog.e(request.url().toString())
                // 太多了，不需要记录到日志体系
                //                        PadLogger.crmlog("Request", request.url().toString());
                chain.proceed(request)
            }
        client = builder.build()
        createRetrofit()
    }

    protected fun createRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://www.baidu.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(client)
            .build()
        downloadService = retrofit.create(DownloadService::class.java)
    }

    fun getDownloadService(): DownloadService {
        return downloadService!!
    }
}