package edu.temple.grpr

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MyLocationService : Service() {

    override fun onBind(intent: Intent): IBinder {
        //create notficiation for foreground service

        //set up handler that communicates location
            //create VM with LatLng?


        //"Return the communication channel to the service."

    }
}