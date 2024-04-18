package com.example.musicapp.activities

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.musicapp.MyApplication
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityEditProfileBinding
import com.example.musicapp.viewModel.UserViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.log

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var spinKitView: RelativeLayout

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var userViewModel: UserViewModel

    private var imageUrl: Uri? = null
    private var nameUser = ""
    private var birthdayEt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ...")
        progressDialog.setCanceledOnTouchOutside(false)

        userViewModel.name.observe(this) { name ->
            binding.nameUser.text = name.toString()
            nameUser = name.toString()
        }

        userViewModel.email.observe(this) { email ->
            binding.emailEt.text = email.toString()
        }

        userViewModel.birthday.observe(this) { birthday ->
            binding.birthdayEt.text = birthday.toString()
            birthdayEt = birthday.toString()
        }

        userViewModel.profileImage.observe(this) { profileImage ->
            try {
                Glide.with(this@EditProfileActivity)
                    .load(profileImage)
                    .placeholder(R.drawable.ic_person_gray)
                    .into(binding.imageArtists)

            } catch (e: Exception) {
                Log.d("loadUserInfo", "onDataChange: ${e.message}")
            }
        }

        spinKitView = binding.loading

        spinKitView.visibility = View.GONE

        binding.imageArtists.setOnClickListener {
            showImageAttachMenu()
        }

        binding.editUserName.setOnClickListener {
            showDialog()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.editBirthDay.setOnClickListener {
            MyApplication.showCalendar(this, { selectedDate, _ ->
               if (selectedDate.isNotEmpty()) {
                   spinKitView.visibility = View.VISIBLE
                   lifecycleScope.launch {
                       userViewModel.updateUserData(null, null, selectedDate, null, null, null, null)
                   }
                   binding.birthdayEt.text = selectedDate
                   checkUpdate("birthDay")
               } else {
                   Toast.makeText(this, "Bạn phải lớn hơn 18 tuổi", Toast.LENGTH_SHORT).show()
               }
            }, birthdayEt)
        }

    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_dialog_edit_username)

        val confirmButton = dialog.findViewById<Button>(R.id.confirm)
        val userNameEt = dialog.findViewById<EditText>(R.id.userNameEt)
        val cancelDialogButton = dialog.findViewById<Button>(R.id.cancelDialog)

        userNameEt.setText(nameUser)

        confirmButton.setOnClickListener {
            nameUser = userNameEt.text.toString()
            binding.nameUser.text = nameUser
            spinKitView.visibility = View.VISIBLE
            lifecycleScope.launch {
                userViewModel.updateUserData(nameUser, null, null, null, null, null, null)
            }
            checkUpdate("name")
            dialog.dismiss()
        }

        cancelDialogButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showImageAttachMenu() {
        val popupMenu = PopupMenu(this, binding.imageArtists)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 0, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if (id == 0) {
                pickImageCamera()
            } else if (id == 1) {
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageCamera() {
        spinKitView.visibility = View.VISIBLE
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUrl = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        spinKitView.visibility = View.VISIBLE
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUrl = data!!.data

                binding.imageArtists.setImageURI(imageUrl)
                upDateImageDataBase()
            } else {
                spinKitView.visibility = View.GONE
                Toast.makeText(this, "Hủy", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                binding.imageArtists.setImageURI(imageUrl)
                upDateImageDataBase()
            } else {
                spinKitView.visibility = View.GONE
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private var imageUpdate = ""
    private fun upDateImageDataBase() {
        val filePathAndName = "ProfileImage/" + firebaseAuth.uid
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUrl!!)
            .addOnSuccessListener { taskSnapshot ->
                progressDialog.dismiss()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                lifecycleScope.launch {
                    userViewModel.updateUserData(null, null, null, uploadedImageUrl, null, null, null)
                }
                imageUpdate = uploadedImageUrl
                checkUpdate("image")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                spinKitView.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Failed to upload image due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkUpdate(value: String) {
        userViewModel.updateResult.observe(this) { isSuccess ->
            if (isSuccess) {
                val resultIntent = Intent()
                if (value == "name") {
                    resultIntent.putExtra("updatedName", nameUser)
                } else if (value == "image" && imageUpdate != "") {
                    resultIntent.putExtra("updatedImage", imageUpdate)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                spinKitView.visibility = View.GONE
                Toast.makeText(this, "Cập nhật dữ liệu thành công", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Cập nhật dữ liệu thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

}