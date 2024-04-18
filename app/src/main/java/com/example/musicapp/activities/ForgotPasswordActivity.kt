package com.example.musicapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    private var emailTx = ""

    private lateinit var emailEt: EditText
    private lateinit var forgotPassBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ")
        progressDialog.setCanceledOnTouchOutside(false)

        emailEt = binding.emailEt
        forgotPassBtn = binding.forgotPassBtn
        forgotPassBtn.isEnabled = false

        if (intent.hasExtra("emailTx")) {
            emailTx = intent.getStringExtra("emailTx")!!
            Log.d("Hieu57", emailTx.toString())
            if (emailTx.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailTx).matches()) {
                forgotPassBtn.isEnabled = true
            }
        } else ("")

        binding.emailEt.setText(emailTx)

        emailEt.addTextChangedListener(textWatcherEmail)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.forgotPassBtn.setOnClickListener {
            recoverPassword()
        }
    }

    private var email = ""
    private fun recoverPassword() {
        email = binding.emailEt.text.toString()
        progressDialog.setMessage("Thông tin cập nhật mật khẩu đã gửi đến email ${email}")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Thông tin đã được gửi thành công", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error forgot password ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private val textWatcherEmail = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val email = emailEt.text.toString()

            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.checkEmail.text = ""
                binding.checkEmail.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        android.R.color.transparent
                    )
                )
                forgotPassBtn.isEnabled = true
            } else {
                binding.checkEmail.text = "Email liên kết không hợp lệ"
                binding.checkEmail.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.red
                    )
                )
            }
        }
    }

}