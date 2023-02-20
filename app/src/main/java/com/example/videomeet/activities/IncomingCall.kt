package com.example.videomeet.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeet.R
import com.example.videomeet.databinding.ActivityIncomingCallBinding
import com.example.videomeet.databinding.ActivityMainBinding
import com.example.videomeet.network.APIClient
import com.example.videomeet.network.APIService
import com.example.videomeet.utilities.Constants
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.net.URL

class IncomingCall : AppCompatActivity() {
    private lateinit var binding : ActivityIncomingCallBinding
    var meetingType : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingCallBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        meetingType = intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE)
        if (meetingType != null){
            if (meetingType.equals("video")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_video)
                binding.incomingTextMeetingType.text = "Incoming Video Call"
            }else{
                binding.imageMeetingType.setImageResource(R.drawable.ic_phone)
                binding.incomingTextMeetingType.text = "Incoming Voice Call"
            }
        }

        var name = intent.getStringExtra(Constants.KEY_NAME)
        if (name != null){
            binding.incomingTextFirstChar.text = name.substring(0,1)
            binding.incomingUserName.text = name + " is calling"
        }

        var email = intent.getStringExtra(Constants.KEY_EMAIL)
        if (email != null){
            binding.incomingUseremail.text = email
        }

        binding.acceptCall.setOnClickListener {
            sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN).toString()
            )
        }

        binding.rejectCall.setOnClickListener {
            sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN).toString()
            )
        }

    }

    private fun sendInvitationResponse(type : String, receiverToken : String){
        try {
            var tokens : JSONArray = JSONArray()
            tokens.put(receiverToken)

            var body : JSONObject = JSONObject()
            var data : JSONObject = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type)

            body.put(Constants.REMOTE_MSG_DATA, data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(body.toString(), type)

        }catch (e : Exception){
            Toast.makeText(applicationContext,e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(remoteMessageBody : String, type : String) {
        APIClient.getClient().create(APIService::class.java).sendRemoteMessage(
            Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(object : retrofit2.Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful){
                    if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED){
                        try {
                            var serverURL : URL = URL("https://meet.jit.si")

                            var builder : JitsiMeetConferenceOptions.Builder = JitsiMeetConferenceOptions.Builder()
                            builder.setServerURL(serverURL)
                            builder.setRoom(intent.getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                            if (meetingType.equals("audio")){
                                builder.setVideoMuted(true)
                            }
                            JitsiMeetActivity.launch(applicationContext, builder.build())
                            finish()
                        }catch (e : Exception){
                            Toast.makeText(applicationContext,response.message(), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else{
                        Toast.makeText(applicationContext,"Invitation Rejected", Toast.LENGTH_SHORT).show()
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

    private val invitationResponseReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            var type = p1?.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            if (type != null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELED)){
                    Toast.makeText(applicationContext,"Invitation Cancelled", Toast.LENGTH_SHORT).show()
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

}