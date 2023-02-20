package com.example.videomeet.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.videomeet.R
import com.example.videomeet.adapter.UserAdapter
import com.example.videomeet.databinding.ActivityMainBinding
import com.example.videomeet.databinding.ActivitySignInBinding
import com.example.videomeet.listeners.UserListeners
import com.example.videomeet.models.User
import com.example.videomeet.utilities.Constants
import com.example.videomeet.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(), UserListeners {
    private lateinit var binding : ActivityMainBinding
    private lateinit var preferenceManager : PreferenceManager
    private lateinit var alert : AlertDialog.Builder
    private lateinit var userList : ArrayList<User>
    private lateinit var userAdapter : UserAdapter

    var REQUEST_CODE_BATTERY_OPTIMIZATION : Int = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        alert = AlertDialog.Builder(this)

        binding.title.text = "Hello, " + preferenceManager.getString(Constants.KEY_NAME)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                sendFCMTokenToDatabase(it.result)
            }
        }

        val userRecycler : RecyclerView = binding.recyclerView

        userList = ArrayList()
        userAdapter = UserAdapter(userList, this)
        binding.recyclerView.adapter = userAdapter

        binding.swipeRefresh.setOnRefreshListener(this::getUsers)

        getUsers()
        checkForBatteryOptimization()

        binding.signOut.setOnClickListener {
            alert.setTitle("Are you Sure?")
                .setMessage("Are you Sure You want to Logout")
                .setCancelable(true)
                .setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                    val database : FirebaseFirestore = FirebaseFirestore.getInstance()
                    val documentRef : DocumentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                    )
                    var decision : HashMap<String, Any> = HashMap()
                    decision[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
                    documentRef.update(decision)
                        .addOnSuccessListener {
                            preferenceManager.clearPreferences()
                            var intent = Intent(applicationContext, SignInActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(applicationContext,"Unable to Logout : " + it.message, Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("No"){ dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.cancel()
                }
                .show()
        }

    }

    private fun getUsers(){
        binding.swipeRefresh.isRefreshing = true
        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                binding.swipeRefresh.isRefreshing = false
                var myUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if (it.isSuccessful && it.result != null){
                    userList.clear()
                    for (documentSnap : QueryDocumentSnapshot in it.result){
                        if (myUserId.equals(documentSnap.id)){
                            continue
                        }
                        var user = User()
                        user.Name = documentSnap.getString(Constants.KEY_NAME)
                        user.Email = documentSnap.getString(Constants.KEY_EMAIL)
                        user.Token = documentSnap.getString(Constants.KEY_FCM_TOKEN)
                        userList.add(user)
                    }
                    if (userList.size > 0)
                        userAdapter.notifyDataSetChanged()
                    else{
                        binding.errorMessage.text = "No Users available"
                        binding.errorMessage.visibility = View.VISIBLE
                    }
                } else{
                    binding.errorMessage.text = "No Users available"
                    binding.errorMessage.visibility = View.VISIBLE
                }
            }
    }

    private fun sendFCMTokenToDatabase(token : String){
        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentRef : DocumentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        documentRef.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                Toast.makeText(applicationContext,"Unable to Send Token : " + it.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun initiateVideoMeeting(user: User?) {
        if (user!!.Token == null || user.Token.trim().isEmpty()) {
            Toast.makeText(
                applicationContext,user.Name + " is not available for meeting",
                Toast.LENGTH_SHORT
            ).show()
        }else {
            var intent = Intent(applicationContext, OutgoingCall::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "video")
            startActivity(intent)
        }
    }

    override fun initiateAudioMeeting(user: User?) {
        if (user!!.Token == null || user.Token.trim().isEmpty()) {
            Toast.makeText(
                applicationContext,user.Name + " is not available for meeting",
                Toast.LENGTH_SHORT
            ).show()
        }else {
            var intent = Intent(applicationContext, OutgoingCall::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "audio")
            startActivity(intent)
        }
    }

    private fun checkForBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            var powerManager : PowerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)){
                var builder : AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Warning")
                builder.setMessage("Battery Optimization is enabled. It can interrupt running Background Services.")
                builder.setPositiveButton("Disable", DialogInterface.OnClickListener { _, _ ->
                    var intent : Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION)
                })
                builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, _ ->
                    dialogInterface.dismiss()
                })
                builder.create().show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION){
            checkForBatteryOptimization()
        }
    }

}