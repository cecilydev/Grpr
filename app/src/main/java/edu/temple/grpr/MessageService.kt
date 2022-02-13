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

private const val SESSION_KEY = "session_key"
private const val acct_url = "https://kamorris.com/lab/grpr/account.php"

class MessageService : FirebaseMessagingService() {

    private lateinit var preferences: SharedPreferences


    override fun onNewToken(p0: String) {
        preferences = getSharedPreferences("GRPR", MODE_PRIVATE)
        val session = preferences.getString(SESSION_KEY, null)

        if (session!=null){
            //update FCM if already logged in
            updateFCMToken(p0)
        }
        super.onNewToken(p0)
    }




    fun updateFCMToken(fcm_token: String) {
        preferences = getSharedPreferences("GRPR", MODE_PRIVATE)
        val volleyQueue = Volley.newRequestQueue(this)
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, acct_url,
            {
                val resp_json = JSONObject(it.toString())
                //save data and open map
                if (resp_json.get("status")!="SUCCESS"){
                    Toast.makeText(this, getString((R.string.error), resp_json.get("message")), Toast.LENGTH_LONG).show()
                }
            },
            {})
        {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["action"]="UPDATE"
                params["username"]=preferences.getString("username", null).toString()
                params[SESSION_KEY]=preferences.getString(SESSION_KEY, null).toString()
                params["fcm_token"]=fcm_token
                return params
            }
        }
        volleyQueue.add(stringRequest)
    }


}