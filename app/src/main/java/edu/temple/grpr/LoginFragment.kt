package edu.temple.grpr

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class LoginFragment : Fragment() {

    var REGISTER = false
    lateinit var firstName: TextView
    lateinit var lastName: TextView
    lateinit var username: TextView
    lateinit var password: TextView
    lateinit var confirmPassword: TextView
    lateinit var login: Button
    lateinit var message: TextView
    lateinit var error: TextView


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
        error = layout.findViewById(R.id.textViewError)


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
                login.setText(R.string.login)
            } else{
                message.setText(R.string.login_message)
                firstName.visibility = View.VISIBLE
                lastName.visibility = View.VISIBLE
                confirmPassword.visibility = View.VISIBLE
                login.setText(R.string.register)
            }
            REGISTER = !REGISTER
        }

        return layout
    }

    fun login(){
        Helper.api.login(requireContext(), User(username.text.toString(), null, null), password.text.toString(), object: Helper.api.Response {
            override fun processResponse(response: JSONObject) {
                if (Helper.api.isSuccess(response)) {
                    Helper.user.saveSessionData(requireContext(), response.getString("session_key"))
                    Helper.user.saveUser(requireContext(), User(
                        username.text.toString(),
                        null,
                        null
                    ))
                    (activity as loginInterface).loginSuccessful()
                } else {
                    error.text = getString((R.string.error), Helper.api.getErrorMessage(response))
                    error.visibility = View.VISIBLE
                }
            }
        } )
    }


    fun register() {
        Helper.api.createAccount(
            requireContext(),
            User(username.text.toString(), firstName.text.toString(), lastName.text.toString()),
            password.text.toString(),
            object : Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    if (Helper.api.isSuccess(response)) {
                        Helper.user.saveSessionData(
                            requireContext(),
                            response.getString("session_key")
                        )
                        Helper.user.saveUser(
                            requireContext(), User(
                                username.text.toString(),
                                firstName.text.toString(),
                                lastName.text.toString()
                            )
                        )
                        (activity as loginInterface).loginSuccessful()
                    } else {
                        error.text =
                            getString((R.string.error), Helper.api.getErrorMessage(response))
                        error.visibility = View.VISIBLE
                    }
                }
            })
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