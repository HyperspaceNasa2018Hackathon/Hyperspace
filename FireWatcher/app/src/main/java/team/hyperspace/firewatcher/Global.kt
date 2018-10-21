package team.hyperspace.firewatcher

import android.app.Application
import com.facebook.stetho.Stetho

class Global : Application() {

    override fun onCreate() {
        super.onCreate()
//        Stetho.initializeWithDefaults(this)
    }
}