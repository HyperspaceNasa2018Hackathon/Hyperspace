package team.hyperspace.firewatcher.firemonitor

import android.arch.lifecycle.LiveData
import android.support.annotation.MainThread
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import team.hyperspace.firewatcher.network.FirmData
import team.hyperspace.firewatcher.network.GetFirmBody
import team.hyperspace.firewatcher.network.NetworkService
import java.text.SimpleDateFormat
import java.util.*

class FireDataLiveData : LiveData<List<FirmData>>() {
    private var disposable : Disposable = Disposables.disposed()

    @MainThread
    fun getCurrentFireData() {
        val dateStr = convertDateToString(Date())
        val body : GetFirmBody = GetFirmBody(dateStr, dateStr, 30)
        disposable.dispose()
        disposable = NetworkService.fireDataService.getFirms(body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(object : Action {
                override fun run() {
                    disposable = Disposables.disposed()
                }
            })
            .subscribe(
                object : Consumer<List<FirmData>> {
                    override fun accept(result: List<FirmData>?) {
                        value = result
                    }
                },
                object : Consumer<Throwable> {
                    override fun accept(t: Throwable?) {
                        Log.e("FireDataLiveData", "failed to load fire data", t)
                    }
                }
            )
    }

    private fun convertDateToString(date : Date) : String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(date)
    }
}