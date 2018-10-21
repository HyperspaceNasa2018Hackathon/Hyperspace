package team.hyperspace.firewatcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists

class LauncherActivity : AppCompatActivity() {
    private val tag : String = "LauncherActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        requestNecessaryPermissions()
    }

    private val permissionRequestCode = 101
    private val necessaryPermissions : List<String> = Lists.newArrayList(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private fun requestNecessaryPermissions() {
        val notGrantedPermissions = getNotGrantedPermissions()
        if (notGrantedPermissions.isNotEmpty()) {
            val array : Array<String> = Array(notGrantedPermissions.size, { i -> notGrantedPermissions.get(i)})
            ActivityCompat.requestPermissions(this, array, permissionRequestCode)
        } else {
            jumpToFireWatcher()
        }
    }

    private fun getNotGrantedPermissions() : ImmutableList<String> {
        val listBuilder = ImmutableList.Builder<String>()
        for (permission : String in necessaryPermissions) {
            if (!isPermissionGranted(permission)) {
                listBuilder.add(permission)
            }
        }
        return listBuilder.build()
    }

    private fun isPermissionGranted(permission : String) : Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            permissionRequestCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(tag, "Permission has been denied by user")
                } else {
                    Log.i(tag, "Permission has been granted by user")
                    jumpToFireWatcher()
                }
            }
        }
    }

    private fun jumpToFireWatcher() {
        val handler : Handler = Handler()
        handler.postDelayed(
            object : Runnable {
                override fun run() {
                    val intent : Intent = Intent(this@LauncherActivity, FireWatcherActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }, 1500
        )
    }
}
