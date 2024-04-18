package com.example.musicapp.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.musicapp.R
import com.example.musicapp.adapters.ArtistsAdapters
import com.example.musicapp.databinding.ActivitySingerListBinding
import com.example.musicapp.databinding.RowSingerBinding
import com.example.musicapp.models.ArtistsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SingerListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingerListBinding

    private lateinit var artistsArrayList: ArrayList<ArtistsModel>

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var artistsAdapters: ArtistsAdapters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        loadArtists()
    }

    private fun loadArtists() {
        artistsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Artists")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                artistsArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ArtistsModel::class.java)

                    artistsArrayList.add(model!!)
                }
                artistsAdapters = ArtistsAdapters(this@SingerListActivity, artistsArrayList)

                binding.categoriesRv.adapter = artistsAdapters
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}