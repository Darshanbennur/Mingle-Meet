package com.example.videomeet.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.OnReceiveContentListener
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeet.R
import com.example.videomeet.databinding.ActivityIncomingCallBinding
import com.example.videomeet.databinding.ActivityOutgoingCallBinding
import com.example.videomeet.models.User
import com.example.videomeet.network.APIClient
import com.example.videomeet.network.APIService
import com.example.videomeet.utilities.Constants
import com.example.videomeet.utilities.PreferenceManager
import com.google.firebase.messaging.FirebaseMessaging
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.Serializable
import java.net.URL
import java.util.*
import javax.security.auth.callback.Callback

class OutgoingCall : AppCompatActivity() {
    private lateinit var binding : ActivityOutgoingCallBinding
    private lateinit var preferenceManager : PreferenceManager
    var inviterToken: String? = null
    var meetingRoom: String? = null
    var meetingType : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutgoingCallBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        meetingType = intent.getStringExtra("type")
        if(meetingType != null){
            if (meetingType == "video"){
                binding.imageMeetingType.setImageResource(R.drawable.ic_video)
                binding.outgoingTextMeetingType.text = "Video Call Outgoing"
            } else{
                binding.imageMeetingType.setImageResource(R.drawable.ic_phone)
                binding.outgoingTextMeetingType.text = "Audio Call Outgoing"
            }
        }

        var user = intent.getSerializableExtra("user") as User
        if (user != null){
            binding.outgoingTextFirstChar.text = user.Name.substring(0,1)
            binding.outgoingUserName.text = user.Name
            binding.outgoingUseremail.text = user.Email
        }

        binding.rejectCall.setOnClickListener {
            if (user != null) {
                cancelInvitation(user.Token)
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful && it.result != null){
                inviterToken = it.result
                if (meetingType != null && user != null){
                    initiateMeeting(meetingType!!, user.Token)
                }
            }
        }
    }

    private fun initiateMeeting(meetingType : String, receiverToken : String) {
        try {
            var tokens : JSONArray = JSONArray()
            tokens.put(receiverToken)

            var body : JSONObject = JSONObject()
            var data : JSONObject = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION)
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType)
            data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL))
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken)

            meetingRoom =
                preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                        UUID.randomUUID().toString().substring(0,5)
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION)

        }catch (e : java.lang.Exception){
            Toast.makeText(applicationContext,e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(remoteMessageBody : String, type : String) {
        APIClient.getClient().create(APIService::class.java).sendRemoteMessage(
            Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(object : retrofit2.Callback<String>{
            override fun onResponse( call: Call<String>, response: Response<String>) {
                if (response.isSuccessful){
                    if (type == Constants.REMOTE_MSG_INVITATION){
                        Toast.makeText(applicationContext,"Invitation Sent Successfully", Toast.LENGTH_SHORT).show()
                    } else if(type == Constants.REMOTE_MSG_INVITATION_RESPONSE){
                        Toast.makeText(applicationContext,"Invitation Cancelled", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else{
                    Toast.makeText(applicationContext,response.message(), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(applicationContext,t.message, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun cancelInvitation(receiverToken : String){
        try {
            var tokens : JSONArray = JSONArray()
            tokens.put(receiverToken)

            var body : JSONObject = JSONObject()
            var data : JSONObject = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELED)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE)

        }catch (e : Exception){
            Toast.makeText(applicationContext,e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

     private val invitationResponseReceiver : BroadcastReceiver = object : BroadcastReceiver() {
         override fun onReceive(p0: Context?, p1: Intent?) {
             var type = p1?.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
             if (type != null){
                 if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED){
                     try {
                         var serverURL : URL = URL("https://meet.jit.si")
                         var builder : JitsiMeetConferenceOptions.Builder = JitsiMeetConferenceOptions.Builder()
                         builder.setServerURL(serverURL)
                         builder.setRoom(meetingRoom)
                         if (meetingType.equals("audio")){
                             builder.setVideoMuted(true)
                         }
                         JitsiMeetActivity.launch(applicationContext, builder.build())
                         finish()
                     }catch (e : Exception){
                         Toast.makeText(applicationContext,e.message, Toast.LENGTH_SHORT).show()
                         finish()
                     }
                 } else if (type == Constants.REMOTE_MSG_INVITATION_REJECTED){
                     Toast.makeText(applicationContext,"Invitation Rejected", Toast.LENGTH_SHORT).show()
                     finish()
                 }
             }
         }
     }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseReceiver
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}