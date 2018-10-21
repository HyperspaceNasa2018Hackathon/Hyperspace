package team.hyperspace.firewatcher.utility

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.HandlerThread
import android.util.Log
import android.util.TimeUtils
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit


class LocationHelper {
    companion object {
        val locationHelper : LocationHelper = LocationHelper()
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handlerThread : HandlerThread = HandlerThread("Location Request Thread")

    @SuppressLint("MissingPermission")
    fun getUserLastPosition(context : Context) : Single<Location> {
        initFusedLocationProviderClient(context)
        return requestLocationUpdate()
    }

    private fun initFusedLocationProviderClient(context : Context) {
        if (!::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() : Single<Location> {
        val locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val subject : Subject<Location> = PublishSubject.create()
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                fusedLocationClient.removeLocationUpdates(this)
                if (result != null) {
                    Log.d("sean", "location result " + result.lastLocation)
                    subject.onNext(result.lastLocation)
                    subject.onComplete()
                } else {
                    subject.onError(Throwable("Get Location Failed"))
                }
            }
        }, handlerThread.looper)
        return subject.singleOrError()
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(context : Context) : Observable<Location> {
        initFusedLocationProviderClient(context)
        val locationRequest = LocationRequest()
        locationRequest.interval = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES)
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        val subject : Subject<Location> = PublishSubject.create()
        val callack = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                if (result != null) {
                    subject.onNext(result.lastLocation)
                } else {
                    subject.onError(Throwable("Get Location Failed"))
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, callack, handlerThread.looper)
        return subject.doOnDispose(object : Action {
            override fun run() {
                fusedLocationClient.removeLocationUpdates(callack)
            }

        })
    }
}