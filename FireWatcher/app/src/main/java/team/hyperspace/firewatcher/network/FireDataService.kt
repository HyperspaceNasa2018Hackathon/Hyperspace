package team.hyperspace.firewatcher.network

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface FireDataService {
    @POST("/staging/api/v1.0/get_firms")
    fun getFirms(@Body body : GetFirmBody) : Single<List<FirmData>>

    @POST("/staging/api/v1.0/get_near_real_time")
    fun getRealTimeData() : Single<List<RealTimeData>>
}

data class GetFirmBody(val StartDate: String, val EndDate: String, val Confidence: Int) {}

data class FirmData(val latitude: Double,
                    val longitude : Double,
                    val brightness : Double,
                    val acq_date : String,
                    val acq_time : Int,
                    val confidence : Int,
                    val bright_t31 : Double,
                    val frp : Double,
                    val daynight : String) {}

data class RealTimeData(val latitude : String,
                        val longitude : String,
                        val temperature : String,
                        val acq_date : String,
                        val acq_time : String,
                        val confidence : String,
                        val resource : String,
                        val country : String,
                        val time_status : String,
                        val img_url : String)