package edu.temple.grpr

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
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


    override fun onMessageReceived(p0: RemoteMessage) {
        val payload = JSONObject(p0.data["payload"].toString())
        if (payload.getString("action")=="UPDATE") {
            val data = payload.getString("data")
            val broadcast = LocalBroadcastManager.getInstance(this)
            val intent = Intent(Intent.ACTION_ATTACH_DATA)
            val result = broadcast.sendBroadcast(intent.putExtra("data", payload.getString("data")))
        }
        if (payload.getString("action")=="END"){
            //handle end here
        }
    }



}