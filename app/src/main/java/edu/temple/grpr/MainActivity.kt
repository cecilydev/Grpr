package edu.temple. grpr

import android.Manifest
import android.R.drawable.ic_menu_close_clear_cancel
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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import edu.temple.grpr.LoginFragment.*
import org.json.JSONObject

private const val NEW_INSTANCE=0
private const val SAVED_INSTANCE=1

class MainActivity : AppCompatActivity(), loginInterface {

    var isConnected = false
    lateinit var locationBinder: LocationService.LocationBinder
    private lateinit var loginFragment : LoginFragment
    private lateinit var mapFragment: MapsFragment

    lateinit var current_group: TextView
    lateinit var create_close_group_button: FloatingActionButton

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

        val session = Helper.user.getSessionKey(this)
        val group_id = Helper.user.getGroupId(this)
        //val username= preferences.getString(USERNAME, null).toString()

        current_group = findViewById(R.id.textViewCurrentGroup)
        create_close_group_button = findViewById(R.id.floatingCreateCloseButton)

        create_close_group_button.setOnClickListener {
            if (create_close_group_button.tag=="close")
            createDialog("CLOSE GROUP", "Are you sure you want to close this group?", "close").show()
            else {
                Helper.api.createGroup(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, object: Helper.api.Response {
                    override fun processResponse(response: JSONObject) {
                        if (Helper.api.isSuccess(response)) {
                            val group=response.getString("group_id")
                            Helper.user.saveGroupId(this@MainActivity, group)
                            current_group.text=getString(R.string.current_group, group)
                            current_group.visibility = View.VISIBLE
                            create_close_group_button.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                            create_close_group_button.tag="close"
                            createDialog("CREATE GROUP", getString(R.string.new_group, group), "create").show()
                            bindService(
                                Intent(this@MainActivity, LocationService::class.java)
                                , serviceConnection
                                , BIND_AUTO_CREATE
                            )
                        } else {
                            Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }


        if(session==null){
            if (savedInstanceState==null) loginScreen(NEW_INSTANCE)
            else loginScreen(SAVED_INSTANCE)
        } else {
            if (savedInstanceState==null) mapScreen(NEW_INSTANCE)
            else mapScreen(SAVED_INSTANCE)
        }

    }



    //menu related stuff
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
            Helper.api.logout(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    if (Helper.api.isSuccess(response)) {
                        Helper.user.clearSessionData(this@MainActivity)
                        loginScreen(SAVED_INSTANCE)
                    } else {
                        Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                    }
                }
            })
            true
        }
        R.id.action_join ->{
            //joinGroup
            createDialog("JOIN GROUP", "", "join").show()
            true
        }
        R.id.action_leave -> {
            //leaveGroup
            true
        }
        else -> {
            false
        }
    }


    //login and fragment related stuff

    override fun loginSuccessful() {
        mapScreen(SAVED_INSTANCE)
        MAP = true
        invalidateOptionsMenu()

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            //update app server
            Helper.api.updateFCM(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!,it,  object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    if (Helper.api.isSuccess(response)) {
                        Log.d("FCM_TOKEN", "updated")
                    } else {
                        Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
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
        create_close_group_button.visibility = View.GONE
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
        create_close_group_button.visibility = View.VISIBLE
        val group = Helper.user.getGroupId(this@MainActivity)
        if (group!=null){
            current_group.text=getString(R.string.current_group, group)
            current_group.visibility = View.VISIBLE
            create_close_group_button.setImageResource(ic_menu_close_clear_cancel)
            create_close_group_button.tag="close"
            bindService(
                Intent(this, LocationService::class.java)
                , serviceConnection
                , BIND_AUTO_CREATE
            )
        }else{
            if (Helper.user.getSessionKey(this@MainActivity)!=null){
                //query and start service if there
                Helper.api.queryStatus(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!,  object: Helper.api.Response {
                    override fun processResponse(response: JSONObject) {
                        if (Helper.api.isSuccess(response)) {
                            Helper.user.saveGroupId(this@MainActivity, response.getString("group_id"))
                            current_group.text=getString(R.string.current_group, response.getString("group_id"))
                            current_group.visibility = View.VISIBLE
                            create_close_group_button.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                            create_close_group_button.tag="close"
                            bindService(
                                Intent(this@MainActivity, LocationService::class.java)
                                , serviceConnection
                                , BIND_AUTO_CREATE
                            )
                        } else {
                            Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }

        }

    }


    //permissions

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


    //dialogs

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
                        Helper.api.closeGroup(this@MainActivity, Helper.user.get(this@MainActivity), Helper.user.getSessionKey(this@MainActivity)!!,  Helper.user.getGroupId(this@MainActivity)!!, object: Helper.api.Response {
                            override fun processResponse(response: JSONObject) {
                                if (Helper.api.isSuccess(response)) {
                                    Helper.user.clearGroupId(this@MainActivity)
                                    unbindService(serviceConnection)
                                    //group_id="null"
                                    current_group.text=""
                                    current_group.visibility = View.GONE
                                    create_close_group_button.setImageResource(android.R.drawable.ic_input_add)
                                    create_close_group_button.tag="create"
                                } else {
                                    Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                        dialog.dismiss()
                    })
                setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            }
        }
        if (type.equals("join")){
            builder.apply {
                setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                       // dialog.dismiss()
                    })
                setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
                setView(R.layout.join_dialog)
            }
        }
        return builder.create()
    }

}