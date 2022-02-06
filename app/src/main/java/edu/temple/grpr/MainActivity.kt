package edu.temple. grpr

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
        val token = preferences.getString("TOKEN", null)


        if(token==null){
            loginFragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, loginFragment)
                .commit()
            MAP = false
            invalidateOptionsMenu()

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
        val item = menu?.findItem(R.id.logout)
        if (item != null)
            item.setVisible(MAP)
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

    fun logout() {
        //wipe token/login data
        //exit app
        finish()
    }


}