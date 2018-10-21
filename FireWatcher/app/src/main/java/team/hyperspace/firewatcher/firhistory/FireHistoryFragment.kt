package team.hyperspace.firewatcher.firhistory

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.location.Geocoder
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
import com.google.common.collect.Lists
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.squareup.timessquare.CalendarPickerView
import team.hyperspace.firewatcher.R
import team.hyperspace.firewatcher.firemonitor.FireMonitorFragment
import team.hyperspace.firewatcher.network.FirmData
import java.util.*

class FireHistoryFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private fun getMinDate() : Date {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.set(2000, 9,1)
            return calendar.time
        }
    }

    private lateinit var map: GoogleMap
    private lateinit var mapView : MapView
    private lateinit var fireHistoryModel : FireHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fireHistoryModel = ViewModelProviders.of(this).get(FireHistoryViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fire_monitor, container, false)
        mapView = view!!.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
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
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        map.setMyLocationEnabled(true);
        val sydney = LatLng(22.543,120.356)
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        fireHistoryModel.fireHistoryLiveData.observe(this, object : Observer<List<FirmData>> {
            override fun onChanged(data: List<FirmData>?) {
                Log.d("sean", "data changed " + data)
                if (data != null && data.size > 0) {
                    updateHeatMap(data)
                } else {
                    clearHeatMap()
                }
            }
        })
        fireHistoryModel.fireHistoryLiveData.getCurrentFireData()
    }

    private lateinit var provider : HeatmapTileProvider
    private lateinit var tileOverlay : TileOverlay
    private fun updateHeatMap(data: List<FirmData>) {
        if (::tileOverlay.isInitialized) {
            tileOverlay.remove()
        }
        val latlngs : Collection<LatLng> = Collections2.transform(data, object : com.google.common.base.Function<FirmData, LatLng> {
            override fun apply(input: FirmData?): LatLng? {
                return LatLng(input!!.latitude, input.longitude)
            }
        })
        provider = HeatmapTileProvider.Builder().data(latlngs).build()
        tileOverlay = map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
    }

    private fun clearHeatMap() {
        if (::tileOverlay.isInitialized) {
            tileOverlay.remove()
        }
    }

    private var selectedMapType : Int = 0

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
                    Log.e(FireMonitorFragment.TAG, "get location from string failed", t)
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
                R.id.action_pick_date -> {
                    popupSelectDateRageDialog(fragmentContext)
                    return true
                }
            }
        }
        return false
    }

    private val defaultDate : Date = Calendar.getInstance(Locale.getDefault()).time
    private var beginDate : Date = defaultDate
    private var endDate : Date = defaultDate
    private val minDate : Date by lazy {
        getMinDate()
    }

    private fun popupSelectDateRageDialog(fragmentContext : Context) {
        val inflater = LayoutInflater.from(fragmentContext)
        if (inflater != null) {
            val pickerContainer = inflater.inflate(R.layout.dialog_pick_date_range_dialog, null)
            val picker : CalendarPickerView = pickerContainer.findViewById(R.id.calendar_view)
            val maxDate = Calendar.getInstance(Locale.getDefault())
            maxDate.add(Calendar.DAY_OF_MONTH, 1)
            val initializer : CalendarPickerView.FluentInitializer = picker.init(minDate, maxDate.time).inMode(
                CalendarPickerView.SelectionMode.RANGE)

            if (beginDate != endDate) {
                initializer.withSelectedDates(listOf(beginDate, endDate))
            } else {
                initializer.withSelectedDate(Calendar.getInstance(Locale.getDefault()).time)
            }

            val pickedDateMap : MutableMap<Long, Date> = mutableMapOf()
            picker.setOnDateSelectedListener(object : CalendarPickerView.OnDateSelectedListener {
                override fun onDateUnselected(date: Date?) {
                    if (date != null) {
                        pickedDateMap.clear()
                    }
                }

                override fun onDateSelected(date: Date?) {
                    if (date != null) {
                        pickedDateMap.put(date.time, date)
                    }
                }
            })
            AlertDialog.Builder(fragmentContext)
                .setView(pickerContainer)
                .setPositiveButton("ok", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, p1: Int) {
                        if (pickedDateMap.size == 2) {
                            val dates : MutableList<Date> = Lists.newArrayList(pickedDateMap.values)
                            Collections.sort(dates, object : Comparator<Date> {
                                override fun compare(date1: Date?, date2: Date?): Int {
                                    return if (date1!!.time >= date2!!.time) 1 else 0
                                }
                            })
                            beginDate = dates.get(0)
                            endDate = dates.get(1)
                            Log.d("sean", "begin date= " + beginDate + ", end date= " + endDate)
                            fireHistoryModel.fireHistoryLiveData.getFireHistoryInRange(beginDate, endDate)
                        } else if (pickedDateMap.size == 1) {
                            val dates : MutableList<Date> = Lists.newArrayList(pickedDateMap.values)
                            beginDate = dates.get(0)
                            endDate = beginDate
                            Log.d("sean", "begin date= " + beginDate + ", end date= " + endDate)
                            fireHistoryModel.fireHistoryLiveData.getFireHistoryInRange(beginDate, endDate)
                        }
                        if (dialog != null) {
                            dialog.dismiss()
                        }
                    }
                })
                .setNegativeButton("cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, p1: Int) {
                        if (dialog != null) {
                            dialog.dismiss()
                        }
                    }
                })
                .show()
        }
    }
}