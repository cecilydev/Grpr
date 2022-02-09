package edu.temple.grpr

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.util.Log
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

        startForeground(ONGOING_NOTIFICATION_ID, notification)


        //set up location stuff
        val locationManager = getSystemService(LocationManager::class.java)
        val locationListener = LocationListener{
            if (previousLocation!=null){
                //if
                if (it.distanceTo(previousLocation) - 5 >= 0) {
                    //update handler
                    val message = Message.obtain()
                    message.obj = LatLng(it.latitude, it.longitude)
                    locationHandler.sendMessage(message)

                    Log.d("Location", it.toString())

                    //update prev location
                    previousLocation=it
                }
            } else {
                previousLocation = it
            }
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000.toLong(), 5f, locationListener)


    }



}