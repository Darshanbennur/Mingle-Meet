package com.example.videomeet.HomeFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.text.TextDirectionHeuristicsCompat.LOCALE
import androidx.recyclerview.widget.RecyclerView
import com.example.videomeet.activities.OutgoingCall
import com.example.videomeet.adapter.UserAdapter
import com.example.videomeet.databinding.FragmentFriendsBinding
import com.example.videomeet.listeners.UserListeners
import com.example.videomeet.models.User
import com.example.videomeet.utilities.Constants
import com.example.videomeet.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.messaging.FirebaseMessaging

class FriendsFragment : Fragment(), UserListeners {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    lateinit var filteredList : ArrayList<User>

//    var REQUEST_CODE_BATTERY_OPTIMIZATION : Int = 1

    private lateinit var preferenceManager : PreferenceManager
    private lateinit var userList : ArrayList<User>
    private lateinit var userAdapter : UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(activity)

        binding.title.text = "Hey, " + preferenceManager.getString(Constants.KEY_NAME)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                sendFCMTokenToDatabase(it.result)
            }
        }
        filteredList = ArrayList()

        binding.searchView.clearFocus()
        binding.searchView.queryHint = "Search Here ..."

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filterList(p0)
                return true
            }

        })

        val userRecycler : RecyclerView = binding.recyclerView

        userList = ArrayList()
        getUsers()
        userAdapter = UserAdapter(userList, this)
        binding.recyclerView.adapter = userAdapter
        userAdapter.notifyDataSetChanged()

        binding.swipeRefresh.setOnRefreshListener(this::getUsers)
//        checkForBatteryOptimization()

        return binding.root
    }

    private fun onlineStatus(){

    }

    private fun filterList(query: String?) {
        filteredList.clear()
        if (query != null){
            for (i in userList){
                if (i.Name.toLowerCase().contains(query)){
                    filteredList.add(i)
                }
            }
        }
        if (filteredList.isEmpty()){
            filteredList.clear()
            userAdapter = UserAdapter(filteredList, this)
            binding.recyclerView.adapter = userAdapter

            binding.errorMessage.text = "No Users available"
            binding.errorMessage.visibility = View.VISIBLE
            binding.animater.visibility = View.VISIBLE
        }else{
            userAdapter = UserAdapter(filteredList, this)
            binding.recyclerView.adapter = userAdapter
            binding.errorMessage.text = "No Users available"
            binding.errorMessage.visibility = View.GONE
            binding.animater.visibility = View.GONE
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
                        user.isOnline = !(user!!.Token == null || user.Token.trim().isEmpty())
                        userList.add(user)
                    }
                    if (userList.size > 0)
                        userAdapter.notifyDataSetChanged()
                    else{
                        binding.errorMessage.text = "No Users available"
                        binding.errorMessage.visibility = View.VISIBLE
                        binding.animater.visibility = View.VISIBLE
                    }
                } else{
                    binding.errorMessage.text = "No Users available"
                    binding.errorMessage.visibility = View.VISIBLE
                    binding.animater.visibility = View.VISIBLE
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
                Toast.makeText(activity,"Unable to Send Token : " + it.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initiateVideoMeeting(user: User?) {
        if (user!!.Token == null || user.Token.trim().isEmpty()) {
            Toast.makeText(
                activity,user.Name + " is offline",
                Toast.LENGTH_SHORT
            ).show()
        }else {
            var intent = Intent(activity, OutgoingCall::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "video")
            startActivity(intent)
        }
    }

    override fun initiateAudioMeeting(user: User?) {
        if (user!!.Token == null || user.Token.trim().isEmpty()) {
            Toast.makeText(
                activity,user.Name + " is offline",
                Toast.LENGTH_SHORT
            ).show()
        }else {
            var intent = Intent(activity, OutgoingCall::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type", "audio")
            startActivity(intent)
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION){
//            checkForBatteryOptimization()
//        }
//    }

}