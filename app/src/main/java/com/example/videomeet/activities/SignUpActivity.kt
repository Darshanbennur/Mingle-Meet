package com.example.videomeet.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.videomeet.databinding.ActivitySignUpBinding
import com.example.videomeet.models.User
import com.example.videomeet.utilities.Constants
import com.example.videomeet.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var preferenceManager : PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        binding.loginHereButton.setOnClickListener {
            finish()
        }

        binding.registerButton.setOnClickListener {
            val userName = binding.userName.text.toString()
            val userEmail = binding.userEmail.text.toString()
            val userPassword = binding.userPassword.text.toString()

            val user = User(userName, userEmail, userPassword)

            if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()){
                Toast.makeText(applicationContext,"Fields Can't be Empty", Toast.LENGTH_SHORT).show()
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                Toast.makeText(applicationContext,"Invalid Email Address Format",Toast.LENGTH_SHORT).show()
                binding.userEmail.requestFocus()
            }
            else if (userPassword.length < 8){
                Toast.makeText(applicationContext,"Password length minimum 8 characters",Toast.LENGTH_SHORT).show()
                binding.userPassword.requestFocus()
            }
            else{
                signUp(user)
            }

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun signUp(user : User){
        binding.registerButton.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
        var insert : HashMap<String, Any> = HashMap()
        insert[Constants.KEY_NAME] = user.Name
        insert[Constants.KEY_EMAIL] = user.Email
        insert[Constants.KEY_PASSWORD] = user.Password

        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(insert)
            .addOnSuccessListener {
                preferenceManager.putBoolean(Constants.KEY_IS_SINGED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                preferenceManager.putString(Constants.KEY_NAME, user.Name)
                preferenceManager.putString(Constants.KEY_EMAIL, user.Email)
                preferenceManager.putString(Constants.KEY_PASSWORD, user.Password)
                var intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.INVISIBLE
                binding.registerButton.visibility = View.VISIBLE
                Toast.makeText(applicationContext,"Error : " + it.message, Toast.LENGTH_SHORT).show()
            }
    }
}