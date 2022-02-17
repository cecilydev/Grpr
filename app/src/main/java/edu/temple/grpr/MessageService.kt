package edu.temple.grpr

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import org.json.JSONObject


class MessageService : FirebaseMessagingService() {



    override fun onNewToken(p0: String) {
        if (Helper.user.getSessionKey(this)!=null){
            //update FCM if already logged in
            Helper.api.updateFCM(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, p0,  object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    if (Helper.api.isSuccess(response)) {
                        Log.d("FCM_TOKEN", "updated")
                    } else {
                        Log.d("FCM_TOKEN", "not updated with app server")
                    }
                }
            })
        }
        super.onNewToken(p0)
    }



}