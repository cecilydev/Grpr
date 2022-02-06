package edu.temple.grpr

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text


class LoginFragment : Fragment() {

    var REGISTER = false
    lateinit var firstName: TextView
    lateinit var lastName: TextView
    lateinit var username: TextView
    lateinit var password: TextView
    lateinit var confirmPassword: TextView
    lateinit var login: Button
    lateinit var message: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout= inflater.inflate(R.layout.fragment_login, container, false)
        firstName = layout.findViewById(R.id.editTextFirstName)
        lastName = layout.findViewById(R.id.editTextLastName)
        username = layout.findViewById(R.id.editTextUsername)
        password = layout.findViewById(R.id.editTextPassword)
        confirmPassword = layout.findViewById(R.id.editTextPasswordConfirm)
        login = layout.findViewById(R.id.loginButton)
        message = layout.findViewById(R.id.textViewRegisterOrLogin)

        login.setOnClickListener(){
            if (REGISTER)  registerCheck()
            else loginCheck()
        }

        message.setOnClickListener(){
            if (REGISTER){
                message.setText(R.string.register_message)
                //hide
                firstName.visibility = View.GONE
                lastName.visibility = View.GONE
                confirmPassword.visibility = View.GONE
            } else{
                message.setText(R.string.login_message)
                firstName.visibility = View.VISIBLE
                lastName.visibility = View.VISIBLE
                confirmPassword.visibility = View.VISIBLE
            }
            REGISTER = !REGISTER
        }

        return layout
    }


    fun login(){
        //Volley request

        //if success, save data and let main activity know (so it can switch to map)
        (activity as loginInterface).loginSuccessful()
        //else let user know

    }

    fun loginCheck(){
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
            login()
        }
    }

    fun register(){
        //Volley request

        //if success, save data and let main activity know (so it can switch to map)
        (activity as loginInterface).loginSuccessful()
        //else let user know

    }

    fun registerCheck() {
        var error = false
        if (username.text.isEmpty()){
            username.error="Input username"
            error = true
        }
        if (firstName.text.isEmpty()){
            firstName.error="Input first name"
            error = true
        }
        if (lastName.text.isEmpty()){
            lastName.error="Input last name"
            error = true
        }
        if (password.text.isEmpty()){
            password.error="Input password"
            error = true
        }
        if (confirmPassword.text.isEmpty()){
            confirmPassword.error="Input password"
            error = true
        }
        if (password.text.toString()!=confirmPassword.text.toString()){
            confirmPassword.error="Passwords do not match"
            error = true
        }

        if (!error){
            register()
        }
    }


    interface loginInterface{
        fun loginSuccessful()
    }

}