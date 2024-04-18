package com.example.musicapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityAuthSelectionBinding
import com.example.musicapp.models.User
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AuthSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthSelectionBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var spinKitView: SpinKitView

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        spinKitView = binding.spinKit

        spinKitView.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.googleBtn.setOnClickListener {
            signIn()
        }

        binding.phoneBtn.setOnClickListener {
            startActivity(Intent(this, PhoneActivity::class.java))
        }

    }

    private var backPressedOnce = false
    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (!backPressedOnce) {
            Toast.makeText(this, "Lần nữa để thoát", Toast.LENGTH_SHORT).show()
            backPressedOnce = true
            Handler().postDelayed({ backPressedOnce = false }, 2000)
        } else {
            finishAffinity()
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                spinKitView.visibility = View.VISIBLE
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    checkIfUserExists(user)
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfUserExists(user: FirebaseUser?) {
        val uid = user?.uid

        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    spinKitView.visibility = View.GONE
                    val userImage = dataSnapshot.child("profileImage").value as? String
                    val intent = Intent(this@AuthSelectionActivity, MainActivity::class.java)
                    intent.putExtra("userImage", userImage)
                    startActivity(intent)
                } else {
                    spinKitView.visibility = View.GONE
                    addUserFireBase(user)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("TAG", "onCancelled: ${databaseError.message}")
            }
        })
    }

    private fun addUserFireBase(user: com.google.firebase.auth.FirebaseUser?) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val currentDate = "$dayOfMonth/${month + 1}/$year"

        val timestamp = System.currentTimeMillis()
        val user = User(
            uid = user?.uid.toString(),
            email = user?.email.toString(),
            name = user?.email.toString(),
            profileImage = user?.photoUrl.toString(),
            userType = "user",
            timestamp = timestamp,
            birthday = currentDate,
            checkFingerprint = false
        )

        val intent = Intent(this, SingerListActivity::class.java)
        intent.putExtra("user", user)
        intent.putExtra("type", false)
        startActivity(intent)
    }

}