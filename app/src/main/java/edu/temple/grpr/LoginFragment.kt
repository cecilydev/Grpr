package edu.temple.grpr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView


class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout= inflater.inflate(R.layout.fragment_login, container, false)
        val username = layout.findViewById<TextView>(R.id.editTextUsernameLogin)
        val password = layout.findViewById<TextView>(R.id.editTextPasswordLogin)
        val login = layout.findViewById<Button>(R.id.loginButton)
        val register = layout.findViewById<TextView>(R.id.textViewRegister)

        login.setOnClickListener(){
            var error = false
            if (username.text.isEmpty()){
                username.error="Input username"
                error = true
            }
            if (password.text.isEmpty()){
                password.error="Input password"
                error = true
            }
            if (!error){
                login(username.text.toString(), password.text.toString())
            }
        }

        register.setOnClickListener(){
            (activity as loginInterface).clickRegister()
        }

        return layout
    }


    fun login(username: String, password: String): Boolean{
        //Volley request

        //if success, save data and let main activity know (so it can switch to map)
        (activity as loginInterface).loginSuccessful()
        //else let user know
        return false
    }



    interface loginInterface{
        fun clickRegister()
        fun loginSuccessful()
    }

}