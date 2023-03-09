package com.example.videomeet.HomeFragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.videomeet.activities.SignInActivity
import com.example.videomeet.databinding.FragmentProfileBinding
import com.example.videomeet.utilities.Constants
import com.example.videomeet.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var alert : AlertDialog.Builder
    private lateinit var preferenceManager : PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        alert = AlertDialog.Builder(requireActivity())

        preferenceManager = PreferenceManager(activity)
        binding.logout.setOnClickListener {
            alert.setTitle("Are you Sure?")
                .setMessage("Are you Sure You want to Logout")
                .setCancelable(true)
                .setPositiveButton("Yes"){ _: DialogInterface, _: Int ->
                    FirebaseAuth.getInstance().signOut()
                    val database : FirebaseFirestore = FirebaseFirestore.getInstance()
                    val documentRef : DocumentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    val decision : HashMap<String, Any> = HashMap()
                    decision[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
                    documentRef.update(decision)
                        .addOnSuccessListener {
                            preferenceManager.clearPreferences()
                            val intent = Intent(activity, SignInActivity::class.java)
                            startActivity(intent)
                            activity!!.finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity,"Unable to Logout : " + it.message, Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("No"){ dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.cancel()
                }
                .show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}