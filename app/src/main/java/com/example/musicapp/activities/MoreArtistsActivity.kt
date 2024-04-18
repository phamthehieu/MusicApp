package com.example.musicapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMoreArtistsBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class MoreArtistsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoreArtistsBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUrl: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreArtistsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.imageArtists.setOnClickListener {
            pickImageGallery()
        }

        binding.registerArtistsBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var id = ""

    private fun validateData() {
        name = binding.nameArtistsEt.text.toString()
        id = binding.idArtistsEt.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(this, "Tên nghệ sĩ không được để trống...", Toast.LENGTH_SHORT).show()
        } else if (id.isEmpty()) {
            Toast.makeText(this, "Id nghệ sĩ không được để trống...", Toast.LENGTH_SHORT).show()
        } else if (imageUrl != null) {
            updateImage()
        } else {
            updateProfile("")
        }
    }

    private fun updateImage() {
        progressDialog.setTitle("Lưu ảnh nghệ sĩ...")
        progressDialog.show()

        val filePathAndName = "ProfileImage/" + firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUrl!!)
            .addOnSuccessListener { taskSnapshot ->

                progressDialog.dismiss()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to upload image due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setTitle("Lưu thông tin nghệ sĩ...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any> = HashMap()

        hashMap["name"] = "$name"
        hashMap["idArtists"] = "$id"
        hashMap["uid"] = "$uid"
        hashMap["timestamp"] = "$timestamp"
        hashMap["profileImage"] = uploadedImageUrl

        val reference = FirebaseDatabase.getInstance().getReference("Artists")
        reference.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Thêm nghệ sĩ thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed to update profile due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun pickImageGallery() {
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
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

}