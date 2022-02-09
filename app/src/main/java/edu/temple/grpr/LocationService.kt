package edu.temple.grpr

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Message
import com.google.android.gms.maps.model.LatLng

const val ONGOING_NOTIFICATION_ID = 1

class LocationService : Service() {
    lateinit var locationHandler : Handler
    var previousLocation : Location? = null

    inner class LocationBinder: Binder() {
        fun setHandler(handler: Handler){
            locationHandler = handler
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return LocationBinder()
    }


    override fun onCreate() {
        super.onCreate()
        val notification: Notification = Notification.Builder(this, "Location_ID")
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_location_on)
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)


        //set up location stuff
        val locationmanager = getSystemService(LocationManager::class.java)
        val locationListener = LocationListener{
            if (previousLocation!=null){
                //if
                if (it.distanceTo(previousLocation) - 5 >= 0) {
                    //update handler
                    var message = Message.obtain()
                    message.obj = LatLng(it.latitude, it.longitude)
                    locationHandler.sendMessage(message)

                    //update prev location
                    previousLocation=it
                }
            } else {
                previousLocation = it
            }

        }

        

    }


}