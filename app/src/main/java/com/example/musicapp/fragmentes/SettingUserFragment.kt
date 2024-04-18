package com.example.musicapp.fragmentes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.activities.AuthSelectionActivity
import com.example.musicapp.activities.EditProfileActivity
import com.example.musicapp.databinding.FragmentSettingUserBinding
import com.example.musicapp.viewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class SettingUserFragment : Fragment() {

    private lateinit var binding: FragmentSettingUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var userViewModel: UserViewModel

    private lateinit var firebaseUser: FirebaseUser

    private var emailUser = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        firebaseUser = firebaseAuth.currentUser!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingUserBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.backBtn.setOnClickListener {
            checkUser()
        }

        binding.profileSetting.setOnClickListener {
            startActivityForResult(
                Intent(requireContext(), EditProfileActivity::class.java),
                SettingAdminFragment.EDIT_PROFILE_REQUEST_CODE
            )
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finishAffinity()
                }
            })

        userViewModel.profileImage.observe(viewLifecycleOwner) { profileImage ->
            try {
                Glide.with(requireContext())
                    .load(profileImage)
                    .placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)

            } catch (e: Exception) {
                Log.d("loadUserInfo", "onDataChange: ${e.message}")
            }
        }

        userViewModel.name.observe(viewLifecycleOwner) { name ->
            binding.nameUser.text = name.toString()
        }

        userViewModel.email.observe(viewLifecycleOwner) { email ->
            binding.email.text = email.toString()
            emailUser = email.toString()
        }

        userViewModel.checkFingerprint.observe(viewLifecycleOwner) { checkFingerprint ->
            if (checkFingerprint != null) {
                binding.checkBox.isChecked = checkFingerprint
            }
        }

        binding.autoLogin.setOnClickListener { onRelativeLayoutClicked() }

        return binding.root
    }

    private fun onRelativeLayoutClicked() {
        var uidUser = ""
        userViewModel.uid.observe(viewLifecycleOwner) { uid ->
            uidUser = uid.toString()
        }
        val check = checkBiometricSupport()
        when (check) {
            1 -> {
                val checkBox = binding.checkBox
                checkBox.isChecked = !checkBox.isChecked
                if (checkBox.isChecked) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPreferences.edit().apply {
                        putBoolean("rememberMe", true)
                        putString("email", emailUser)
                        apply()
                    }
                    lifecycleScope.launch {
                        userViewModel.updateUserData(null, null, null, null, null, true)
                    }
                } else {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    sharedPreferences.edit().apply {
                        putBoolean("rememberMe", false)
                        remove("email")
                        apply()
                    }
                    lifecycleScope.launch {
                        userViewModel.updateUserData(null, null, null, null, null, false)
                    }
                }
            }

            2 -> {
                Toast.makeText(context, "Thiết bị không có cảm biến vân tay", Toast.LENGTH_SHORT)
                    .show()
            }

            3 -> {
                Toast.makeText(
                    context,
                    "Cảm biến vân tay không khả dụng ngay lúc này",
                    Toast.LENGTH_SHORT
                ).show()
            }

            4 -> {
                Toast.makeText(
                    context,
                    "Không có cảm biến vân tay nào hãy thêm nó",
                    Toast.LENGTH_SHORT
                ).show()
                Handler().postDelayed({
                    val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    startActivity(enrollIntent)
                }, 2000)
            }

            else -> {
                Toast.makeText(context, "Xảy ra lỗi! Xin thử lại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkBiometricSupport(): Int {
        val biometricManager = context?.let { BiometricManager.from(it) }
        if (biometricManager != null) {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    return 1;
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    return 2;
                }

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    return 3;
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                    startActivity(enrollIntent)
                    return 4;
                }
            }
        }
        return 0;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SettingAdminFragment.EDIT_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val updateImage = data?.getStringExtra("updatedImage")
            val updatedName = data?.getStringExtra("updatedName")
            if (!updatedName.isNullOrEmpty()) {
                binding.nameUser.text = updatedName
            } else if (!updateImage.isNullOrEmpty()) {
                Glide.with(this)
                    .load(updateImage)
                    .placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)
            }
        }
    }

    private fun checkUser() {
        firebaseAuth.signOut()
        val firebaseUser = firebaseAuth.currentUser
        val checkBox = binding.checkBox

        if (!checkBox.isChecked) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().apply {
                putBoolean("rememberMe", false)
                remove("email")
                remove("password")
                apply()
            }
        }
        if (firebaseUser == null) {
            startActivity(Intent(requireContext(), AuthSelectionActivity::class.java))
            requireActivity().finish()
        }
    }

}