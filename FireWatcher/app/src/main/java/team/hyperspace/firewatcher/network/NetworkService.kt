package team.hyperspace.firewatcher.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkService {
    companion object {
        private val retrofit : Retrofit by lazy {
            create()
        }

        private fun create() : Retrofit {
            val okHttpClient : OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://hyperspace.southeastasia.cloudapp.azure.com")
                .client(okHttpClient)
                .build()

        }

        val fireDataService : FireDataService by lazy {
            createFireDataService()
        }

        private fun createFireDataService() : FireDataService {
            return retrofit.create(FireDataService::class.java)
        }

        val fireReportService : FireReportService by lazy {
            createFireReportService()
        }

        private fun createFireReportService() : FireReportService {
            return retrofit.create(FireReportService::class.java)
        }
    }
}