package team.hyperspace.firewatcher.firemonitor

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.common.collect.Collections2
import com.google.maps.android.heatmaps.HeatmapTileProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import team.hyperspace.firewatcher.R
import team.hyperspace.firewatcher.network.NetworkService
import team.hyperspace.firewatcher.network.RealTimeData
import team.hyperspace.firewatcher.utility.LocationHelper
import java.util.concurrent.TimeUnit

class FireMonitorFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val TAG : String = "FireMonitorFragment"
    }

    private lateinit var map: GoogleMap
    private lateinit var mapView : MapView
    private lateinit var fireDataModel : FireDataViewModel
    private var disposable : Disposable = Disposables.disposed()
    private var disposable2 : Disposable = Disposables.disposed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fireDataModel = ViewModelProviders.of(this).get(FireDataViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fire_monitor, container, false)
        mapView = view!!.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
    }

    fun onSearchStringUpdate(searchString : String?) {
        searchMap(searchString)
    }

    private fun searchMap(location : String?) {
        if (!TextUtils.isEmpty(location)) {
            val geocoder : Geocoder? = newGeocoder()
            if (geocoder != null) {
                try {
                    val addressList = geocoder.getFromLocationName(location, 1)
                    val address = addressList.get(0)
                    val latLng = LatLng(address.latitude, address.longitude)
                    val cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15f)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                } catch (t : Throwable) {
                    Log.e(TAG, "get location from string failed", t)
                }
            }
        }
    }

    private lateinit var geocoder: Geocoder

    @MainThread
    private fun newGeocoder() : Geocoder? {
        if (!::geocoder.isInitialized && context != null) {
            geocoder = Geocoder(context)
        }
        return geocoder
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            return onMapTypeSelected(item.itemId)
        }
        return false
    }

    private var selectedMapType : Int = 0

    private fun onMapTypeSelected(type : Int) : Boolean {
        val fragmentContext : Context? = context
        if (::map.isInitialized && fragmentContext != null) {
            when (type) {
                R.id.action_map_type -> {
                    AlertDialog.Builder(fragmentContext)
                        .setSingleChoiceItems(
                            arrayOf("normal", "satellite", "terrain", "hybrid"),
                            selectedMapType,
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, index: Int) {
                                    if (dialog != null) {
                                        when(index) {
                                            0 -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
                                            1 -> map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                                            2 -> map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                                            3 -> map.mapType = GoogleMap.MAP_TYPE_HYBRID
                                        }
                                        selectedMapType = index
                                        dialog.dismiss()
                                    }
                                }
                            }
                        )
                        .show()
                    return true
                }
            }
        }
        return false
    }

    override fun onSaveInstanceState(outState : Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        disposable2.dispose()
        disposable.dispose()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private lateinit var userLocation : Location
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        map.setMyLocationEnabled(true);
        val sydney = LatLng(22.543,120.356)
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        val fragmentContext : Context? = context
        if (fragmentContext != null) {
            disposable = LocationHelper.locationHelper.requestLocationUpdates(fragmentContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Consumer<Location> {
                    override fun accept(location: Location?) {
                        if (location != null) {
                            userLocation = location
                        }
                    }
                }, object : Consumer<Throwable> {
                    override fun accept(t: Throwable?) {}
                })

            disposable2 = Observable.interval(5, TimeUnit.SECONDS)
                .subscribe(
                    object : Consumer<Long> {
                        override fun accept(t: Long?) {
                            if (::userLocation.isInitialized) {
                                checkRealTimeData(userLocation)
                            }
                        }
                    }
                )
        }
    }

    private var lastWarningTime : Long = 0

    private fun checkRealTimeData(location: Location) {
        NetworkService.fireDataService.getRealTimeData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                object : Consumer<List<RealTimeData>> {
                    override fun accept(datas: List<RealTimeData>?) {
                        if (context != null && !isDetached && datas != null) {
                            try {
                                for (data in datas) {
                                    val fireLatitude : Double = data.latitude.toDouble()
                                    val fireLontitude : Double = data.longitude.toDouble()
                                    if (isNearFireScene(location.latitude, location.longitude, fireLatitude, fireLontitude) &&
                                        System.currentTimeMillis() - lastWarningTime > TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)) {
                                        lastWarningTime = System.currentTimeMillis()
                                        AlertDialog.Builder(context!!)
                                            .setMessage("You are near fire scene within 500m!!! Please go to safe place asap")
                                            .setPositiveButton("ok", object : DialogInterface.OnClickListener {
                                                override fun onClick(p0: DialogInterface?, p1: Int) {}
                                            }).show()
                                        break
                                    }
                                }
                                updateHeatMap(datas)
                            } catch (ignored : Throwable) { }
                        }
                    }
                },
                object : Consumer<Throwable> {
                    override fun accept(t: Throwable?) {
                    }
                }
            )
    }

    private fun isNearFireScene(userLatitude : Double, userLongitude : Double, fireLatitude : Double, fireLongitude : Double) : Boolean {
        return getDistance(userLatitude, userLongitude, fireLatitude, fireLongitude) < 500
    }

    fun getDistance(userLatitude : Double, userLongitude : Double, fireLatitude : Double, fireLongitude : Double): Float {
        val floatArray : FloatArray  = floatArrayOf(0f, 0f)
        Location.distanceBetween(userLatitude, userLongitude, fireLatitude, fireLongitude, floatArray)
        Log.d("sean", "userLatitude=$userLatitude, userLongitude=$userLongitude, fireLatitude=$fireLatitude, fireLongitude=$fireLongitude")
        Log.d("sean", "fire distance " + floatArray[0])
        return floatArray[0]
    }

    private lateinit var provider : HeatmapTileProvider
    private lateinit var tileOverlay : TileOverlay
    private fun updateHeatMap(data: List<RealTimeData>) {
        val latlngs : Collection<LatLng> = Collections2.transform(data, object : com.google.common.base.Function<RealTimeData, LatLng> {
            override fun apply(input: RealTimeData?): LatLng? {
                return LatLng(input!!.latitude.toDouble(), input.longitude.toDouble())
            }
        })
        provider = HeatmapTileProvider.Builder().data(latlngs).build()
        tileOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
    }
}