package team.hyperspace.firewatcher.firhistory

import android.arch.lifecycle.ViewModel

class FireHistoryViewModel : ViewModel() {
    val fireHistoryLiveData : FireHistoryLiveData by lazy {
        FireHistoryLiveData()
    }
}