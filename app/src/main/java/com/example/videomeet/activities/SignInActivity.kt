package com.example.videomeet.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.videomeet.databinding.ActivitySignInBinding
import com.example.videomeet.models.User
import com.example.videomeet.utilities.Constants
import com.example.videomeet.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var preferenceManager : PreferenceManager
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)
        firebaseAuth = FirebaseAuth.getInstance()

//        if (preferenceManager.getBoolean(Constants.KEY_IS_SINGED_IN)){
//            var intent = Intent(applicationContext, HomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        }

        if (FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(applicationContext, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.registerHereButton.setOnClickListener {
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val userEmail = binding.userEmail.text.toString()
            val password = binding.userPassword.text.toString()

            val user = User(userEmail, password)

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                Toast.makeText(applicationContext,"Invalid Email Address Format", Toast.LENGTH_SHORT).show()
                binding.userEmail.requestFocus()
            }
            else if (password.length < 8){
                Toast.makeText(applicationContext,"Password length minimum 8 characters", Toast.LENGTH_SHORT).show()
                binding.userPassword.requestFocus()
            }
            else{
                alternateSignIn(user)
            }
        }

    }

    private fun alternateSignIn(user : User){
        binding.loginButton.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        firebaseAuth.signInWithEmailAndPassword(user.Email,user.Password).addOnCompleteListener {
            if (it.isSuccessful){
                val database : FirebaseFirestore = FirebaseFirestore.getInstance()
                database.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, user.Email)
                    .get()
                    .addOnCompleteListener { it2 ->
                        if (it2.isSuccessful && it2.result != null && it2.result.documents.size > 0){
                            val documentSnapshot : DocumentSnapshot = it2.result.documents[0]
                            preferenceManager.putBoolean(Constants.KEY_IS_SINGED_IN, true)
                            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME))
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL))
                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        } else{
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.loginButton.visibility = View.VISIBLE
                            Toast.makeText(applicationContext,"Unable to Login", Toast.LENGTH_SHORT).show()
                        }
                    }
                Toast.makeText(applicationContext,"Logged in Successfully", Toast.LENGTH_SHORT).show()
//                finish()
            }
            else{
                Toast.makeText(applicationContext,"Login Error", Toast.LENGTH_SHORT).show()
            }
        }

    }

//    private fun signIn(user : User) {
//        binding.loginButton.visibility = View.INVISIBLE
//        binding.progressBar.visibility = View.VISIBLE
//
//        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
//        database.collection(Constants.KEY_COLLECTION_USERS)
//            .whereEqualTo(Constants.KEY_EMAIL, user.Email)
//            .whereEqualTo(Constants.KEY_PASSWORD, user.Password)
//            .get()
//            .addOnCompleteListener {
//                if (it.isSuccessful && it.result != null && it.result.documents.size > 0){
//                    val documentSnapshot : DocumentSnapshot = it.result.documents[0]
//                    preferenceManager.putBoolean(Constants.KEY_IS_SINGED_IN, true)
//                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
//                    preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME))
//                    preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL))
//                    var intent = Intent(applicationContext, HomeActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    startActivity(intent)
//                } else{
//                    binding.progressBar.visibility = View.INVISIBLE
//                    binding.loginButton.visibility = View.VISIBLE
//                    Toast.makeText(applicationContext,"Unable to Login", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//
//    }
}