package com.example.musicapp.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.musicapp.fragmentes.HomeFragment
import com.example.musicapp.fragmentes.LibrariesFragment
import com.example.musicapp.R
import com.example.musicapp.fragmentes.SearchesFragment
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.fragmentes.SettingAdminFragment
import com.example.musicapp.fragmentes.SettingUserFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseUser: FirebaseUser

    private var userImage = ""
    private var userEmail = ""

    private lateinit var check: Fragment

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        firebaseUser = firebaseAuth.currentUser!!

        userImage = if (intent.hasExtra("userImage")) {
            intent.getStringExtra("userImage")!!
        } else ("")

        userEmail = if (intent.hasExtra("email")) {
            intent.getStringExtra("email")!!
        } else ("")

        replaceFragment(HomeFragment())

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("userType").value
                    Log.d("hieu1111", userType.toString())
                    if (userType == "user") {
                        check = SettingUserFragment()
                    } else if (userType == "admin") {
                        check = SettingAdminFragment()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.search -> replaceFragment(SearchesFragment())
                R.id.libraries -> replaceFragment(LibrariesFragment())
                R.id.setting -> replaceFragment(check)
            }
            true
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.getString("emailUser", userEmail)
    }

    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("userImage", userImage)
        fragment.arguments = bundle

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}