package team.hyperspace.firewatcher.network

import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface FireReportService {

    @Multipart
    @POST("/staging/api/v1.0/report")
    fun report(@Part file : MultipartBody.Part,
               @Part("UserName") userName : RequestBody,
               @Part("Latitude") latitude : RequestBody,
               @Part("Longitude") longtitude : RequestBody,
               @Part("Date") date : RequestBody) : Single<ReportResponse>
}

data class ReportResponse(val get_point : Int, val predictions : List<Prediction>) {}

data class Prediction ( val probability : Double,
                       val tagId : String,
                       val tagName : String) {}