package edu.temple.grpr

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.temple.grpr.RegisterFragment.*
import edu.temple.grpr.LoginFragment.*

class MainActivity : AppCompatActivity(), loginInterface, registerInterface  {

    private lateinit var loginFragment : LoginFragment
    private lateinit var registerFragment: RegisterFragment
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
        }

    }

    companion object {
        private const val LOGIN_DATA = "login_data"
    }

    override fun clickRegister() {
        //switch fragment to register
        registerFragment = RegisterFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, registerFragment)
            .commit()
    }

    override fun loginSuccessful() {
        //switch fragment to map
    }

    override fun registerSuccessful() {
        //switch fragment to map
    }
}