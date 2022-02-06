package edu.temple. grpr

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.temple.grpr.LoginFragment
import edu.temple.grpr.LoginFragment.*
import edu.temple.grpr.MapsFragment
import edu.temple.grpr.R

class MainActivity : AppCompatActivity(), loginInterface {



    private lateinit var loginFragment : LoginFragment
    private lateinit var mapFragment: MapsFragment
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        //possibly edit
        preferences = getSharedPreferences(LOGIN_DATA, MODE_PRIVATE)
        val token = preferences.getString("TOKEN", null)

        if(token==null){
            loginFragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, loginFragment)
                .commit()
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


    override fun loginSuccessful() {
        //switch fragment to map
        mapFragment = MapsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, mapFragment)
            .commit()
    }


}