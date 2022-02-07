package edu.temple. grpr

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.temple.grpr.LoginFragment
import edu.temple.grpr.LoginFragment.*
import edu.temple.grpr.MapsFragment
import edu.temple.grpr.R

class MainActivity : AppCompatActivity(), loginInterface {

    private lateinit var loginFragment : LoginFragment
    private lateinit var mapFragment: MapsFragment
    var MAP = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = getSharedPreferences(LOGIN_DATA, MODE_PRIVATE)
        var token = preferences.getString("TOKEN", null)

        if(token==null){
            loginScreen()
        } else {
            //mapFragment
            mapFragment = MapsFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, mapFragment)
                .commit()
        }

    }

    companion object {
        private const val LOGIN_DATA = "login_data"
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


    override fun loginSuccessful() {
        //switch fragment to map
        mapFragment = MapsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, mapFragment)
            .commit()
        MAP = true
        invalidateOptionsMenu()
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