package com.example.videomeet.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.example.videomeet.HomeFragments.ConnectFragment
import com.example.videomeet.HomeFragments.FriendsFragment
import com.example.videomeet.HomeFragments.NotificationFragment
import com.example.videomeet.HomeFragments.ProfileFragment
import com.example.videomeet.R
import com.example.videomeet.databinding.ActivityHomeBinding
import com.example.videomeet.databinding.ActivitySignInBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityHomeBinding
    private var currentFragmentChecker : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.bottomNavView.background = null
        binding.bottomNavView.menu.getItem(2).isEnabled = false
        replaceFragment(FriendsFragment())
        currentFragmentChecker = R.id.Home

        binding.bottomNavView.setOnItemSelectedListener {
            if(it.itemId != currentFragmentChecker){
                when(it.itemId){
                    R.id.Home -> {
                        replaceFragment(FriendsFragment())
                        currentFragmentChecker = R.id.Home
                    }
                    R.id.pings -> {
                        replaceFragment(NotificationFragment())
                        currentFragmentChecker = R.id.pings
                    }
                    R.id.connect -> {
                        replaceFragment(ConnectFragment())
                        currentFragmentChecker = R.id.connect
                    }
                    R.id.profile -> {
                        replaceFragment(ProfileFragment())
                        currentFragmentChecker = R.id.profile
                    }
                    else -> {

                    }
                }
            } else {
                //something if the fragment was same
            }
            true
        }



    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,fragment)
        fragmentTransaction.commit()
    }
}