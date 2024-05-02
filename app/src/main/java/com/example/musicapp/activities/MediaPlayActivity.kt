package com.example.musicapp.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMediaPlayBinding
import com.example.musicapp.service.MusicService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView

class MediaPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayBinding

    private var checkIsPlay = false
    private var checkIsLogin = true
    private var audioUrl = ""
    private var idAlbum = ""
    private var nameAlbum = ""
    private var nameSong = ""
    private var nameArtists = ""
    private var imageAlbum = ""

    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "titleSong" -> {
                    binding.nameSong.text = sharedPreferences.getString("titleSong", null)
                    nameSong = sharedPreferences.getString("titleSong", null).toString()
                }

                "nameArtists" -> {
                    binding.nameArtists.text = sharedPreferences.getString("nameArtists", null)
                    nameArtists = sharedPreferences.getString("nameArtists", null).toString()
                }

                "imageAlbum" -> {
                    Glide.with(this)
                        .load(sharedPreferences.getString("imageAlbum", null))
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileSong)
                    imageAlbum = sharedPreferences.getString("imageAlbum", null).toString()
                }

                "checkIsPlay" -> {
                    checkIsPlay = sharedPreferences.getBoolean("checkIsPlay", false)
                    Log.d("hieu41", checkIsPlay.toString())
                    if (checkIsPlay) {
                        binding.playBtn.setImageResource(R.drawable.pause)
                    } else {
                        binding.playBtn.setImageResource(R.drawable.play)
                    }
                }

                "checkIsLogin" -> {
                    checkIsLogin = sharedPreferences.getBoolean("checkIsLogin", true)
                }

                "preview" -> {
                    val previewUrl = sharedPreferences.getString("preview", null)
                    if (previewUrl != null) {
                        audioUrl = previewUrl
                    }
                }

                "idAlbum" -> idAlbum = sharedPreferences.getString("idAlbum", null).toString()

                "titleAlbum" -> {
                    binding.album.text = sharedPreferences.getString("titleAlbum", null)
                    nameAlbum = sharedPreferences.getString("titleAlbum", null).toString()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playBtn.setOnClickListener {
            updatePlayButton()
        }

        binding.backBtn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
        }

        binding.moreVertBtn.setOnClickListener {
            showDialog()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        val nameSongDiaLog: TextView? = bottomSheetDialog.findViewById(R.id.nameSongDiaLog)
        val nameArtistsDiaLog: TextView? = bottomSheetDialog.findViewById(R.id.nameArtistsDiaLog)
        val imageDialog: ShapeableImageView = bottomSheetDialog.findViewById(R.id.imageDiaLog)!!
        nameArtistsDiaLog?.text = "$nameArtists + $nameAlbum"
        nameSongDiaLog?.text = nameSong
        Glide.with(this)
            .load(imageAlbum)
            .placeholder(R.drawable.ic_person_gray)
            .into(imageDialog)
        bottomSheetDialog.show()
    }


    private fun updatePlayButton() {
        val intentMain = Intent(this, MusicService::class.java)
        val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (checkIsPlay) {
            checkIsPlay = false
            intentMain.putExtra("active_type", MusicService.ACTION_PAUSE)
            startService(intentMain)
            binding.playBtn.setImageResource(R.drawable.play)
        } else {
            binding.playBtn.setImageResource(R.drawable.play)
            if (checkIsLogin) {
                checkIsLogin = false
                checkIsPlay = true
                intentMain.putExtra("active_type", MusicService.ACTION_PLAY)
                intentMain.putExtra("url_audio", audioUrl)
                startService(intentMain)
                binding.playBtn.setImageResource(R.drawable.pause)
            } else {
                checkIsPlay = true
                intentMain.putExtra("active_type", MusicService.ACTION_RESUME)
                startService(intentMain)
                binding.playBtn.setImageResource(R.drawable.pause)
            }
        }
        editor.putBoolean("checkAutoPlay", false)
        editor.putBoolean("checkIsPlay", checkIsPlay)
        editor.putBoolean("checkIsLogin", checkIsLogin)
        editor.putBoolean("checkIsLogin", false)
        editor.apply()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "titleSong")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "nameArtists")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "imageAlbum")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "checkIsPlay")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "checkIsLogin")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "preview")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "idAlbum")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "titleAlbum")
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }
}