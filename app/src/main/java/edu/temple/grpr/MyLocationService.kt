package edu.temple.grpr

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class LocationService : Service() {

    //create notficiation for foreground service

    //set up handler that communicates location
    //create VM with LatLng?
    inner class LocationBinder: Binder() {

    }

    override fun onBind(intent: Intent): IBinder {
        //create notficiation for foreground service
        return LocationBinder()
    }
}