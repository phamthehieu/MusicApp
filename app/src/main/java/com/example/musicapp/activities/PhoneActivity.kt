package com.example.musicapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityPhoneBinding
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var number: String

    private lateinit var spinKitView: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        spinKitView = binding.spinKit

        spinKitView.visibility = View.GONE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding.sendOTPBtn.setOnClickListener {
            number = binding.phoneEditTextNumber.text.toString()
            spinKitView.visibility = View.VISIBLE
            if (number.isNotEmpty()) {
                if (number.length == 10) {
                    if (number.first() == '0') {
                        number = number.dropWhile { it == '0' }
                        number = "+84$number"
                    }
                    val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } else {
                    spinKitView.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Số điện thoại không hợp lệ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                spinKitView.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Vui lòng nhập số điện thoại",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {

            if (e is FirebaseAuthInvalidCredentialsException) {
                spinKitView.visibility = View.GONE
                Toast.makeText(
                    this@PhoneActivity,
                    "Gửi mã xác minh bị lỗi xin thử lại sau",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (e is FirebaseTooManyRequestsException) {
                spinKitView.visibility = View.GONE
                Toast.makeText(
                    this@PhoneActivity,
                    "Gửi mã xác minh bị lỗi xin thử lại sau",
                    Toast.LENGTH_SHORT
                ).show()
            }
            spinKitView.visibility = View.GONE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            val intent = Intent(this@PhoneActivity , OTPActivity::class.java)
            intent.putExtra("OTP" , verificationId)
            intent.putExtra("resendToken" , token)
            intent.putExtra("phoneNumber" , number)
            startActivity(intent)
            spinKitView.visibility = View.GONE
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendToMain()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this , "Mã xác minh đã nhập không hợp lệ. Xin thửu lại!" , Toast.LENGTH_SHORT).show()
                    }
                }
                spinKitView.visibility = View.GONE
            }
    }

    private fun sendToMain(){
        startActivity(Intent(this , MainActivity::class.java))
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null){
            startActivity(Intent(this , MainActivity::class.java))
        }
    }

}