package com.example.videomeet.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.videomeet.R
import com.example.videomeet.databinding.ActivitySignInBinding
import com.example.videomeet.models.User
import com.example.videomeet.utilities.Constants
import com.example.videomeet.utilities.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
    private lateinit var preferenceManager : PreferenceManager
    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)
        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignIn.setOnClickListener {
            signInWithGoogle()
        }

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

    private fun signInWithGoogle(){
        binding.googleSignIn.visibility = View.INVISIBLE
        binding.progressBarEmail.visibility = View.VISIBLE

        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
            if(result.resultCode == Activity.RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>?) {
        if (task!!.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        } else {
            Toast.makeText(applicationContext,task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.fetchSignInMethodsForEmail(account.email.toString()).addOnCompleteListener { itCheckEmail ->
            val result : SignInMethodQueryResult = itCheckEmail.result
            val signInMethods : List<String> = result.signInMethods!!
            firebaseAuth.signInWithCredential(credentials)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
                        if(signInMethods.isNotEmpty()){
                            //when the user is old
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                .whereEqualTo(Constants.KEY_EMAIL, account.email)
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
                                        binding.progressBarEmail.visibility = View.INVISIBLE
                                        binding.googleSignIn.visibility = View.VISIBLE
                                        Toast.makeText(applicationContext,"Unable to Login", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            Toast.makeText(applicationContext,"Logged in Successfully", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            // when the user is new
                            val userName = account.displayName
                            val userEmail = account.email

                            val insert : HashMap<String, Any> = HashMap()
                            insert[Constants.KEY_NAME] = userName.toString()
                            insert[Constants.KEY_EMAIL] = userEmail.toString()
                            insert["isOnline"] = true

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                .add(insert)
                                .addOnSuccessListener { it1 ->
                                    preferenceManager.putBoolean(Constants.KEY_IS_SINGED_IN, true)
                                    preferenceManager.putString(Constants.KEY_USER_ID, it1.id)
                                    preferenceManager.putString(Constants.KEY_NAME, account.displayName)
                                    preferenceManager.putString(Constants.KEY_EMAIL, account.email)
                                    val intent = Intent(applicationContext, HomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { ite ->
                                    Toast.makeText(applicationContext,"New wale mein Error", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(applicationContext,"Error : " + ite.message, Toast.LENGTH_SHORT).show()
                                }
                        }
                    }else {
                        binding.progressBarEmail.visibility = View.INVISIBLE
                        binding.googleSignIn.visibility = View.VISIBLE
                        Toast.makeText(applicationContext,it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
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

}