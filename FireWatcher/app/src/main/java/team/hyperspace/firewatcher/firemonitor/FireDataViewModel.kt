package team.hyperspace.firewatcher.firemonitor

import android.arch.lifecycle.ViewModel

class FireDataViewModel : ViewModel() {

    val fireDataLiveData : FireDataLiveData by lazy {
        FireDataLiveData()
    }
}

