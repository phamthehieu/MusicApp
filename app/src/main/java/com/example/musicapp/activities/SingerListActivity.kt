package com.example.musicapp.activities

<<<<<<< HEAD
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.adapters.ArtistsAdapters
import com.example.musicapp.adapters.OnSelectedArtistsIdsListener
import com.example.musicapp.databinding.ActivitySingerListBinding
import com.example.musicapp.models.ArtistsModel
import com.example.musicapp.models.User
=======
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicapp.adapters.ArtistsAdapters
import com.example.musicapp.databinding.ActivitySingerListBinding
import com.example.musicapp.databinding.RowSingerBinding
import com.example.musicapp.models.ArtistsModel
>>>>>>> origin/master
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

<<<<<<< HEAD
class SingerListActivity : AppCompatActivity(), OnSelectedArtistsIdsListener {
=======
class SingerListActivity : AppCompatActivity() {
>>>>>>> origin/master

    private lateinit var binding: ActivitySingerListBinding

    private lateinit var artistsArrayList: ArrayList<ArtistsModel>

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var artistsAdapters: ArtistsAdapters

<<<<<<< HEAD
    private lateinit var spinKitView: RelativeLayout

    private lateinit var listArtistsId: ArrayList<String>

=======
>>>>>>> origin/master
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

<<<<<<< HEAD
        binding.successBtn.visibility = View.GONE

=======
>>>>>>> origin/master
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

<<<<<<< HEAD
        spinKitView = binding.loading

        spinKitView.visibility = View.VISIBLE

        loadArtists()

        val user = intent.getSerializableExtra("user") as? User

        binding.successBtn.setOnClickListener {
            if (user != null) {
                addUserFireBase(user)
            } else {
                Toast.makeText(this, "Đã xảy ra lỗi xin thử lại", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this, AuthSelectionActivity::class.java))
            }
        }

=======
        loadArtists()
>>>>>>> origin/master
    }

    private fun loadArtists() {
        artistsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Artists")
<<<<<<< HEAD
        ref.addValueEventListener(object : ValueEventListener {
=======
        ref.addValueEventListener(object: ValueEventListener {
>>>>>>> origin/master
            override fun onDataChange(snapshot: DataSnapshot) {
                artistsArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ArtistsModel::class.java)
<<<<<<< HEAD
                    artistsArrayList.add(model!!)
                }
                artistsAdapters = ArtistsAdapters(this@SingerListActivity, artistsArrayList)
                artistsAdapters.setOnSelectedArtistsIdsListener(this@SingerListActivity)
                spinKitView.visibility = View.GONE
=======

                    artistsArrayList.add(model!!)
                }
                artistsAdapters = ArtistsAdapters(this@SingerListActivity, artistsArrayList)

>>>>>>> origin/master
                binding.categoriesRv.adapter = artistsAdapters
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

<<<<<<< HEAD
    override fun onSelectedArtistsIdsUpdated(selectedArtistsIds: ArrayList<String>) {
        Log.d("hieu73", selectedArtistsIds.size.toString())
        if (selectedArtistsIds.size >= 3) {
            binding.successBtn.visibility = View.VISIBLE
        } else {
            binding.successBtn.visibility = View.GONE
        }
        listArtistsId = selectedArtistsIds
    }

    private fun addUserFireBase(user: User?) {
        val hashMap: HashMap<String, Any?> = HashMap()
        Log.d("Usse", listArtistsId.toString())
        if (user != null) {
            hashMap["uid"] = user.uid
            hashMap["email"] = user.email
            hashMap["name"] = user.name
            hashMap["profileImage"] = user.profileImage
            hashMap["userType"] = user.userType
            hashMap["timestamp"] = user.timestamp
            hashMap["birthday"] = user.birthday
            hashMap["checkFingerprint"] = user.checkFingerprint
            hashMap["listArtistsId"] = listArtistsId

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(user.uid)
                .setValue(hashMap)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("userImage", user.profileImage)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed saving user info due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

=======
>>>>>>> origin/master
}