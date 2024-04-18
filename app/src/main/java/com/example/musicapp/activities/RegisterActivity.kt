package com.example.musicapp.activities

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.musicapp.MyApplication
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var selectedDateEt: TextView
    private lateinit var registerBtn: Button

    private var emailTx = ""
    private var password = ""
    private var calendarTx = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            finish()
        }

        emailEt = binding.emailEt
        passwordEt = binding.passwordEt
        registerBtn = binding.registerBtn
        selectedDateEt = binding.selectedDateEt
        registerBtn.isEnabled = false

        emailEt.addTextChangedListener(textWatcherEmail)
        passwordEt.addTextChangedListener(textWatcherPassword)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val currentDate = "$dayOfMonth/${month + 1}/$year"
        selectedDateEt.text = currentDate

        selectedDateEt.setOnClickListener {
            MyApplication.showCalendar(this, { selectedDate, _ ->
                if (selectedDate.isNotEmpty()) {
                    selectedDateEt.text = selectedDate
                    calendarTx = selectedDate
                    binding.checkDate.text = ""
                    registerBtn.isEnabled = true
                } else {
                    Toast.makeText(this, "Bạn phải lớn hơn 18 tuổi", Toast.LENGTH_SHORT).show()
                }
            }, "")
        }

        binding.registerBtn.setOnClickListener {
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        emailTx = binding.emailEt.text.toString()
        password = binding.passwordEt.text.toString()

        progressDialog.setMessage("Đang tạo tài khoản...")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(emailTx, password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed creating account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setTitle("Lưu thông tin tài khoản...")

        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = emailTx
        hashMap["name"] = uid
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["phoneNumber"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["birthday"] = calendarTx
        hashMap["checkFingerprint"] = false

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Tạo tài khoản thành công...",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("emailTx", emailTx)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed saving user info due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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