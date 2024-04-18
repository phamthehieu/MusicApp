package com.example.musicapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivityOtpactivityBinding
import com.example.musicapp.models.User
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String

    private lateinit var spinKitView: SpinKitView

    val countDownTimeInMillis: Long = 60000

    private val countDownTimer = object : CountDownTimer(countDownTimeInMillis, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // Hiển thị số giây còn lại
            val secondsRemaining = millisUntilFinished / 1000
            binding.resendTextView.text = "Gửi lại sau $secondsRemaining giây"

            binding.resendTextView.isEnabled = false
            binding.resendTextView.isClickable = false
        }

        override fun onFinish() {
            binding.resendTextView.text = "Gửi lại mã xác nhận"
            binding.resendTextView.isEnabled = true
            binding.resendTextView.isClickable = true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        spinKitView = binding.spinKit

        spinKitView.visibility = View.GONE
        binding.verifyOTPBtn.setOnClickListener {
            val typedOTP = (binding.otpEditText1.text.toString() + binding.otpEditText2.text.toString() +
                    binding.otpEditText3.text.toString() + binding.otpEditText4.text.toString() +
                    binding.otpEditText5.text.toString() + binding.otpEditText6.text.toString())

            if (typedOTP.isNotEmpty()) {
                if (typedOTP.length == 6) {
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP, typedOTP
                    )
                    spinKitView.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Toast.makeText(this, "Vui lòng nhập đúng OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resendTextView.setOnClickListener {
            resendVerificationCode()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        countDownTimer.start()

        initializeCallbacks()
        addTextChangeListener()
        resendOTPTvVisibility()
    }

    private fun resendOTPTvVisibility() {
        binding.apply {
            otpEditText1.setText("")
            otpEditText2.setText("")
            otpEditText3.setText("")
            otpEditText4.setText("")
            otpEditText5.setText("")
            otpEditText6.setText("")
            resendTextView.visibility = View.INVISIBLE
            resendTextView.isEnabled = false

            Handler(Looper.myLooper()!!).postDelayed({
                resendTextView.visibility = View.VISIBLE
                resendTextView.isEnabled = true
            }, 60000)
        }
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        countDownTimer.start()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendToMain()
                } else {
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private fun sendToMain() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val currentDate = "$dayOfMonth/${month + 1}/$year"

        val timestamp = System.currentTimeMillis()
        val uid = auth.uid

        val user = User(
            uid = uid.toString(),
            email = "",
            name = phoneNumber,
            profileImage = "",
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

    private fun addTextChangeListener() {
        val editTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val text = p0.toString()
                when (binding.root.focusedChild?.id) {
                    binding.otpEditText1.id -> if (text.length == 1) binding.otpEditText2.requestFocus()
                    binding.otpEditText2.id -> if (text.length == 1) binding.otpEditText3.requestFocus() else if (text.isEmpty()) binding.otpEditText1.requestFocus()
                    binding.otpEditText3.id -> if (text.length == 1) binding.otpEditText4.requestFocus() else if (text.isEmpty()) binding.otpEditText2.requestFocus()
                    binding.otpEditText4.id -> if (text.length == 1) binding.otpEditText5.requestFocus() else if (text.isEmpty()) binding.otpEditText3.requestFocus()
                    binding.otpEditText5.id -> if (text.length == 1) binding.otpEditText6.requestFocus() else if (text.isEmpty()) binding.otpEditText4.requestFocus()
                    binding.otpEditText6.id -> if (text.isEmpty()) binding.otpEditText5.requestFocus()
                }
            }
        }

        binding.apply {
            otpEditText1.addTextChangedListener(editTextWatcher)
            otpEditText2.addTextChangedListener(editTextWatcher)
            otpEditText3.addTextChangedListener(editTextWatcher)
            otpEditText4.addTextChangedListener(editTextWatcher)
            otpEditText5.addTextChangedListener(editTextWatcher)
            otpEditText6.addTextChangedListener(editTextWatcher)
        }
    }

    private fun initializeCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.d("TAG", "onVerificationFailed: ${e.toString()}")
                }
                spinKitView.visibility = View.VISIBLE
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                OTP = verificationId
                resendToken = token
            }
        }
    }
}
