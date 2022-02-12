package edu.temple. grpr

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.temple.grpr.LoginFragment.*
import org.json.JSONObject

private const val SESSION_KEY = "session_key"
private const val USERNAME = "username"
private const val GROUP_ID = "group_id"
private const val NEW_INSTANCE=0
private const val SAVED_INSTANCE=1
private const val acct_url = "https://kamorris.com/lab/grpr/account.php"
private const val grp_url="https://kamorris.com/lab/grpr/group.php"


class MainActivity : AppCompatActivity(), loginInterface {

    var isConnected = false
    lateinit var locationBinder: LocationService.LocationBinder
    private lateinit var loginFragment : LoginFragment
    private lateinit var mapFragment: MapsFragment
    private lateinit var preferences: SharedPreferences

    lateinit var current_group: TextView
    lateinit var close_group_button: FloatingActionButton

    lateinit var group_id: String
    lateinit var session: String
    lateinit var username: String

    var MAP = true

    private val locationViewModel : LocationViewModel by lazy {
        ViewModelProvider(this).get(LocationViewModel::class.java)
    }

    val locationHandler = Handler(Looper.getMainLooper()) {
        if (it.obj != null) {
            locationViewModel.setLatLng(it.obj as LatLng)
        }
        true
    }


    val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isConnected = true
            locationBinder = service as LocationService.LocationBinder
            locationBinder.setHandler(locationHandler)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getPreferences(MODE_PRIVATE)
        session= preferences.getString(SESSION_KEY, null).toString()
        username= preferences.getString(USERNAME, null).toString()
        group_id= preferences.getString(GROUP_ID, null).toString()

        current_group = findViewById(R.id.textViewCurrentGroup)
        close_group_button = findViewById(R.id.floatingCloseButton)

        close_group_button.setOnClickListener {
            closeGroup()
        }


        if(session=="null"){
            if (savedInstanceState==null) loginScreen(NEW_INSTANCE)
            else loginScreen(SAVED_INSTANCE)
        } else {
            if (savedInstanceState==null) mapScreen(NEW_INSTANCE)
            else mapScreen(SAVED_INSTANCE)
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
        session = session_key
        val edit = preferences.edit()
        edit.putString(SESSION_KEY, session_key)
        edit.putString(USERNAME, username)
        edit.apply()
    }


    override fun loginSuccessful() {
        mapScreen(SAVED_INSTANCE)
        MAP = true
        invalidateOptionsMenu()
    }


    fun loginScreen(Instance: Int){
        loginFragment = LoginFragment()
        if (Instance== NEW_INSTANCE){
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, loginFragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, loginFragment)
                .commit()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, loginFragment)
            .commit()
        MAP = false
        current_group.visibility = View.GONE
        close_group_button.visibility = View.GONE
        invalidateOptionsMenu()
    }

    fun mapScreen(Instance: Int){
        if (!permissionGranted()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 123)
        } //mapFragment
        mapFragment = MapsFragment()
        if (Instance== NEW_INSTANCE) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, mapFragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, mapFragment)
                .commit()
        }
        if (group_id!="null"){
            current_group.text=getString(R.string.current_group, group_id)
            current_group.visibility = View.VISIBLE
            close_group_button.visibility = View.VISIBLE
        }

    }

    fun logout() {
        //call to API
        val params: MutableMap<String, String> = HashMap()
        val resp: (JSONObject) -> Unit = {resp_json: JSONObject ->
            //wipe data and go to login screen
            if (resp_json.get("status") == "SUCCESS") {
                val edit = preferences.edit()
                edit.clear()
                edit.apply()
                loginScreen(SAVED_INSTANCE)
                //show the error to the user
            } else {
                Toast.makeText(
                    this, getString((R.string.error), resp_json.get("message")), Toast.LENGTH_LONG).show()
            }
        }

        params["action"] = "LOGOUT"
        params["username"] = username
        params["session_key"] = session

        volleyRequest(acct_url, params, resp)
    }

    fun createGroup(){
        val params: MutableMap<String, String> = HashMap()
        val resp: (JSONObject) -> Unit = {resp_json: JSONObject ->
            //wipe data and go to login screen
            if (resp_json.get("status") == "SUCCESS") {
                val edit = preferences.edit()
                edit.putString("group_id", resp_json.getString("group_id"))
                edit.apply()
                group_id=resp_json.getString("group_id")
                close_group_button.visibility = View.VISIBLE
                current_group.text=getString(R.string.current_group, group_id)
                current_group.visibility = View.VISIBLE
                createDialog("CREATE GROUP", getString(R.string.new_group, group_id), "create").show()
                bindService(
                    Intent(this, LocationService::class.java)
                    , serviceConnection
                    , BIND_AUTO_CREATE
                )
                //show the error to the user
            } else {
                Toast.makeText(
                    this, getString((R.string.error), resp_json.get("message")), Toast.LENGTH_LONG).show()
            }
        }
        params["action"] = "CREATE"
        params["username"] = username
        params["session_key"] = session

        volleyRequest(grp_url, params, resp)

    }

    fun closeGroup(){
        //verify user wants to close
        createDialog("CLOSE GROUP", "Are you sure you want to close this group?", "close").show()
    }


    private fun permissionGranted () : Boolean {
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

    }

    fun createDialog(title: String, message: String, type: String) : AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message)
        if (type.equals("create")){
            builder.apply {
                setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            }
        }
        if (type.equals("close")) {
            builder.apply {
                setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User clicked OK button
                        val params: MutableMap<String, String> = HashMap()
                        val resp: (JSONObject) -> Unit = {resp_json: JSONObject ->
                            //wipe data and go to login screen
                            if (resp_json.get("status") == "SUCCESS") {
                                val edit = preferences.edit()
                                edit.remove("group_id")
                                edit.apply()
                                unbindService(serviceConnection)
                                group_id="null"
                                current_group.text=""
                                current_group.visibility = View.GONE
                                close_group_button.visibility = View.GONE
                                //show the error to the user
                            } else {
                                Toast.makeText(this.context, getString((R.string.error), resp_json.get("message")), Toast.LENGTH_LONG).show()
                            }
                        }

                        params["action"] = "CLOSE"
                        params["username"] = username
                        params["session_key"] = session
                        params["group_id"]=group_id
                        volleyRequest(grp_url, params, resp)
                        dialog.dismiss()


                    })
                setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            }
            }

        return builder.create()
    }


    fun volleyRequest(url:String, params: MutableMap<String, String>, onResult: (result: JSONObject) -> Unit ){
        val volleyQueue = Volley.newRequestQueue(this)
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            {
                val resp_json = JSONObject(it)
                onResult(resp_json)
            },
            {})
        {
            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }
            override fun getParams(): MutableMap<String, String> {
                return params
            }
        }
        volleyQueue.add(stringRequest)
    }

}