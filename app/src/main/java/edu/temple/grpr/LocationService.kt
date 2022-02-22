package edu.temple.grpr

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteAbortException
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

const val ONGOING_NOTIFICATION_ID = 1

class LocationService : Service() {
    lateinit var locationListener : LocationListener
    var locationHandler : Handler? = null
    var previousLocation : Location? = null

    val locationManager : LocationManager by lazy {
        getSystemService(LocationManager::class.java)
    }

    inner class LocationBinder: Binder() {
        fun setHandler(handler: Handler){
            locationHandler = handler
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return LocationBinder()
    }


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel("Location_ID", "Location Channel", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)


        val notification: Notification = Notification.Builder(this, "Location_ID")
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_location_on)
            .build()

        //set up location stuff
        locationListener = LocationListener{
           // if ((previousLocation!=null && it.distanceTo(previousLocation) - 5 >= 0) || previousLocation==null){
                    val message = Message.obtain()
                    val loc = LatLng(it.latitude, it.longitude)
                    message.obj = loc
                    locationHandler?.sendMessage(message)

                    if (Helper.user.getSessionKey(this)!=null)
                        Helper.api.updateLocation(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, Helper.user.getGroupId(this)!!, loc, null)

                   // previousLocation=it
               // }
        }

        startForeground(ONGOING_NOTIFICATION_ID, notification)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, locationListener)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        locationHandler=null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        locationManager.removeUpdates(locationListener)
        super.onDestroy()
    }


}