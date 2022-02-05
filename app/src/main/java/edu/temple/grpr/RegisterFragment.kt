package edu.temple.grpr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView


class RegisterFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_register, container, false)
        val firstName = layout.findViewById<TextView>(R.id.editTextFirstName)
        val lastName = layout.findViewById<TextView>(R.id.editTextLastName)
        val username = layout.findViewById<TextView>(R.id.editTextUserNameRegister)
        val password = layout.findViewById<TextView>(R.id.editTextPasswordRegister)
        val confirmPassword = layout.findViewById<TextView>(R.id.editTextPasswordConfirm)
        val registerButton = layout.findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener(){
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
                register(firstName.text.toString(), lastName.text.toString(), username.text.toString(), password.text.toString())
            }
        }
        return layout
    }

    fun register(firstName: String, lastName: String, username: String, password: String){
        //call to Volley

        //if successful let activity know (and save data)
        (activity as registerInterface).registerSuccessful()

        //else let user know
    }

    interface registerInterface{
        fun registerSuccessful()
    }

}