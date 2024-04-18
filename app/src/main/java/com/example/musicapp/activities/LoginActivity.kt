package com.example.musicapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.databinding.ActivityLoginBinding
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.musicapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.provider.Settings
import android.view.View
import java.util.concurrent.Executor
import java.util.regex.Pattern
import kotlin.math.log

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var emailTx = ""
    private var passwordTx = ""

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val rememberMe = sharedPreferences.getBoolean("rememberMe", false)
        var savedEmail = ""
        if (rememberMe) {
            savedEmail = sharedPreferences.getString("email", "").toString()
            binding.emailEt.setText(savedEmail)
        }
        if (intent.hasExtra("emailTx")) {
            savedEmail = intent.hasExtra("emailTx").toString()
        }

        binding.emailEt.setText(savedEmail)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ...")
        progressDialog.setCanceledOnTouchOutside(false)

        emailEt = binding.emailEt
        passwordEt = binding.passwordEt
        loginBtn = binding.loginBtn
        loginBtn.isEnabled = false

        emailEt.addTextChangedListener(textWatcherEmail)
        passwordEt.addTextChangedListener(textWatcherPassword)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.loginBtn.setOnClickListener {
            loginUser()
        }

        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            intent.putExtra("emailTx", binding.emailEt.text.toString())
            startActivity(intent)
        }

        binding.loginFingerprint.setOnClickListener {
            loginWithFingerprint()
        }

    }

    private fun loginWithFingerprint() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Xác thực vân tay")
            .setSubtitle("Sử dụng vân tay để đăng nhập")
            .setNegativeButtonText("Hủy")
            .build()

        val biometricPrompt = BiometricPrompt(this, Executor { it.run() },
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    loginFingerprint()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        this@LoginActivity,
                        "Dấu vân tay không hợp lệ xin thử lại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    private fun loginFingerprint() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val savedEmail = sharedPreferences.getString("email", "").toString()
        val passwordUser = sharedPreferences.getString("password", "").toString()

        if (binding.emailEt.text.toString() != savedEmail && passwordUser == "") {
            Toast.makeText(
                this,
                "Tài khoản không tích hợp vân tay",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            firebaseAuth.signInWithEmailAndPassword(savedEmail, passwordUser)
                .addOnSuccessListener {
                    checkUser()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Login failed due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun loginUser() {
        progressDialog.setMessage("Đăng nhập...")
        progressDialog.show()

        emailTx = binding.emailEt.text.toString()
        passwordTx = binding.passwordEt.text.toString()

        firebaseAuth.signInWithEmailAndPassword(emailTx, passwordTx)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Login failed due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser!!
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val email = sharedPreferences.getString("email", "").toString()

        if (binding.passwordEt.text.toString() != "") {
            sharedPreferences.edit().apply {
                putString("password", binding.passwordEt.text.toString())
                apply()
            }
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    val userImage = snapshot.child("profileImage").value as? String
                    val emailUser = snapshot.child("email").value as? String
                    val checkFingerprint = snapshot.child("checkFingerprint").value as? Boolean
                    if (email != emailUser && checkFingerprint == true) {
                        sharedPreferences.edit().apply {
                            putString("email", emailUser)
                            putString("password", binding.passwordEt.text.toString())
                            apply()
                        }
                    }
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    if (userImage != null) {
                        intent.putExtra("userImage", userImage)
                    } else if (emailUser != null) {
                        intent.putExtra("email", emailUser)
                    }
                    this@LoginActivity.startActivity(intent) // Start activity using LoginActivity context
                    progressDialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private val textWatcherEmail = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val email = emailEt.text.toString()

            val isEmailLengthValid = email.length >= 10

            if (isEmailLengthValid && email.isNotEmpty()) {
                binding.checkUser.text = ""
                binding.checkUser.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        android.R.color.transparent
                    )
                )

            } else {
                binding.checkUser.text = "Tài khoản cần ít nhất 10 ký tự"
                binding.checkUser.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.red
                    )
                )
            }
        }
    }

    private val textWatcherPassword = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val password = passwordEt.text.toString()

            val isPasswordValid = isPasswordValid(password)

            if (isPasswordValid && password.isNotEmpty()) {
                binding.checkPassword.text = ""
                binding.checkUser.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        android.R.color.transparent
                    )
                )
                loginBtn.isEnabled = true

            } else {
                binding.checkPassword.text =
                    "Mật khẩu phải có ít nhất 10 ký tự. Bạn nên dùng ít nhất 1 số hoặc 1 ký tự đặc biệt"
                binding.checkPassword.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.red
                    )
                )
            }

        }
    }

    private fun isPasswordValid(password: String): Boolean {
        // Kiểm tra mật khẩu có ít nhất 10 ký tự và chứa ít nhất 1 chữ cái và 1 số
        val pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{10,}\$")
        return pattern.matcher(password).matches()
    }

}