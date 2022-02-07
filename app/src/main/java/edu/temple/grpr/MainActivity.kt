package edu.temple. grpr

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.temple.grpr.LoginFragment
import edu.temple.grpr.LoginFragment.*
import edu.temple.grpr.MapsFragment
import edu.temple.grpr.R
import org.w3c.dom.Text

private const val SESSION_KEY = "session_key"
private const val USERNAME = "username"
private const val GROUP_ID = "group_id"

class MainActivity : AppCompatActivity(), loginInterface {

    private lateinit var loginFragment : LoginFragment
    private lateinit var mapFragment: MapsFragment
    private lateinit var preferences: SharedPreferences

    lateinit var current_group: TextView
    lateinit var close_group_button: FloatingActionButton

    lateinit var group_id: String
    lateinit var token: String
    lateinit var username: String
    var MAP = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getPreferences(MODE_PRIVATE)
        token= preferences.getString(SESSION_KEY, null).toString()
        username= preferences.getString(USERNAME, null).toString()
        group_id= preferences.getString(GROUP_ID, null).toString()

        current_group = findViewById(R.id.textViewCurrentGroup)
        close_group_button = findViewById(R.id.floatingCloseButton)

        Log.d("token", token)

        if(token=="null"){
            loginScreen()
        } else {
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
        val itemLogout = menu?.findItem(R.id.logout)
        val itemSetting = menu?.findItem(R.id.groups)
        if (itemLogout != null) itemLogout.setVisible(MAP)
        if(itemSetting !=null) itemSetting.setVisible(MAP)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.logout) {
            logout()
            return true
        }
        return false
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
            close_group_button.visibility = View.VISIBLE
        }
    }

    fun loginScreen(){
        loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainerView, loginFragment)
            .commit()
        MAP = false
        invalidateOptionsMenu()
    }

    fun logout() {
        //wipe token/login data
        //return to login page
        loginScreen()
    }


}