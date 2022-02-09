package edu.temple. grpr

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.MapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.temple.grpr.LocationViewModel
import edu.temple.grpr.LoginFragment
import edu.temple.grpr.LoginFragment.*
import edu.temple.grpr.MapsFragment
import edu.temple.grpr.R
import org.json.JSONObject
import java.util.function.Consumer

private const val SESSION_KEY = "session_key"
private const val USERNAME = "username"
private const val GROUP_ID = "group_id"

class MainActivity : AppCompatActivity(), loginInterface {

    val locationManager : LocationManager by lazy {
        getSystemService(LocationManager::class.java)
    }

    private lateinit var loginFragment : LoginFragment
    private lateinit var mapFragment: MapsFragment
    private lateinit var preferences: SharedPreferences

    lateinit var current_group: TextView
    lateinit var close_group_button: FloatingActionButton

    lateinit var group_id: String
    lateinit var token: String
    lateinit var username: String
    val url = "https://kamorris.com/lab/grpr/account.php"
    var MAP = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getPreferences(MODE_PRIVATE)
        token= preferences.getString(SESSION_KEY, null).toString()
        username= preferences.getString(USERNAME, null).toString()
        group_id= preferences.getString(GROUP_ID, null).toString()

        val locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)

        current_group = findViewById(R.id.textViewCurrentGroup)
        close_group_button = findViewById(R.id.floatingCloseButton)

        if(token=="null"){
            loginFragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, loginFragment)
                .commit()
            MAP = false
            invalidateOptionsMenu()
        } else {
          /*  if (!permissionGranted()) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 123)
            }*/
            //mapFragment
            mapFragment = MapsFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, mapFragment)
                .commit()
            if (group_id!="null"){
                current_group.text=group_id
                current_group.visibility = View.VISIBLE
                close_group_button.visibility = View.VISIBLE
            }
        }


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val itemLogout = menu?.findItem(R.id.action_logout)
        val itemSetting = menu?.findItem(R.id.groups)
        if (itemLogout != null) itemLogout.setVisible(MAP)
        if(itemSetting !=null) itemSetting.setVisible(MAP)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            logout()
            true
        }
        R.id.action_create -> {
            createGroup()
            true
        }
        else -> {
            false
        }
    }

    override fun updateData(username: String, session_key: String) {
        this.username= username
        token = session_key
        Log.d("new token", token.toString())
        val edit = preferences.edit()
        edit.putString(SESSION_KEY, session_key)
        edit.putString(USERNAME, username)
        edit.apply()
    }


    override fun loginSuccessful() {
        /*if (!permissionGranted()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 123)
        }*/
        //switch fragment to map
        mapFragment = MapsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, mapFragment)
            .commit()
        MAP = true
        invalidateOptionsMenu()

        if (group_id!="null"){
            current_group.text=group_id
            current_group.visibility = View.VISIBLE
            //close_group_button.visibility = View.VISIBLE
        }
    }

    fun loginScreen(){
        loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, loginFragment)
            .commit()
        MAP = false
        current_group.visibility = View.GONE
        close_group_button.visibility = View.GONE
        invalidateOptionsMenu()
    }

    fun logout() {
        //call to API
        val volleyQueue = Volley.newRequestQueue(this)
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            {
                val resp_json = JSONObject(it.toString())
                //wipe data and go to login screen
                if (resp_json.get("status")=="SUCCESS"){
                    val edit = preferences.edit()
                    edit.clear()
                    edit.apply()
                    loginScreen()
                //show the error to the user
                }else {
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
                params["action"] = "LOGOUT"
                params["username"] = username
                params["session_key"] = token
                return params
            }
        }
        volleyQueue.add(stringRequest)
    }

    fun createGroup(){
        //popup with
        //call to create
            //if successful, show close button and save group info to preferences. Start service

    }

    fun closeGroup(){
        //verify user wants to close
        //call to close
    }


    /*private fun permissionGranted () : Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            }
        }

    }*/



}