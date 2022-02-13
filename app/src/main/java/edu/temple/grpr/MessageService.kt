package edu.temple.grpr

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

private const val FCM_KEY="fcm_token"
private const val SESSION_KEY = "session_key"

class MessageService : FirebaseMessagingService() {

    private lateinit var preferences: SharedPreferences

    override fun onNewToken(p0: String) {
        preferences = getSharedPreferences("GRPR", MODE_PRIVATE)
        val session = preferences.getString(SESSION_KEY, null)

        if (session!=null){
            //update FCM if already logged in
        }
        super.onNewToken(p0)
    }


}