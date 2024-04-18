package com.example.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivitySplashBinding
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var spinKitView: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        spinKitView = binding.spinKit

        spinKitView.visibility = View.VISIBLE

        Handler().postDelayed({
//            checkUser()
            startActivity(Intent(this, SingerListActivity::class.java))
        }, 2000)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, AuthSelectionActivity::class.java))
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val profileImage = snapshot.child("profileImage").value
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        intent.putExtra("userImage", profileImage.toString())
                        startActivity(intent)
                        spinKitView.visibility = View.GONE
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}