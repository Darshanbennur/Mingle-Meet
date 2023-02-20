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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.bottomNavView.background = null
        binding.bottomNavView.menu.getItem(2).isEnabled = false
        replaceFragment(FriendsFragment())

        binding.bottomNavView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Home -> replaceFragment(FriendsFragment())
                R.id.pings -> replaceFragment(NotificationFragment())
                R.id.connect -> replaceFragment(ConnectFragment())
                R.id.profile -> replaceFragment(ProfileFragment())

                else -> {

                }
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